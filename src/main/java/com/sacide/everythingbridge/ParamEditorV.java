/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author saud
 */
public class ParamEditorV {
    
    private Properties params;
    private DefaultTableModel model;
    private JTable table;
    
    public static final String MOUSE_DPI = "Mouse dpi";
    public static final String SSL_KEY_FILE = "SSL Keystore File";
    public static final String SSL_KEY_ALIAS = "SSL Keystore Alias";
    public static final String SSL_KEY_STORE_PASS = "SSL Keystore pass";
    public static final String SSL_KEYPASS = "SSL Keypass";
    
    public ParamEditorV() {
        params = new Properties(new File("./params.prop"));
        params.put(MOUSE_DPI, "10");
        System.out.println(new File("./").getAbsolutePath());
        params.put(SSL_KEY_FILE, "D:\\Dropbox\\Java Projects\\EverythingBridge\\src\\main\\resources\\mykey.keystore");
        params.put(SSL_KEY_ALIAS, "alias");
        params.put(SSL_KEY_STORE_PASS, "123456");
        params.put(SSL_KEYPASS, "abcdef");
        params.load();
    }
    
    private String checkParamValue(String param, String value) {
        switch(param) {
            case MOUSE_DPI:
                if(value.matches("[0-9]+"))
                    return null;
                else
                    return "Error: Value must be a number";
            case SSL_KEY_FILE:
                if(new File(value).exists())
                    return null;
                else
                    return "Error: File not found";
            case SSL_KEY_ALIAS:
                    return null;
            case SSL_KEY_STORE_PASS:
                    return null;
            case SSL_KEYPASS:
                    return null;
            default:
                return "Error: param not found";
        }
    }
    
    private ArrayList<String> checkParamValues() {
        ArrayList<String> errors = new ArrayList<String>();
        for (Map.Entry<String, String> entry : params.getMap().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String ret = checkParamValue(key, value);
            if(ret == null) continue;
            errors.add(key+" => "+ret);
        }
        return errors;
    }
    
    public int getParamAsInt(String param) {
        return Integer.parseInt(params.get(param));
    }
    public String getParamAsString(String param) {
        return params.get(param);
    }
    
    public void show(JFrame parentFrame) {
        Properties oldMap = new Properties(params);
        ArrayList<String> errors = null;
        do {
            JPanel mainPanel = new JPanel(new BorderLayout());
                model = new DefaultTableModel(new Object[1][2], new Object[] {"Parameter", "Value"}) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                           return false;
                        }
                };
                table = new JTable(model);

                mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

                JPanel buttonPanel = new JPanel();
                    JButton editButton = new JButton("Edit Parameter");
                    editButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            editParam();
                            mapToTable();
                        }
                    });
                    buttonPanel.add(editButton);
                mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            mapToTable();
            final JComponent[] inputs = new JComponent[] {mainPanel};
            int option = JOptionPane.showConfirmDialog(parentFrame, inputs, "Parameter Manager", JOptionPane.PLAIN_MESSAGE);
            if(option == -1) {
                params = oldMap;
                return;
            }
            tableToMap();

            errors = checkParamValues();
            if(errors.isEmpty()) {
                params.save();
                return;
            }
            
            String errorMsg = "";
            for (String error : errors) {
                errorMsg += error + "\n";
            }
            JOptionPane.showMessageDialog(null, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
        } while(errors == null || !errors.isEmpty());
        
    }
    
    private void tableToMap() {
        params.clear();
        for (int row=0; row<model.getRowCount(); row++){
            String uname = model.getValueAt(row, 0).toString();
            String pword = model.getValueAt(row, 1).toString();
            params.put(uname, pword);
        }
    }
    
    private void mapToTable() {
        model.setRowCount(0);
        for (Map.Entry<String, String> en : params.getMap().entrySet()) {
             Object key = en.getKey();
             Object value = en.getValue();
            model.addRow(new Object[] {key, value});
        }
    }
    
    private void editParam() {
        if(table.getSelectedRow() == -1)
            return;
        
        String pname = (String) model.getValueAt(table.getSelectedRow(), 0);
        String oldVal = (String) model.getValueAt(table.getSelectedRow(), 1);
        
        JLabel pnameLabel = new JLabel(pname);
        JTextField valField = new JTextField();
        valField.setText(oldVal);
        
        final JComponent[] inputs = new JComponent[] {
                pnameLabel,
                valField
        };
        int result = JOptionPane.showConfirmDialog(null, inputs, "Edit Parameter", JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String errorMsg = checkParamValue(pname, valField.getText());
            if(errorMsg == null) {
                params.put(pname, valField.getText());
            } else {
                JOptionPane.showMessageDialog(null, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
}
