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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author saud
 */
public class UserManagerV {
    
    private Properties users;
    private DefaultTableModel model;
    private JTable table;
    private final String encryptionKey = Encryptor.genKey("npauvfnpfjlksmnvnpfd");
    
    public UserManagerV() {
        users = new Properties(new File("./users.prop"));
        users.load(encryptionKey);
    }
    
    public void show(JFrame parentFrame) {
        JPanel mainPanel = new JPanel(new BorderLayout());
            table = new JTable(
                    new DefaultTableModel(new Object[1][2], new Object[] {"uname", "pword"}) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                           return false;
                        }
                    }
            );

            model = (DefaultTableModel) table.getModel();
            mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
                JButton addButton = new JButton("Add User");
                addButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        addUser();
                        map2Table();
                    }
                });
                buttonPanel.add(addButton);

                JButton editButton = new JButton("Edit User");
                editButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        editUser();
                        map2Table();
                    }
                });
                buttonPanel.add(editButton);

                JButton removeButton = new JButton("Remove User");
                removeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        removeUser();
                        map2Table();
                    }
                });
                buttonPanel.add(removeButton);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        map2Table();
        final JComponent[] inputs = new JComponent[] {mainPanel};
        JOptionPane.showConfirmDialog(parentFrame, inputs, "User Manager", JOptionPane.PLAIN_MESSAGE);
        tableToMap();
        users.save(encryptionKey);
    }
    
    private void tableToMap() {
        users.clear();
        for (int row=0; row<model.getRowCount(); row++){
            String uname = model.getValueAt(row, 0).toString();
            String pword = model.getValueAt(row, 1).toString();
            users.put(uname, pword);
        }
    }
    
    private void map2Table() {
        model.setRowCount(0);
        for (Map.Entry<String, String> en : users.getMap().entrySet()) {
             Object key = en.getKey();
             Object value = en.getValue();
            model.addRow(new Object[] {key, value});
        }
    }
    
    public boolean checkPassword(String uname, String pword) {
        return users.hasKey(uname) && users.get(uname).equals(pword);
    }
    
    private void addUser() {
        JTextField uname = new JTextField();
        JTextField pword = new JTextField();
        final JComponent[] inputs = new JComponent[] {
                new JLabel("Username"),
                uname,
                new JLabel("Password"),
                pword
        };
        int result = JOptionPane.showConfirmDialog(null, inputs, "New User", JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            users.put(uname.getText(), pword.getText());
        }
    }
    
    private void editUser() {
        if(table.getSelectedRow() == -1)
            return;
        
        String oldName = (String) model.getValueAt(table.getSelectedRow(), 0);
        String oldPword = (String) model.getValueAt(table.getSelectedRow(), 1);
        
        JTextField uname = new JTextField();
        uname.setText(oldName);
        JTextField pword = new JTextField();
        pword.setText(oldPword);
        
        final JComponent[] inputs = new JComponent[] {
                new JLabel("Username"),
                uname,
                new JLabel("Password"),
                pword
        };
        int result = JOptionPane.showConfirmDialog(null, inputs, "Edit User", JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            users.remove(oldName);
            users.put(uname.getText(), pword.getText());
        }
    }
    
    private void removeUser() {
        if(table.getSelectedRow() == -1)
            return;
        
        String oldName = (String) model.getValueAt(table.getSelectedRow(), 0);
        users.remove(oldName);
    }
    
}
