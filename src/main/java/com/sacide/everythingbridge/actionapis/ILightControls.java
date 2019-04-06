/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge.actionapis;

import LifxCommander.Messages.DataTypes.HSBK;
import java.io.IOException;
import javafx.scene.paint.Color;

/**
 *
 * @author saud
 */
public interface ILightControls {
    
    /**
     * Turns device on or off.
     * @param d the device to interact with.
     * @param on if true, turns the device on.
     * @param duration the animation time for the change. milliseconds
     * @throws java.io.IOException
     */
    public void setLightPowerState(Device d, boolean on, long duration) throws IOException;
    
    /**
     * Turns devices on or off.
     * @param on if true, turns the devices on.
     * @param duration the animation time for the change. milliseconds
     * @throws java.io.IOException
     */
    public void broadcastLightPowerState(boolean on, long duration) throws IOException;
    
    /**
     * Queries the given device for its current color.
     * @param d the device to interact with.
     * @return the color of the given device.
     * @throws java.io.IOException
     */
    public boolean getLightPowerState(Device d) throws IOException;
    
    
    /**
     * Changes the color of the given device.
     * @param d the device to interact with.
     * @param hsbk the color to change to. values set to -1 will not change.
     * @param duration the animation time for the change. milliseconds
     * @throws java.io.IOException
     */
    public void setLightColor(Device d, HSBK hsbk, long duration) throws IOException;
    
    /**
     * Changes the color of the given device.
     * @param d the device to interact with.
     * @param brightness a value from 0 to 1 where 0 is the dimmest and 1 is the brightest
     * @param duration the animation time for the change. milliseconds
     * @throws java.io.IOException
     */
    public void setLightBrightness(Device d, double brightness, long duration) throws IOException;
    
    /**
     * Broadcasts a color change instruction to the given color.
     * @param hsbk the color to change to. values set to -1 will not change.
     * @param duration the animation time for the change. milliseconds
     * @throws java.io.IOException
     */
    public void broadcastLightColor(HSBK hsbk, long duration) throws IOException;
    
    /**
     * Queries the given device for its current color.
     * @param d the device to interact with.
     * @return the color of the given device.
     * @throws java.io.IOException
     */
    public HSBK getLightColor(Device d) throws IOException;
    
}
