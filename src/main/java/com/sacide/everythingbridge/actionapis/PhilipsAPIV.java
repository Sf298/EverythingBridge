/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge.actionapis;

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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
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
public final class PhilipsAPIV {
    
    private final String DISCOVERY_URL = "https://discovery.meethue.com/";
    private HashMap<String, BridgeObject> bridges;
    private String selectedBridgeMAC;           //*
    
    private String username = "";               //*
    
    private int status = 0;
    public static final int STATUS_DICONNECTED = 0;
    public static final int STATUS_GOT_BRIDGES = 1;
    public static final int STATUS_BRIDGE_SELECTED = 2;
    public static final int STATUS_GOT_USERNAME = 3;
    
    private final Window parentWindow;
    private final Properties saveFile; // username, selected bridge mac
    
    /**
     * Creates a PhilipsAPIV Object.
     * @param parentWindow The parent window to freeze when show() is called.
     */
    public PhilipsAPIV(Window parentWindow) {
        this(parentWindow, new File("./philips.prop"));
    }
    
    /**
     * Creates a PhilipsAPIV Object.
     * @param parentWindow The parent window to freeze when show() is called.
     * @param persistenceFile The file in which to store the username and preferred bridge mac. 
     */
    public PhilipsAPIV(Window parentWindow, File persistenceFile) {
        this.parentWindow = parentWindow;
        saveFile = new Properties(persistenceFile);
        if(saveFile.fileExists()) {
            load();
        } else {
            discoverBridges();
            System.out.println("WARNING: Philips API not setup!");
            save();
        }
    }
    
