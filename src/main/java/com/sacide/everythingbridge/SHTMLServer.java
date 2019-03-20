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
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * @author saud
 */
public abstract class SHTMLServer {
    
    private ServerSocket ss;
    private final int port;
    public final ArrayList<SHTMLServerThread> clients = new ArrayList<>();
    public final HashSet<Integer> tokens = new HashSet<>();
    private Random r = new Random();
    
    public SHTMLServer(int port, String keystoreFilename, String alias, String storepass, String keypass) {
        this.port = port;
        
        try {
            // load certificate
            FileInputStream fIn = new FileInputStream(keystoreFilename);
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(fIn, storepass.toCharArray());
            // display certificate
            /*Certificate cert = keystore.getCertificate(alias);
            System.out.println(cert);*/
            // setup the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keystore, keypass.toCharArray());
            // setup the trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keystore);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException ex) {
            Logger.getLogger(SHTMLServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // create https server
        server = HttpsServer.create(new InetSocketAddress(port), 0);
        // create ssl context
        SSLContext sslContext = SSLContext.getInstance(protocol);
        // setup the HTTPS context and parameters
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            public void configure(HttpsParameters params) {
                try {
                    // initialise the SSL context
                    SSLContext c = SSLContext.getDefault();
                    SSLEngine engine = c.createSSLEngine();
                    params.setNeedClientAuth(false);
                    params.setCipherSuites(engine.getEnabledCipherSuites());
                    params.setProtocols(engine.getEnabledProtocols());
                    // get the default parameters
                    SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                    params.setSSLParameters(defaultSSLParameters);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Failed to create HTTPS server");
                }
            }
        });
    }
    
    public abstract void handleMessage(SHTMLServerThread t, String  m);
    
    public void start() {
        try {
            System.out.println("starting...");
            ss = new ServerSocket(port);
            
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
                Socket s = ss.accept();
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
            t = r.nextInt();
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
