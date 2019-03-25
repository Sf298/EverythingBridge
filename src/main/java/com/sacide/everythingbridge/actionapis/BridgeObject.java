/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge.actionapis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/**
 *
 * @author saud
 */
public class BridgeObject {
    
    public final String name;
    public final String ip;
    public final String mac;
    public final String id;

    public BridgeObject(String name, String ip, String mac, String id) {
        this.name = name;
        this.ip = ip;
        this.mac = mac;
        this.id = id;
    }
    
    public BridgeObject(JSONObject obj) {
        this.id = obj.getString("id");
        this.ip = obj.getString("internalipaddress");
        this.mac = obj.getString("macaddress");
        this.name = obj.getString("name");
    }
    
    public String getFormattedString() {
        if(name != null) {
            return name+" - "+ip;
        } else {
            return ip;
        }
    }
    
    public static String findBridgeFromFormattedString(HashMap<String, BridgeObject> bridges, String formattedString) {
        for(Map.Entry<String, BridgeObject> bridge : bridges.entrySet()) {
            BridgeObject value = bridge.getValue();
            if(value.getFormattedString().equals(formattedString))
                return value.mac;
        }
        return "";
    }
    public static String[] getBridgesComboBox(HashMap<String, BridgeObject> bridges) {
        ArrayList<String> out = new ArrayList<>();
        for(Map.Entry<String, BridgeObject> bridge : bridges.entrySet()) {
            BridgeObject value = bridge.getValue();
            out.add(value.getFormattedString());
        }
        return out.toArray(new String[out.size()]);
    }
    
}
