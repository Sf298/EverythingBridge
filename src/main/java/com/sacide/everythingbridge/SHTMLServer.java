/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * @author saud
 */
public abstract class SHTMLServer {
    
    public final ArrayList<SHTMLServerThread> clients = new ArrayList<>();
    public final HashSet<Integer> tokens = new HashSet<>();
    
    private final int port;
    private final String keystoreFilename;
    private final String storepass;
    private final String keypass;
    
    private SSLServerSocket sslServerSocket;
    private Random r = new Random();
    
    public SHTMLServer(int port, String keystoreFilename, String storepass, String keypass) {
        this.port = port;
        this.keystoreFilename = keystoreFilename;
        this.storepass = storepass;
        this.keypass = keypass;
    }
    
    public abstract void handleMessage(SHTMLServerThread t, String  m);
    
    public void start() {
        try {
            System.out.println("starting...");
            SSLContext sslContext = this.createSSLContext();
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(this.port);
            
            System.out.println("Server can be accessed on address(es): \n"+getIPAdresses());
            System.out.println("Started server on port: "+port);
            System.out.println();
            
        } catch (IOException ex) {
            Logger.getLogger(SHTMLServer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Server initialisation failed. Is port already in use?");
        }
        
        while(true) {
            try {
                //System.out.println("waiting for connection...");
                SSLSocket s = (SSLSocket) sslServerSocket.accept();
                SHTMLServerThread st = new SHTMLServerThread(this, s, r.nextInt());
                st.setName("ST-Thread");
                clients.add(st);
                st.start();
                //System.out.println("connection "+st+" initiated");
            } catch (IOException ex) {
                Logger.getLogger(SHTMLServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private SSLContext createSSLContext() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(keystoreFilename),storepass.toCharArray());
            
            // Create key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keypass.toCharArray());
            KeyManager[] km = keyManagerFactory.getKeyManagers();
            
            // Create trust manager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();
            
            // Initialize SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(km, tm, null);
            
            return sslContext;
        } catch (IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException ex){
            Logger.getLogger(SHTMLServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public String getIPAdresses() {
        try {
            String out = "";
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while(nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while(ias.hasMoreElements()) {
                    InetAddress ia = ias.nextElement();
                    out += "\t" + ni.getDisplayName() + " : " + ia.getHostAddress() + "\n";
                }
            }
            return out.substring(0, out.length()-1);
        } catch (SocketException ex) {
            Logger.getLogger(SHTMLServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public int newToken() {
        int t;
        do {
            t = r.nextInt(Integer.MAX_VALUE);
        } while(tokens.contains(t));
        
        tokens.add(t);
        return t;
    }
    
    public boolean checkToken(int token) {
        return tokens.contains(token);
    }
    
    public void clearTokens() {
        clients.clear();
        tokens.clear();
    }
    
    public HashMap<String, String> parseArgs(String args) {
        if(args.contains("?")) {
            args = args.split("\\?")[1];
        }
        
        HashMap map = new HashMap();
        if(!args.contains("&")) {
            String[] temp = args.split("=");
            map.put(temp[0], temp[1]);
        } else {
            String[] temp1 = args.split("&");
            for(String arg : temp1) {
                String[] temp2 = arg.split("=");
                map.put(temp2[0], (temp2.length==2) ? temp2[1] : "");
            }
        }
        return map;
    }
    
}
