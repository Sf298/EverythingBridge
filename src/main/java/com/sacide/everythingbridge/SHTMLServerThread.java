/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author saud
 */
public class SHTMLServerThread extends Thread {
    
    private SHTMLServer server;
    private PrintWriter pw = null;
    private BufferedReader br = null;

    SHTMLServerThread(SHTMLServer server, Socket s, int webToken) {
        this.server = server;
        try {
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            pw = new PrintWriter(s.getOutputStream());
            
        } catch (IOException ex) {
            Logger.getLogger(SHTMLServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run() {
        try {
            String line = br.readLine();
            server.handleMessage(this, line);
            
            br.close();
            pw.close();
        } catch (SocketException ex) {
            Logger.getLogger(SHTMLServerThread.class.getName()).log(Level.SEVERE, null, ex);
            /*try {
                Runtime.getRuntime().exec("java -jar "+Main.JAR_NAME);
                System.exit(0);
            } catch (IOException ex1) {
                Logger.getLogger(SHTMLServerThread.class.getName()).log(Level.SEVERE, null, ex1);
            }*/
        }catch (IOException ex) {
            Logger.getLogger(SHTMLServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendHTML(boolean auth, String body) {
        sendHTML(auth, "text/html", body);
    }
    
    public void sendHTML(boolean auth, String type, String body) {
        pw.println("HTTP/1.1 200 OK");
        pw.println("Content-Type: "+type);
        pw.println("\r\n");
        if(auth)
            pw.println(body);
        else
            pw.println("<p> login error </p>");
        pw.flush();
    }
    
}
