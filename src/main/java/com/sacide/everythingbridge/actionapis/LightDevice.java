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
public abstract class LightDevice extends Device {
    
    public LightDevice(String ip, int port, String label) {
        super(ip, port, label);
    }
    public LightDevice(String ip, int port) {
        super(ip, port);
    }
    public LightDevice(Device d) {
        super(d);
    }
    
    public abstract void setLightPowerState(boolean on, long duration) throws IOException;
    
    public abstract boolean getLightPowerState() throws IOException;
    
    public abstract void setLightBrightness(double brightness, long duration) throws IOException;
    
}
