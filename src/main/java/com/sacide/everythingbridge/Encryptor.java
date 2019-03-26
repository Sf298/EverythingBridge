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
        long longSeed = bytes2long(hash(seed+"oiufhsou").getBytes());
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
    
    public static String hash(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
            return new String(encodedhash);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Encryptor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
