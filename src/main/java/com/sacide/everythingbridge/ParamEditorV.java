/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author saud
 */
public class ParamEditorV {
    
    private Properties params;
    
    public static final String SSL_KEY_FILE = "SSL Keystore File";
    public static final String SSL_KEY_ALIAS = "SSL Keystore Alias";
    public static final String SSL_KEY_STORE_PASS = "SSL Keystore pass";
    public static final String SSL_KEYPASS = "SSL Keypass";
    
    public ParamEditorV() {
        params = new Properties(new File("./params.prop"), " = ");
        if(!params.fileExists()) {
	    System.out.println("File 'params.prop' not found. Creating and adding default values...");
            params.put(SSL_KEY_FILE, "D:\\Dropbox\\Java Projects\\EverythingBridge\\src\\main\\resources\\mykey.keystore");
            params.put(SSL_KEY_ALIAS, "alias");
            params.put(SSL_KEY_STORE_PASS, "123456");
            params.put(SSL_KEYPASS, "abcdef");
            params.save();
        }
    }
    
    public void init() {
		params.load();
        
        ArrayList<String> errors = checkParamValues();
        if(!errors.isEmpty()) {
            for(String error : errors) {
                System.out.println(error);
            }
            System.out.println("Error"+(errors.size()>1 ? "s" : "")+"in 'params.prop' detected. Exiting program...");
            System.exit(0);
        }
    }
    
    private String checkParamValue(String param, String value) {
        switch(param) {
            case SSL_KEY_FILE:
                if(new File(value).exists())
                    return null;
                else
                    return "Error: File not found";
            case SSL_KEY_ALIAS:
                    return null;
            case SSL_KEY_STORE_PASS:
                    return null;
            case SSL_KEYPASS:
                    return null;
            default:
                return "Error: param not found";
        }
    }
    
    private ArrayList<String> checkParamValues() {
        ArrayList<String> errors = new ArrayList<String>();
        for (Map.Entry<String, String> entry : params.getMap().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String ret = checkParamValue(key, value);
            if(ret == null) continue;
            errors.add(key+" => "+ret);
        }
        return errors;
    }
    
    public int getParamAsInt(String param) {
        return Integer.parseInt(params.get(param));
    }
    public String getParamAsString(String param) {
        return params.get(param);
    }
    
}
