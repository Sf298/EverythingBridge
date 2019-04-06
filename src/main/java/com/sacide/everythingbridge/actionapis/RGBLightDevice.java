/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge.actionapis;

import LifxCommander.Messages.DataTypes.HSBK;
import java.io.IOException;

/**
 *
 * @author saud
 */
public abstract class RGBLightDevice extends LightDevice {
    
    public RGBLightDevice(String ip, int port, String label) {
        super(ip, port, label);
    }

    public RGBLightDevice(String ip, int port) {
        super(ip, port);
    }

    public RGBLightDevice(Device d) {
        super(d);
    }
    
    public abstract void setLightColor(HSBK hsbk, long duration) throws IOException;
    
    public abstract HSBK getLightColor() throws IOException;
    
}
