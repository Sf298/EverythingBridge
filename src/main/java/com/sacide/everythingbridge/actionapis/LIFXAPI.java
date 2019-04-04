/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge.actionapis;

import com.sf298.saudstoolbox.PacketBuilder;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author saud
 */
public class LIFXAPI {
    
    private static DatagramSocket socket;
    private static final byte[] BROADCAST_ADDRESS = str2ip("255.255.255.255");
    private static final int PORT = 56700;
    
    private static ArrayList<Device> devices;
    
    public static void init() {
        try {
            socket = new DatagramSocket(PORT);
        } catch (SocketException ex) {
            Logger.getLogger(LIFXAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void turnOnLight(String ipAddress, boolean turnOn, int durationMs) {
        byte[] fh = getFrameHeader((short)(HEADER_SIZE_BYTES+(16+32)/8), false, 0);
        byte[] fah = getFrameAddressHeader(null, false, false, (byte)0);
        byte[] ph = getProtocolHeader((short)117);
        PacketBuilder payload = new PacketBuilder(false, true);
        payload.addField(16, turnOn?65535:0);
        payload.addField(32, durationMs);
        byte[] pay = payload.compileBits().toByteArray();
        byte[] out = concatArrays(fh,fah,ph,pay);
        System.out.println(Arrays.toString(out));
        
        PacketBuilder temp = new PacketBuilder(payload);
        temp.loadPacketData(pay);
        temp.swapBytePairs(1);
        System.out.println(temp.getFieldsString());
        
        //sendPacket(ipAddress, PORT, out);
        /*ArrayList<DatagramPacket> responses = receiveAll(HEADER_SIZE_BYTES+(8+32)/8, 5000);
        System.out.println("a "+responses.size());*/
    }
    
    public static void discoverDevices(int timeoutMs) {
        byte[] fh = getFrameHeader(HEADER_SIZE_BYTES, true, 123);
        byte[] fah = getFrameAddressHeader(null, false, true, (byte)100);
        byte[] ph = getProtocolHeader((short)2);
        byte[] out = concatArrays(fh,fah,ph);
        
        sendPacket(BROADCAST_ADDRESS, PORT, out);
        ArrayList<DatagramPacket> responses = receiveAll(HEADER_SIZE_BYTES+(8+32)/8, timeoutMs);
        
        System.out.println("responses len = "+responses.size());
        for(DatagramPacket response : responses) {
            PacketBuilder resp = getBlankHeader();
            resp.addField("service", 8, 0);
            resp.addField("port",   32, 0);
            resp.loadPacketData(response.getData());
            
            System.out.println("target = "+Long.toHexString(resp.getFieldValue("target")));
            devices = new ArrayList<>();
            devices.add(new Device(
                    response.getAddress().getHostAddress(),
                    (int)resp.getFieldValue("port")
            ));
            
            System.out.println(resp.getFieldsString());
        }
    }
    
    private static final byte HEADER_SIZE_BYTES = 36;
    private static PacketBuilder getBlankHeader() {
        PacketBuilder out = new PacketBuilder(true, false);
        out.addField("size",      16, 0);
        out.addField("origin",     2, 0);
        out.addField("tagged",     1, 0);
        out.addField("addressable",1, 0);
        out.addField("protocol",  12, 0);
        out.addField("source",    32, 0);
        
        out.addField("target",   64, 0);
        out.addField("reserved", 48, 0);
        out.addField("reserved", 6,  0);
        out.addField("ack",      1,  0);
        out.addField("res",      1,  0);
        out.addField("sequence", 8,  0);
        
        out.addField("reserved", 64,  0);
        out.addField("type",     16,  0);
        out.addField("reserved", 16,  0);
        return out;
    }
    // 8 bytes
    private static byte[] getFrameHeader(short size, boolean tagged, int source) {
        PacketBuilder out = new PacketBuilder(true, false);
        out.addField("size", 16, size); // size, size of whole message in bytes
        out.addField("origin", 2,  0); // origin, leave 0
        out.addField("tagged", 1,  tagged?1:0); // tagged, 1 if msg is GetService else 0
        out.addField("addressable", 1,  1); // addressable, indicates that the next header will be a frame address header. must be 1
        out.addField("protocol", 12, 1024); // protocol, must be 1024
        out.addField("source", 32, source); // source, unique value set by the client, used by responses
        return out.compileBits().toByteArray();
    }
    // 16 bytes
    private static byte[] getFrameAddressHeader(String mac, boolean ack, boolean res, byte sequence) {
        long MAC = 0;
        if(mac!=null) {
            mac = mac.toUpperCase().replaceAll("[^0-9A-F]", "");
            while(mac.length()<16) mac += "0";
            if(mac.startsWith("F")) {
                mac = "E"+mac.substring(1);
                MAC = new BigInteger(mac, 16).longValue();
            }
        }
        
        PacketBuilder out = new PacketBuilder(true, false);
        out.addField("target", 64, MAC); // target, dest address or 0 for broadcast
        out.addField("reserved", 48, 0);   // reserved
        out.addField("reserved", 6,  0);   // reserved
        out.addField("ack", 1,  ack?1:0); // ack_required, if an acknowledgement message should be sent
        out.addField("res", 1,  res?1:0); // res_required, if a response message should be sent
        out.addField("sequence", 8,  sequence);  // sequence, identifier when source is the same
        return out.compileBits().toByteArray();
    }
    // 12 bytes
    private static byte[] getProtocolHeader(short type) {
        PacketBuilder out = new PacketBuilder(true, false);
        out.addField("reserved", 64, 0); // reserved
        out.addField("type", 16, type);  // type, the type of request
        out.addField("reserved", 16, 0); // reserved
        return out.compileBits().toByteArray();
    }
    
    private static ArrayList<DatagramPacket> broadcast(byte[] msg, int receiveLen, int timeoutMs) {
        try {
            socket.setSoTimeout(timeoutMs);
            ArrayList<DatagramPacket> responses = new ArrayList<>();
            try {
                DatagramPacket packet = new DatagramPacket(msg, msg.length, InetAddress.getByAddress(BROADCAST_ADDRESS), PORT);
                socket.send(packet);
                while(true) {
                    DatagramPacket temp = new DatagramPacket(new byte[receiveLen], receiveLen);
                    socket.receive(temp);
                    if(!isLocalIP(temp.getAddress()))
                        responses.add(temp);
                }
            } catch (SocketTimeoutException  ex) {
                System.out.println("timeout");
            } catch (IOException ex) {
                Logger.getLogger(LIFXAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
            return responses;
        } catch (SocketException ex) {
            Logger.getLogger(LIFXAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private static void sendPacket(String address, int port, byte[] msg) {
        sendPacket(str2ip(address), port, msg);
    }
    private static void sendPacket(byte[] address, int port, byte[] msg) {
        try {
            DatagramPacket packet = new DatagramPacket(
                    msg, msg.length,
                    InetAddress.getByAddress(address),
                    port);
            socket.send(packet);
        } catch (SocketTimeoutException  ex) {
            System.out.println("timeout");
        } catch (IOException ex) {
            Logger.getLogger(LIFXAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static ArrayList<DatagramPacket> receiveAll(int receiveLen, int timeoutMs) {
        ArrayList<DatagramPacket> responses = new ArrayList<>();
        try {
            while(true) {
                DatagramPacket temp = receive(receiveLen, timeoutMs);
                if(temp != null) responses.add(temp);
            }
        } catch (SocketTimeoutException  ex) {
            System.out.println("timeout");
        }
        return responses;
    }
    private static DatagramPacket receive(int receiveLen, int timeoutMs) throws SocketTimeoutException {
        try {
            socket.setSoTimeout(timeoutMs);
            DatagramPacket temp = new DatagramPacket(new byte[receiveLen], receiveLen);
            socket.receive(temp);
            if(!isLocalIP(temp.getAddress()))
                return temp;
        } catch (SocketTimeoutException  ex) {
            throw ex;
        } catch (IOException ex) {
            Logger.getLogger(LIFXAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
    
    private static byte[] concatArrays(byte[]... arrs) {
        ArrayList<Byte> temp = new ArrayList<>();
        for(int i=0; i<arrs.length; i++) {
            for(int j=0; j<arrs[i].length; j++) {
                temp.add(arrs[i][j]);
            }
        }
        
        byte[] out = new byte[temp.size()];
        for(int i=0; i<out.length; i++)
            out[i] = temp.get(i);
        return out;
    }
    
    private static byte[] str2ip(String address) {
        address = address.replace("/", "");
        String[] arr = address.split("\\.");
        if(arr.length != 4 && arr.length != 6)
            throw new RuntimeException("Error parsing IP");
        
        byte[] out = new byte[arr.length];
        for(int i=0; i<arr.length; i++) {
            int temp = Integer.parseInt(arr[i]);
            out[i] = (byte) temp;
        }
        return out;
    }
    
    private static void printByte(byte in) {
        System.out.println();
        for(int i=0; i<Byte.SIZE; i++) {
            System.out.print(in & 1);
            in = (byte) (in >>> 1);
        }
        System.out.println();
    }
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
    static public byte[] convertBinaryStringToLittleEndianByteArray(String binValueAsString) {
		if((binValueAsString.length() % 8) == 0) {
			int arrayLength = binValueAsString.length() / 8;
			byte[] byteArray = new byte[arrayLength];
			long binaryToLong = Long.parseLong(binValueAsString, 2);
			ByteBuffer byteBuffer = ByteBuffer.allocate(8);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			byteBuffer.putLong(binaryToLong);
			
			for(int i=0; i<arrayLength; i++){
				byteArray[i] = byteBuffer.array()[i];	
			}
			
			return byteArray;
		}
		else {
			System.out.println("Error: Binary number does not fit into an even number of bytes");
			return null;
		}
	}
    
    private static class Device {
        public String ip;
        public int port;
        public Device(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
    }
}
