/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge.actionapis;

import LifxCommander.ControlMethods;
import LifxCommander.Messages.DataTypes.Command;
import LifxCommander.Messages.DataTypes.HSBK;
import LifxCommander.Messages.DataTypes.Payload;
import LifxCommander.Messages.Device.Acknowledgement;
import LifxCommander.Messages.Device.GetLabel;
import LifxCommander.Messages.Device.GetService;
import LifxCommander.Messages.Device.StateLabel;
import LifxCommander.Messages.Device.StateService;
import LifxCommander.Messages.Light.Get;
import LifxCommander.Messages.Light.GetPower_Light;
import LifxCommander.Messages.Light.SetColor;
import LifxCommander.Messages.Light.SetPower_Light;
import LifxCommander.Messages.Light.StatePower_Light;
import LifxCommander.Messages.Light.State_Light;
import LifxCommander.Values.Levels;
import LifxCommander.Values.Power;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author saud
 */
public class LifxCommanderW implements IActionAPI {
    
    public static final int SINGLE_TIMEOUT = 1000;
    public static final int PORT = 56700;
    public static final String BROADCAST_IP = "255.255.255.255";
    
    public static void test() {
        try {
            LifxCommanderW lifx = new LifxCommanderW();
            lifx.discoverDevices();
            RGBLightDevice ld = lifx.toRGBLightDevice(new Device("192.168.0.56", 56700));
            ld.setLightPowerState(true, 0);
            
            
            HSBK col;
            double num = Math.random();
            if(num<(1/4f)) {
                col = HSBK.CRIMSON;
                System.out.println("red");
            } else if(num<(2/4f)) {
                col = HSBK.INDIGO;
                System.out.println("indigo");
            } else if(num<(3/4f)) {
                col = HSBK.FOREST_GREEN;
                System.out.println("green");
            } else {
                col = HSBK.INCANDESCENT;
                System.out.println("incan");
            }
            ld.setLightColor(HSBK.DAYLIGHT, 0);
            ld.setLightBrightness(0.47, 0);
        } catch (IOException ex) {
            Logger.getLogger(LifxCommanderW.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
        
    }
    
    @Override
    public Collection<Device> discoverDevices() {
        HashSet<Device> out = new HashSet<>();
        try {
            GetService gs = new GetService();
            ControlMethods.sendBroadcastMessage(new Command(gs).getByteArray(), PORT);
            
            for(DatagramPacket response : ControlMethods.receiveAllUdpMessages(1000)) {
                StateService stateService = (StateService) buildPayload(response);
                long port = stateService.getPort();
                Device d = new Device(response.getAddress().getHostAddress(), (int)port);
                
                GetLabel getLabel = new GetLabel();
                ControlMethods.sendUdpMessage(new Command(getLabel).getByteArray(), d.ip_id, d.port);
                DatagramPacket labelArr = ControlMethods.receiveUdpMessage(SINGLE_TIMEOUT);
                StateLabel stateLabel = (StateLabel) buildPayload(labelArr);
                d.label = stateLabel.getLabel();
                
                out.add(d);
            }
        } catch (IOException ex) {
            Logger.getLogger(LifxCommanderW.class.getName()).log(Level.SEVERE, null, ex);
        }
        return out;
    }
    
    public RGBLightDevice toRGBLightDevice(Device d) {
        return new RGBLightDevice(d) {
            @Override
            public void setLightPowerState(boolean on, long duration) throws IOException {
                for (int i = 0; i < 3; i++) { // try 3 times in case of faliure
                    SetPower_Light setPower = new SetPower_Light(on?Power.ON:Power.OFF, duration);
                    Command comm = new Command(setPower);
                    comm.getFrameAddress().setAckRequired(true);
                    ControlMethods.sendUdpMessage(comm.getByteArray(), ip_id, port);

                    DatagramPacket labelArr = ControlMethods.receiveUdpMessage(SINGLE_TIMEOUT);
                    if(labelArr==null) continue;
                    Acknowledgement state = (Acknowledgement) buildPayload(labelArr);
                    if(state.getCode() == 45)
                        return;
                }
                throw new IOException("No response from device");
            }
            @Override
            public boolean getLightPowerState() throws IOException {
                for (int i = 0; i < 3; i++) { // try 3 times in case of faliure
                    GetPower_Light getPower = new GetPower_Light();
                    Command comm = new Command(getPower);
                    comm.getFrameAddress().setResRequired(true);
                    ControlMethods.sendUdpMessage(comm.getByteArray(), ip_id, port);

                    DatagramPacket labelArr = ControlMethods.receiveUdpMessage(SINGLE_TIMEOUT);
                    if(labelArr==null) continue;
                    StatePower_Light state = (StatePower_Light) buildPayload(labelArr);
                    return state.getLevel() > 0;
                }
                throw new IOException("No response from device");
            }
            @Override
            public void setLightColor(HSBK hsbk, long duration) throws IOException {
                if(hsbk.hasEmpty())
                    hsbk.updateEmptyWith(getLightColor());

                for (int i = 0; i < 3; i++) { // try 3 times in case of faliure
                    SetColor setColor = new SetColor(hsbk, duration);
                    Command comm = new Command(setColor);
                    comm.getFrameAddress().setAckRequired(true);
                    ControlMethods.sendUdpMessage(comm.getByteArray(), ip_id, port);

                    DatagramPacket labelArr = ControlMethods.receiveUdpMessage(SINGLE_TIMEOUT);
                    if(labelArr==null) continue;
                    Acknowledgement state = (Acknowledgement) buildPayload(labelArr);
                    if(state.getCode() == 45)
                        return;
                }
                throw new IOException("No response from device");
            }
            @Override
            public void setLightBrightness(double brightness, long duration) throws IOException {
                ((RGBLightDevice)d).setLightColor(new HSBK(-1, -1, (int) (Levels.MAX*brightness), -1), duration);
            }
            @Override
            public HSBK getLightColor() throws IOException {
                for (int i = 0; i < 3; i++) { // try 3 times in case of faliure
                    Get get = new Get();
                    Command comm = new Command(get);
                    comm.getFrameAddress().setResRequired(true);
                    ControlMethods.sendUdpMessage(comm.getByteArray(), ip_id, port);

                    DatagramPacket labelArr = ControlMethods.receiveUdpMessage(SINGLE_TIMEOUT);
                    if(labelArr==null) continue;
                    State_Light state = (State_Light) buildPayload(labelArr);
                    return state.getColor();
                }
                throw new IOException("No response from device");
            }
        };
    }
    
    
    private static Payload buildPayload(DatagramPacket packet) {
        return (Payload) buildPayload(packet.getData());
    }
    private static Payload buildPayload(byte[] arr) {
        Command c = new Command();
        c.setFromCommandByteArray(arr);
        return (Payload) c.getPayload();
    }
    
}
