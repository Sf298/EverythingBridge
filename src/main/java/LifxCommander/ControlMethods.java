package LifxCommander;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControlMethods {
    
    private static DatagramSocket socket = null;
    private static final int PORT = 56700;
    static public void ensureSocketStarted() throws SocketException {
        if(socket == null)
            socket = new DatagramSocket(PORT);
    }
    
    static public void sendBroadcastMessage(byte[] messageByteArray, int port) throws IOException {
        ensureSocketStarted();
        socket.setBroadcast(true);
        
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while(interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            
            if(networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }
            
            for(InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress broadcast = interfaceAddress.getBroadcast();
                if(broadcast == null) continue;
            
                DatagramPacket packet = new DatagramPacket(messageByteArray, messageByteArray.length, broadcast, port);
                socket.send(packet);
            }
        }
    }
    
    static public void sendUdpMessage(String message, String ipAddress, int port) throws IOException {
        byte[] messageByteArray = CommonMethods.convertHexStringToByteArray(message);
        InetAddress address = InetAddress.getByName(ipAddress);
        DatagramPacket packet = new DatagramPacket(messageByteArray, messageByteArray.length, address, port);
        ensureSocketStarted();
        socket.send(packet);
    }
    
    static public void sendUdpMessage(byte[] messageByteArray, String ipAddress, int port) throws IOException {
        InetAddress address = InetAddress.getByName(ipAddress);
        DatagramPacket packet = new DatagramPacket(messageByteArray, messageByteArray.length, address, port);
        ensureSocketStarted();
        socket.send(packet);
    }
    
    static public byte[] receiveUdpMessage() throws IOException{
        ensureSocketStarted();
        byte[] data = new byte[1500];
            
        DatagramPacket packet = new DatagramPacket(data, data.length);
        while(true) {
            socket.receive(packet);
            return packet.getData();
        }
    }
    
    static public DatagramPacket receiveUdpMessage(int timeoutMs) throws IOException {
        ensureSocketStarted();
        socket.setSoTimeout(timeoutMs);
        byte[] data = new byte[1500];

        DatagramPacket packet = new DatagramPacket(data, data.length);
        try {
            socket.receive(packet);
            return packet;
        } catch (SocketTimeoutException ex) {
            //System.out.println("timeout");
        }
        return null;
    }

    public static ArrayList<DatagramPacket> receiveAllUdpMessages(int timeoutMs) throws IOException {
        ensureSocketStarted();
        ArrayList<DatagramPacket> out = new ArrayList<>();
        while(true) {
            DatagramPacket temp = receiveUdpMessage(timeoutMs);
            if(temp != null) {
                if(!isLocalIP(temp.getAddress())) {
                    out.add(temp);
                }
            } else
                break;
        }
        return out;
    }
    
    private static boolean isLocalIP(InetAddress address) throws IOException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface network = interfaces.nextElement();
            if (network.isLoopback() || ! network.isUp()) {
                continue;
            }
            Enumeration<InetAddress> inetAddresses = network.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress netAddress = inetAddresses.nextElement();
                if (address.getHostAddress().equals(netAddress.getHostAddress()))
                    return true;
            }
        }
        return false;
    }
    
}
