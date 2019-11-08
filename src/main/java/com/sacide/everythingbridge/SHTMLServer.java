/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ServerSocketFactory;
import net.freeutils.httpserver.HTTPServer;

/**
 *
 * @author saud
 */
public class SHTMLServer {
    
    private final int port;
    private final String keystoreFilename;
    private final String storepass;
    private final String keypass;
    private final HTTPServer server;
    private final HTTPServer.VirtualHost vhost;
    
    public SHTMLServer(int port, String keystoreFilename, String storepass, String keypass) {
        this.port = port;
        this.keystoreFilename = keystoreFilename;
        this.storepass = storepass;
        this.keypass = keypass;
        
        this.server = new HTTPServer(port);
        this.vhost = server.getVirtualHost(null);
    }
    
    public void start() {
        try {
            System.out.println("starting...");
            
            server.setServerSocketFactory(getServerSocketFactory());
            server.start();
            
            System.out.println("Server can be accessed on address(es): \n"+getIPAdresses());
            System.out.println("Started server on port: "+port);
            System.out.println();
            
        } catch (IOException | CertificateException | UnrecoverableKeyException ex) {
            Logger.getLogger(SHTMLServer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Server initialisation failed. Is port already in use?");
        }
    }
    
    private ServerSocketFactory getServerSocketFactory() throws FileNotFoundException, IOException, CertificateException, UnrecoverableKeyException {
        try {
            char[] password = storepass.toCharArray();
            KeyStore ks = KeyStore.getInstance("jks");
            ks.load(new FileInputStream(keystoreFilename), password);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);
            return sslContext.getServerSocketFactory();
        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException ex) {
            Logger.getLogger(SHTMLServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void addContext(String path, HTTPServer.ContextHandler handler, String... methods) {
        vhost.addContext(path, handler, methods);
    }
    
    public static String getIPAdresses() {
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
    
}
