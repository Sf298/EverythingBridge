/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author saud
 */
public class SHTMLServerGUI {
    
    private JFrame frame;
    private JTextArea ta;
    private SHTMLServer server;
    private Thread t = null;
    public UserManagerV um;
    public ParamEditorV pe;
    //public PhilipsAPIV hue;
    
    public SHTMLServerGUI(SHTMLServer server, UserManagerV um, ParamEditorV pe/*, PhilipsAPIV hue*/) {
        this.server = server;
        this.um = um;
        this.pe = pe;
        //this.hue = hue;
        this.frame = new JFrame("Server "+Main.VERSION);
        
        JMenuBar menubar = new JMenuBar();
            JMenu file = new JMenu("Settings");
            
                JMenuItem usersMenuItem = new JMenuItem("Users");
                usersMenuItem.setToolTipText("Open user manager");
                usersMenuItem.addActionListener((ActionEvent event) -> {
                    um.show(frame);
                    System.out.println("Updated users");
                });
                file.add(usersMenuItem);
                
                JMenuItem paramsMenuItem = new JMenuItem("Change Params");
                paramsMenuItem.setToolTipText("Change website and server parameters");
                paramsMenuItem.addActionListener((ActionEvent event) -> {
                    pe.show(frame);
                    System.out.println("Updated parameters");
                });
                file.add(paramsMenuItem);
                
                JMenuItem hueMenuItem = new JMenuItem("Philips Hue Manager");
                hueMenuItem.setToolTipText("Change and setup connections with the Philips Hue Bridge");
                hueMenuItem.addActionListener((ActionEvent event) -> {
                    //hue.show(frame);
                    System.out.println("Updated parameters");
                });
                file.add(hueMenuItem);
                
                JMenuItem clearMenuItem = new JMenuItem("Clear Tokens");
                clearMenuItem.setToolTipText("Clear client tokens");
                clearMenuItem.addActionListener((ActionEvent event) -> {
                    server.clearTokens();
                    System.out.println("Tokens cleared");
                });
                file.add(clearMenuItem);
                
            menubar.add(file);
        frame.setJMenuBar(menubar);
        
        
        JPanel mainPanel = new JPanel(new BorderLayout());
            ta = new JTextArea("");
            ta.setEditable(false);
            ta.setFont(new Font("monospaced", Font.PLAIN, 12));
            mainPanel.add(new JScrollPane(ta), BorderLayout.CENTER);
        frame.add(mainPanel);
        
    }
    
    public void setIcon(String iconURL) {
        URL url = this.getClass().getResource(iconURL);
        //System.out.println("url = "+url);
        if(url != null) {
            frame.setIconImage(new ImageIcon(url).getImage());
        }
    }
    
    public void show() {
        printerThread(ta);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(750, 500);
        frame.setVisible(true);
        
        pe.show(frame);
        
        /*try {
        Thread.sleep(2000);
        } catch (InterruptedException ex) {
        Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        
        server.start();
    }
    
    public void interrupt() {
        if(t!=null) t.interrupt();
    }
    
    private void printerThread(JTextArea ta) {
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PipedOutputStream pOut = new PipedOutputStream();   
                    System.setOut(new PrintStream(pOut));   
                    PipedInputStream pIn = new PipedInputStream(pOut);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(pIn));

                    while(!Thread.interrupted()) {
                        try {
                            while(reader.ready()) {
                                String line = reader.readLine();
                                if(line != null) {
                                    ta.append(line);
                                    ta.append("\n");
                                }
                            }
                            Thread.sleep(200);
                        } catch (IOException | InterruptedException ex) {
                            Logger.getLogger(SHTMLServerGUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                } catch (IOException ex) {
                    Logger.getLogger(SHTMLServerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();
    }
    
}