    /**
     * Shows a UI for editing the settings.
     */
    public void show() {
        discoverBridges();
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
            JLabel statusPanel = new JLabel("Status: ");
            ActionListener statusLabelUpdater = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    switch(status) {
                        case STATUS_DICONNECTED:
                            statusPanel.setText("Status: No bridges found");
                            break;
                        case STATUS_GOT_BRIDGES:
                            statusPanel.setText("Status: Multiple bridges found");
                            break;
                        case STATUS_BRIDGE_SELECTED:
                            statusPanel.setText("Status: Bridge selected");
                            break;
                        case STATUS_GOT_USERNAME:
                            statusPanel.setText("Status: Connected");
                            break;
                    }
                }
            };
            addStatusChangeListener(statusLabelUpdater);
            statusLabelUpdater.actionPerformed(null);
            mainPanel.add(statusPanel);
        
            // bridge selector
            mainPanel.add(new JLabel("Selected bridge:"));
            JComboBox bridgeCombo = new JComboBox(BridgeObject.getBridgesComboBox(bridges));
            bridgeCombo.setSelectedItem(bridges.get(selectedBridgeMAC).getFormattedString());
            bridgeCombo.addActionListener((ActionEvent e) -> {
                selectedBridgeMAC = BridgeObject.findBridgeFromFormattedString(bridges,
                        (String) bridgeCombo.getSelectedItem());
            });
            mainPanel.add(bridgeCombo);
            
            if(status == STATUS_BRIDGE_SELECTED && username.length() > 0) {
                checkUsernameValid();
            }
            
            mainPanel.add(new JLabel("Authenticate device (press button on bridge then click 'Auth'):"));
            JButton unameButton = new JButton("Auth");
            unameButton.addActionListener((ActionEvent e) -> {
                createUsername();
            });
            addStatusChangeListener((ActionEvent e) -> {
                unameButton.setEnabled(status == STATUS_BRIDGE_SELECTED);
        });
            unameButton.setEnabled(status == STATUS_BRIDGE_SELECTED);
            mainPanel.add(unameButton);
            
        
        final JComponent[] inputs = new JComponent[] {mainPanel};
        JOptionPane.showConfirmDialog(parentWindow, inputs, "Philips API Config", JOptionPane.PLAIN_MESSAGE);
        saveFile.save();
    }
    
    /**
     * Updates the cache available bridges.
     */
    public void discoverBridges() {
        try {
            String str = getFromHttps(new URL(DISCOVERY_URL));
            JSONArray objs = new JSONArray(str);
            bridges.clear();
            for(int i=0; i<objs.length(); i++) {
                BridgeObject temp = new BridgeObject(objs.getJSONObject(i));
                bridges.put(temp.mac, temp);
            }
            
            if(bridges.size() == 1) {
                for(Map.Entry<String, BridgeObject> entry : bridges.entrySet())
                    selectedBridgeMAC = entry.getKey();
                setStatus(STATUS_BRIDGE_SELECTED);
            } else if(bridges.size() > 1) {
                setStatus(STATUS_GOT_BRIDGES);
            } else if(bridges.size() == 0) {
                selectedBridgeMAC = "";
                setStatus(STATUS_DICONNECTED);
            }
            if(!bridges.containsKey(selectedBridgeMAC)) {
                selectedBridgeMAC = "";
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(PhilipsAPIV.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PhilipsAPIV.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Initiates the routine for creating the username (API key).
     * Must be called within 30 sec of pressing the physical button on the bridge.
     */
    public void createUsername() {
        try {
            String bridgeIP = bridges.get(selectedBridgeMAC).ip;
            URL url = new URL("http://"+bridgeIP+"/api");
            JSONArray response = new JSONArray(makeAPIRequest(url, "POST", "\"devicetype\"", "\"EverythingBridge#EBUser\""));
            username = response.getJSONObject(0).getJSONObject("success").getString("username");
        } catch (IOException ex) {
            Logger.getLogger(PhilipsAPIV.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Checks if the current provided username works and updates the status accordingly.
     */
    private void checkUsernameValid() {
        if(status < STATUS_BRIDGE_SELECTED)
            return;
        
        boolean isUsernameValid = false;
        if(username.length() > 0) {
            // TODO: test username
            JSONObject response = getLights();
            isUsernameValid = !response.toString().contains("unauthorized user");
        }
        if(isUsernameValid) {
            setStatus(STATUS_GOT_USERNAME);
        } else {
            setStatus(STATUS_BRIDGE_SELECTED);
        }
            
    }
    
    /**
     * Gets the status of the connection to the bridge.
     * @return The status.
     */
    public int getStatus() {
        return status;
    }
    
    /**
     * Gets the currently stored username (API key)
     * @return The username.
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Gets the MAC address of the currently selected bridge.
     * @return The MAC address.
     */
    public String getSelectedBridgeMAC() {
        return selectedBridgeMAC;
    }
    
    /**
     * Sets the currently used bridge to the given MAC address.
     * Only works if the given address is available.
     * @param mac The MAC address.
     * @return Whether or not the operation was successful.
     */
    public boolean setSelectedBridgeMAC(String mac) {
        discoverBridges();
        if(!bridges.containsKey(mac))
            return false;
        selectedBridgeMAC = mac;
        return true;
    }
    
    /**
     * Gets a copy of the list of available bridges.
     * @return The list of available bridges.
     */
    public HashMap<String, BridgeObject> getAvailableBridges() {
        return new HashMap<>(bridges);
    }
    
    /**
     * Gets a list of available lights from the bridge.
     * @return The JSONObject as received from the bridge. Refer to the Hue API.
     */
    public JSONObject getLights() {
        try {
            return new JSONObject(makeAPIRequest("/api/"+username+"/lights", "GET"));
        } catch (IOException ex) {
            Logger.getLogger(PhilipsAPIV.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * Sets the properties for a light.
     * Sample properties: ("hue", 50000, "on", true, "bri", 200, "effect", "colorloop")
     * @param id The light ID. Can be found using getLights().
     * @param properties The properties to change. Refer to the Hue API.
     * @return The JSONObject as received from the bridge. Refer to the Hue API.
     */
    public JSONObject setLightProps(int id, String... properties) {
        try {
            return new JSONObject(makeAPIRequest("/api/"+username+"/lights/"+id+"/state", "PUT"), properties);
        } catch (IOException ex) {
            Logger.getLogger(PhilipsAPIV.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * Sets the list of all properties of a light.
     * @param id The light ID. Can be found using getLights().
     * @return The JSONObject as received from the bridge. Refer to the Hue API.
     */
    public JSONObject getLightProps(int id) {
        try {
            return new JSONObject(makeAPIRequest("/api/"+username+"/lights/"+id, "GET"));
        } catch (IOException ex) {
            Logger.getLogger(PhilipsAPIV.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
    private static String makeAPIRequest(URL url, String method, String... properties) throws IOException {
        if(System.currentTimeMillis() < nextRequestTime) {
            try {
                Thread.sleep(nextRequestTime - System.currentTimeMillis());
            } catch (InterruptedException ex) {
                Logger.getLogger(PhilipsAPIV.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod(method.toUpperCase());
        
        if(properties != null) {
            String body = properties2body(properties);
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
        return sb.toString();
    }
    /**
     * Sends a request and receives a response.
     * @param context The context to send the request to.
     * @param method The HTTP method to use.
     * @param properties The properties to include in the body.
     * @return The String response as received from the bridge. Refer to the Hue API. 
     * @throws If an I/O error occurs.
     */
    public String makeAPIRequest(String context, String method, String... properties) throws IOException {
        String bridgeIP = bridges.get(selectedBridgeMAC).ip;
        return makeAPIRequest(new URL("http://"+bridgeIP+ (context.startsWith("/")?"":"/") +context), method, properties);
    }
    
    
    private static String properties2body(String... properties) {
        StringBuilder body = new StringBuilder("{");
            for(int i=0; i<properties.length; i+=2) {
                body = body.append (properties[i]).append (": ").append (properties[i+1]). append(",");
            }
            return body.deleteCharAt(body.length()-1).append("}").toString();
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
    
    private final HashSet<ActionListener> statusChangeListeners = new HashSet<>();
    private void setStatus(int status) {
        this.status = status;
        for(ActionListener actionListener : statusChangeListeners) {
            actionListener.actionPerformed(new ActionEvent(this, status, "", status));
        }
    }
    
    /**
     * Adds an ActionListener that is called when the connection status is updated.
     * @param l The listener to add
     */
    public void addStatusChangeListener(ActionListener l) {
        statusChangeListeners.add(l);
    }
    /**
     * Removes the ActionListener.
     * @param l The listener to remove
     */
    public void removeStatusChangeListener(ActionListener l) {
        statusChangeListeners.remove(l);
    }
    
    public void save() {
        saveFile.put("username", username);
        saveFile.put("selectedBridgeMAC", selectedBridgeMAC);
        saveFile.save();
    }
    public void load() {
        saveFile.load();
        discoverBridges();

        username = saveFile.get("username");
        if(bridges.containsKey(saveFile.get("selectedBridgeMAC"))) {
            selectedBridgeMAC = saveFile.get("selectedBridgeMAC");
        }

        checkUsernameValid();
    }
}
