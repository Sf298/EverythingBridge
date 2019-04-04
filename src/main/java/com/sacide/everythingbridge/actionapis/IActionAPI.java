/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge.actionapis;

import java.util.Collection;

/**
 *
 * @author saud
 */
public interface IActionAPI {
    
    /**
     * Finds all available devices and stores them in a local cache.
     */
    public void discoverDevices();
    
    /**
     * Gets all devices stored in the local cache.
     * @return returns the devices.
     */
    public Collection<Device> getDevices();
    
}
