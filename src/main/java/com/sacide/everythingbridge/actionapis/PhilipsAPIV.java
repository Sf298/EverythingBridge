/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge.actionapis;

import LifxCommander.Messages.DataTypes.HSBK;
import LifxCommander.Values.Levels;
import com.sacide.everythingbridge.Properties;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import javafx.scene.paint.Color;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.security.sasl.AuthenticationException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.json.*;

/**
 *
 * @author saud
 */
public final class PhilipsAPIV implements IActionAPI {
    
    private final String DISCOVERY_URL = "https://discovery.meethue.com/";
    private HashMap<String, BridgeObj> allBridges;                   //*
    private HashMap<String, BridgeObj> onlineBridges;
    
    private final Properties saveFile; // username, selected bridge mac
    
    public static void test() {
        PhilipsAPIV ph = new PhilipsAPIV();
        Collection<Device> devices = ph.discoverDevices();
        
        for(Map.Entry<String, BridgeObj> entry : ph.allBridges.entrySet()) {
            BridgeObj value = entry.getValue();
            System.out.println(value.id+" "+value.ip+" "+value.uname);
        }
        
        System.out.println("num devices: "+devices.size());
        for(Device d : devices) {
            System.out.println(d);
            if(!(d instanceof LightDevice)) continue;
            try {
                LightDevice ld = (LightDevice) d;
                ld.setLightPowerState(false, 0);
                ld.setLightBrightness(0.47, 0);
            } catch (IOException ex) {
                Logger.getLogger(PhilipsAPIV.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        System.exit(0);
    }
    
    /**
     * Creates a PhilipsAPIV Object.
     */
    public PhilipsAPIV() {
        this(new File("./philips.prop"));
    }
    
    /**
     * Creates a PhilipsAPIV Object.
     * @param parentWindow The parent window to freeze when show() is called.
     * @param persistenceFile The file in which to store the username and preferred bridge mac. 
     */
    public PhilipsAPIV(File persistenceFile) {
        saveFile = new Properties(persistenceFile);
        if(saveFile.fileExists()) {
            load();
        } else {
            allBridges = new HashMap<>();
            discoverBridges();
            save();
        }
    }
    
    
    @Override
    public Collection<Device> discoverDevices() {
        discoverBridges();
        HashSet<Device> out = new HashSet<>();
        out.addAll(discoverAllLights());
        return out;
    }
    
    /**
     * Updates cache of bridges.
     */
    public void discoverBridges() {
        try {
            String str = getFromHttps(new URL(DISCOVERY_URL));
            JSONArray objs = new JSONArray(str);
            onlineBridges = new HashMap<>();
            for(int i=0; i<objs.length(); i++) {
                BridgeObj temp = new BridgeObj(objs.getJSONObject(i));
                if(allBridges.containsKey(temp.id)) {
                    BridgeObj old = allBridges.get(temp.id);
                    old.ip = temp.ip;
                    onlineBridges.put(old.id, old);
                } else {
                    allBridges.put(temp.id, temp);
                    onlineBridges.put(temp.id, temp);
                }
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(PhilipsAPIV.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PhilipsAPIV.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Gets a list of available lights from all available bridges.
     * @return returns all the lights
     */
    public HashSet<LightDevice> discoverAllLights() {
        HashSet<LightDevice> out = new HashSet<>();
        for(Map.Entry<String, BridgeObj> entry : onlineBridges.entrySet()) {
            BridgeObj bridge = entry.getValue();
            if(bridge.uname == null) continue;
            
            try {
                String resp = makeAPIRequest(parseURL(bridge, "/api/<uname>/lights"), "GET", null);
                if(!isJSONObj(resp)) continue;
                JSONObject lights = new JSONObject(resp);
                for(String lightID : lights.keySet()) {
                    JSONObject light = lights.getJSONObject(lightID);
                    boolean reachable = light.getJSONObject("state").getBoolean("reachable");
                    if(reachable) {
                        Device d = new Device(bridge.id, Integer.parseInt(lightID));
                        d.label = light.getString("name");
                        out.add(toLightDevice(d));
                    }
                }
            } catch (AuthenticationException ex) {
                bridge.uname = null;
            } catch (IOException ex) {
                Logger.getLogger(PhilipsAPIV.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return out;
    }

    /**
     * Attempts to log in to all discovered bridges
     */
    public void tryAuth() {
        for (Map.Entry<String, BridgeObj> entry : onlineBridges.entrySet()) {
            BridgeObj val = entry.getValue();
            if(val.uname != null) continue;
            try {
                JSONArray response = new JSONArray(makeAPIRequest(parseURL(val, "/api"), "POST",
                        properties2body("\"devicetype\"", "\"EverythingBridge#EBUser\"")));
                JSONObject reponse2 = response.getJSONObject(0);
                if(reponse2.has("success"))
                    val.uname = reponse2.getJSONObject("success").getString("username");
                else
                    throw new AuthenticationException("link button not pressed");
            } catch (AuthenticationException ex) {
            } catch (IOException ex) {
                Logger.getLogger(PhilipsAPIV.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    
    
    private static final long REQUEST_FREQ_TIME = 100;
    private static long nextRequestTime = 0;
    /**
     * Sends a request and receives a response.
     * @param url The url to send the request to.
     * @param method The HTTP method to use.
     * @param properties The properties to include in the body.
     * @return The String response as received from the bridge. Refer to the Hue API. 
     * @throws If an I/O error occurs.
     */
    private String makeAPIRequest(URL url, String method, String body) throws AuthenticationException, IOException {
        if(System.currentTimeMillis() < nextRequestTime) {
            try {
                Thread.sleep(nextRequestTime - System.currentTimeMillis());
            } catch (InterruptedException ex) {
                Logger.getLogger(PhilipsAPIV.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method.toUpperCase());
        conn.setDoOutput(true);
        
        if(body != null) {
            conn.setRequestProperty("Content-Length", Integer.toString(body.length()));
            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes("UTF8"));
            os.flush();
            os.close();
        }
        
        Scanner in = new Scanner(conn.getInputStream());
        StringBuilder sb = new StringBuilder();
        while(in.hasNext()) {
            sb.append(in.nextLine()).append("\n");
        }
        in.close();
        nextRequestTime = System.currentTimeMillis() + REQUEST_FREQ_TIME;
        String resp = sb.toString();
        
        if(isJSONArr(resp) && resp.contains("error") && resp.contains("\"type\": 101"))
            throw new AuthenticationException(resp);
        
        return resp;
    }
    private static String getFromHttps(URL url) throws IOException {
        HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        
        StringBuilder sb = new StringBuilder();
        String input;
        while ((input = br.readLine()) != null){
            sb.append(input).append("\n");
        }
        br.close();
        
        return sb.toString();
    }
    
    /**
     * 
     * @param b
     * @param context eg "/api/<uname>/lights/<deviceId>/state"
     * @return
     * @throws MalformedURLException 
     */
    private URL parseURL(BridgeObj b, String context) throws MalformedURLException {
        context = context.replace("<uname>", b.uname);
        return new URL("http://"+b.ip+context);
    }
    /**
     * 
     * @param d
     * @param context eg "/api/<uname>/lights/<deviceId>/state"
     * @return
     * @throws MalformedURLException 
     */
    private URL parseURL(Device d, String context) throws MalformedURLException {
        BridgeObj bridge = onlineBridges.get(d.ip_id);
        context = context.replace("<uname>", bridge.uname);
        context = context.replace("<deviceId>", d.port+"");
        if(!context.startsWith("/")) context = "/"+context;
        return new URL("http://" + bridge.ip + context);
    }
    
    private static String properties2body(String... properties) {
        StringBuilder body = new StringBuilder("{");
            for(int i=0; i<properties.length; i+=2) {
                body = body.append (properties[i]).append (": ").append (properties[i+1]). append(",");
            }
            return body.deleteCharAt(body.length()-1).append("}").toString();
    }
    
    /**
     * Shows a UI for editing the settings.
     * @param parentWindow the parent window
     */
    public void show(Window parentWindow) {
        discoverBridges();
        
        JPanel mainPanel = new JPanel(new BorderLayout());
            
            mainPanel.add(new JLabel("Press the button on the bridges you wish to authenticate with then click 'Auth'"));
            JButton unameButton = new JButton("Auth");
            unameButton.addActionListener((ActionEvent e) -> {
                discoverBridges();
                tryAuth();
            });
            mainPanel.add(unameButton);
            
        
        final JComponent[] inputs = new JComponent[] {mainPanel};
        JOptionPane.showConfirmDialog(parentWindow, inputs, "Philips API Config", JOptionPane.PLAIN_MESSAGE);
        saveFile.save();
    }
    
    public void save() {
        for(BridgeObj val : allBridges.values()) {
            saveFile.put(val.id, val.uname);
        }
        saveFile.save();
    }
    public void load() {
        saveFile.load();
        allBridges = new HashMap<>();
        
        for (Map.Entry<String, String> entry : saveFile.getMap().entrySet()) {
            String id = entry.getKey();
            String uname = entry.getValue();
            
            BridgeObj b = new BridgeObj(null, id);
            b.uname = uname;
            allBridges.put(id, b);
        }
    }
    
    
    
    public LightDevice toLightDevice(Device d) {
        return new LightDevice(d) {
            @Override
            public void setLightPowerState(boolean on, long duration) throws IOException {
                makeAPIRequest(parseURL(this, "/api/<uname>/lights/<deviceId>/state"), "PUT",
                        properties2body(
                                "\"on\"", on+"",
                                "\"transitiontime\"", (duration/100)+""));
            }
            @Override
            public boolean getLightPowerState() throws IOException {
                String response = makeAPIRequest(parseURL(this, "/api/<uname>/lights/<deviceId>"), "GET", null);
                JSONObject resp = new JSONObject(response);
                return resp.getJSONObject("state").getBoolean("on");
            }
            @Override
            public void setLightBrightness(double brightness, long duration) throws IOException {
                makeAPIRequest(parseURL(this, "/api/<uname>/lights/<deviceId>/state"), "PUT",
                        properties2body("\"bri\"", ( (int)(brightness*253)+1 )+""));
            }
        };
    }
    public LightDevice toRGBLightDevice(Device d) {
        return new RGBLightDevice(d) {
            @Override
            public void setLightPowerState(boolean on, long duration) throws IOException {
                makeAPIRequest(parseURL(this, "/api/<uname>/lights/<deviceId>/state"), "PUT",
                        properties2body(
                                "\"on\"", on+"",
                                "\"transitiontime\"", (duration/100)+""));
            }
            @Override
            public boolean getLightPowerState() throws IOException {
                String response = makeAPIRequest(parseURL(this, "/api/<uname>/lights/<deviceId>"), "GET", null);
                JSONObject resp = new JSONObject(response);
                return resp.getJSONObject("state").getBoolean("on");
            }
            @Override
            public void setLightColor(HSBK hsbk, long duration) throws IOException {
                if(hsbk.hasEmpty())
                    hsbk.updateEmptyWith(getLightColor());

                makeAPIRequest(parseURL(this, "/api/<uname>/lights/<deviceId>/state"), "PUT",
                        properties2body(
                                "\"hue\"", hsbk.getHue(65535)+"",
                                "\"sat\"", hsbk.getSaturation(254)+"",
                                "\"bri\"", (hsbk.getBrightness(253)+1)+"" ));
            }
            @Override
            public void setLightBrightness(double brightness, long duration) throws IOException {
                ((RGBLightDevice) d).setLightColor(new HSBK(-1, -1, (int) (HSBK.MAX_BRI*brightness), -1), duration);
            }
            @Override
            public HSBK getLightColor() throws IOException {
                String response = makeAPIRequest(parseURL(this, "/api/<uname>/lights/<deviceId>"), "GET", null);
                JSONObject state = new JSONObject(response).getJSONObject("state");
                HSBK out = new HSBK();
                if(state.has("hue"))
                    out.setHue(state.getInt("hue"), 65535);
                else
                    out.setHue(0);
                if(state.has("sat"))
                    out.setSaturation(state.getInt("sat"), 254);
                else
                    out.setHue(0);
                if(state.has("bri"))
                    out.setBrightness(state.getInt("bri")-1, 253);
                else
                    out.setHue(0);
                return out;
            }
        };
    }
    
    
    private static boolean isJSONArr(String json) {
        int arrInd = json.indexOf("[");
        if(arrInd == -1) return false;
        int objInd = json.indexOf("{");
        if(objInd == -1) return false;
        return arrInd < objInd;
    }
    private static boolean isJSONObj(String json) {
        int objInd = json.indexOf("{");
        if(objInd == -1) return false;
        int arrInd = json.indexOf("[");
        if(arrInd == -1) return true;
        return objInd < arrInd;
    }
    
    private class BridgeObj {
        public String ip;
        public final String id;
        public String uname = null;
        public BridgeObj(String ip, String id) {
            this.ip = ip;
            this.id = id;
        }
        public BridgeObj(JSONObject obj) {
            this.id = obj.getString("id");
            this.ip = obj.getString("internalipaddress");
        }
    }
}
