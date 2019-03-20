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
    
    public ParamEditorV() {
        params = new Properties(new File("./params.prop"));
        params.put(MOUSE_DPI, "10");
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
            default:
                return "Error";
        }
    }
    
    public int getParamAsInt(String param) {
        return Integer.parseInt(params.get(param));
    }
    public String getParamAsString(String param) {
        return params.get(param);
    }
    
    public void show(JFrame parentFrame) {
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
                    JButton editButton = new JButton("Edit User");
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
        JOptionPane.showConfirmDialog(parentFrame, inputs, "User Manager", JOptionPane.PLAIN_MESSAGE);
        tableToMap();
        params.save();
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
