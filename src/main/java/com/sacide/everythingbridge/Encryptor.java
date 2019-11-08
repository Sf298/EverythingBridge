/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author demon
 */
public class Encryptor {
    
    public static String genKey(String seed) {
        long longSeed = bytes2long(hashSHA256(seed, "oiufhsou").getBytes());
        Random r = new Random(longSeed);
        int len = r.nextInt(17)+17;
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<len; i++) {
            int temp = r.nextInt(27*2+10);
            char c;
            if(temp < 27)
                c = (char)(temp+'a');
            else if(temp < 27*2)
                c = (char)((temp-27)+'A');
            else
                c = (char)((temp-27*2)+'0');
            sb.append(c);
        }
        return sb.toString();
    }
    
    public static long bytes2long(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        for(int i=0; i<Math.min(Long.BYTES, bytes.length); i++) {
            buffer.put(bytes[i]);
        }
        buffer.flip();
        return buffer.getLong();
    }
    
    public static String hashSHA256(String str, String... salts) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
			String out = str;
			for(String salt : salts) {
				out += salt;
				out = bytesToHex(digest.digest(out.getBytes(StandardCharsets.UTF_8)));
			}
			return out;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Encryptor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private static String bytesToHex(byte[] hash) {
	StringBuilder hexString = new StringBuilder();
	for (int i = 0; i < hash.length; i++) {
	    String hex = Integer.toHexString(0xff & hash[i]);
	    if(hex.length() == 1) hexString.append('0');
	    hexString.append(hex);
	}
	return hexString.toString();
    }
    
    public static String encrypt(String str, String key) {
        int inKStep = (key.length()%2 == 0) ? 3 : 4;
        StringBuilder out = new StringBuilder();
        for(int i=0; i<str.length(); i++) {
            int inC = str.charAt(i);
            int inK = key.charAt((i+inKStep)%key.length());
            int val = (i*3+1) % key.length() / 2;
            
            int cypher = (inC + inK + val) % 128;
            out.append((char)cypher);
        }
        //System.out.println("encrypted: "+Arrays.toString(toIntArr(out.toString())));
        return out.toString();
    }
    
    public static String decrypt(String str, String key) {
        int inKStep = (key.length()%2 == 0) ? 3 : 4;
        StringBuilder out = new StringBuilder();
        for(int i=0; i<str.length(); i++) {
            int inC = str.charAt(i);
            int inK = key.charAt((i+inKStep)%key.length());
            int val = (i*3+1) % key.length() / 2;
            
            int decryped = (128 + inC - inK - val) % 128;
            out.append((char)decryped);
        }
        //System.out.println("decrypted: "+Arrays.toString(toIntArr(out.toString())));
        return out.toString();
    }
    
    private static int[] toIntArr(String str) {
        int[] out = new int[str.length()];
        for(int i=0; i<str.length(); i++) {
            out[i] = str.charAt(i);
        }
        return out;
    }
    
}
