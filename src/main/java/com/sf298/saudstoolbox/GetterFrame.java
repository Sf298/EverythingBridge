/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sf298.saudstoolbox;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Saud
 */
public class GetterFrame {
    
    private boolean isComplete = false;
    private boolean windowManuallyClosed = false;
    private JDialog frame;
    private JPanel contentJPanel;
    private GridBagConstraints c;
    
    /**
     * 
     * @param parentFrame The parent JFrame, can be null
     * @param contentPanel Allows the user to use custom panels
     * @param title Frame title
     */
    public GetterFrame(Window parentFrame, JPanel contentPanel, String title) {
        JPanel rootPanel = new JPanel(new BorderLayout());
        
        contentJPanel = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 1;
         c.gridx = 0;
         c.insets = new Insets(3, 20, 3, 20);
        if(contentPanel!=null)
            contentJPanel = contentPanel;
        
        rootPanel.add(contentJPanel, BorderLayout.CENTER);
        
        JButton doneButton = new JButton("Done");
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                frame.dispose();
                isComplete = true;
            }
        });
        rootPanel.add(doneButton, BorderLayout.SOUTH);
        
        
        frame = new JDialog(parentFrame, title, ModalityType.APPLICATION_MODAL);
        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {
                windowManuallyClosed = true;
                isComplete = true;
            }
            @Override
            public void windowClosed(WindowEvent e) {}
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
        
        frame.add(rootPanel, BorderLayout.CENTER);
        
    }
    /**
     * 
     * @param parentFrame The parent JFrame, can be null
     * @param title Frame title
     */
    public GetterFrame(Window parentFrame, String title) {
        this(parentFrame, null, title);
    }
    
    public JDialog getFrame() {
        return frame;
    }
    
    /**
     * Shows the dialog. Must call complete() afterwards.
     * @param width width of the dialog
     * @param height height of the dialog
     */
    public void showDialog(int width, int height) {
        frame.setSize(width, height);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
        frame.setVisible(true);
    }
    
    /**
     * Waits for the user to click 'done'.
     */
    public void complete() {
        while(!isComplete) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {
                //Logger.getLogger(TextGetter2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Shows the dialog then waits for the user to click 'done'. Do not  need to call complete().
     * @param width width of the dialog
     * @param height height of the dialog
     */
    public void showAndComplete(int width, int height) {
        showDialog(width, height);
        complete();
    }
    
    /**
     * Checks to see if the user canceled or closed the dialog
     * @return false if the user canceled or closed the dialog
     */
    public boolean isInputComplete() {
        return !windowManuallyClosed;
    }
    
    
    /**
     * add a plain JLabel tot the frame
     * @param text The text for the JLabel
     * @return the JLabel
     */
    public JLabel addLabel(String text) {
        JLabel field = new JLabel(text);
        contentJPanel.add(field, c);
        return (JLabel) field;
    }
    
    /**
     * initialises a plain text field
     * @param title The label to use, shows nothing if null
     * @param hintButton add a button to the right of the input field, excluded if null
     * @return [The component to add to the frame, the text field to get the text]
     */
    public static JComponent[] getTextField(String title, JButton hintButton) {
        JPanel out = new JPanel(new BorderLayout());
        
            if(title != null) {
                JLabel label = new JLabel(title + ": ");
                out.add(label, BorderLayout.WEST);
            }
            
            JTextField field = new JTextField();
            field.setMaximumSize(new Dimension(-1, 22));
            out.add(field, BorderLayout.CENTER);
            
            if(hintButton != null)
                out.add(hintButton, BorderLayout.EAST);
        
        return new JComponent[] {out, field};
    }
    /**
     * initialises a plain text field
     * @param title The label to use, shows nothing if null
     * @return [the text field to get the text]
     */
    public JTextField addTextField(String title) {
        return addTextField(title, null);
    }
    /**
     * initialises a plain text field
     * @param title The label to use, shows nothing if null
     * @param hintButton add a button to the right of the input field, excluded if null
     * @return [the text field to get the text]
     */
    public JTextField addTextField(String title, JButton hintButton) {
        JComponent[] components = getTextField(title, hintButton);
        contentJPanel.add(components[0], c);
        return (JTextField) components[1];
    }
    
    /**
     * initialises a plain text area
     * @param title The label to use, shows nothing if null
     * @param scrollable if the user can scroll around the textField
     * @param quickInsert add buttons to the top to quickly insert text
     * @return [The component to add to the frame, the text field to get the text]
     */
    public JComponent[] getTextArea(String title, String... quickInsert) {
        JPanel out = new JPanel(new BorderLayout());
            
            if(title != null) {
                JLabel label = new JLabel(title + ": ");
                out.add(label, BorderLayout.WEST);
            }
            
            JTextArea field = new JTextArea();
            field.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {onChange();}
                @Override
                public void removeUpdate(DocumentEvent e) {onChange();}
                public void onChange() {
                    field.setRows(field.getLineCount());
                }
                @Override
                public void changedUpdate(DocumentEvent e) {}
            });

            out.add(field, BorderLayout.CENTER);
            
            if(quickInsert!=null && quickInsert.length>0) {
                JPanel buttonPanel = new JPanel();
                for(int i=0; i<quickInsert.length; i++) {
                    JButton b = new JButton(quickInsert[i]);
                    b.setFont(new java.awt.Font("Arial", Font.BOLD, 10));
                    b.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            field.insert(b.getText(), field.getCaretPosition());
                        }
                    });
                    buttonPanel.add(b);
                }
                out.add(buttonPanel, BorderLayout.NORTH);
            }
        
        return new JComponent[] {out, field};
    }
    /**
     * initialises a plain text area
     * @param title The label to use, shows nothing if null
     * @param scrollable if the user can scroll around the textField
     * @param quickInsert add buttons to the top to quickly insert text
     * @return [the text field to get the text]
     */
    public JTextArea addTextArea(String title, boolean scrollable, String... quickInsert) {
        JComponent[] components = getTextArea(title, quickInsert);
        if(scrollable) {
            c.fill = GridBagConstraints.BOTH;
            c.weighty = 1;
            JScrollPane scroll = new JScrollPane(components[0]);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            contentJPanel.add(scroll, c);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weighty = 0;
        } else {
            contentJPanel.add(components[0], c);
        }
        return (JTextArea) components[1];
    }
    
    /**
     * initialises a password text field
     * @param title The label to use, shows nothing if null
     * @return [The component to add to the frame, the text field to get the text]
     */
    public static JComponent[] getHiddenTextField(String title) {
        JPanel out = new JPanel(new BorderLayout());
            
            if(title != null) {
                JLabel label = new JLabel(title + ": ");
                out.add(label, BorderLayout.WEST);
            }
            
            JTextField field = new JPasswordField();
            out.add(field, BorderLayout.CENTER);
        
        return new JComponent[] {out, field};
    }
    /**
     * initialises a password text field
     * @param title The label to use, shows nothing if null
     * @return [the text field to get the text]
     */
    public JTextField addHiddenTextField(String title) {
        JComponent[] components = getHiddenTextField(title);
        contentJPanel.add(components[0], c);
        return (JTextField) components[1];
    }
    
    /**
     * initialises a directory selection field
     * @param title The label to use, shows nothing if null
     * @param dialogTitle The title for the FileChooser when opened
     * @param defaultPath The directory to start in, use "" for default value
     * @return [the JTextField to get the text]
     */
    public static JComponent[] getDirectoryChooserField(String title, String dialogTitle, String defaultPath) {
        JButton button = new JButton("...");
        JComponent[] temp = getTextField(title, button);
        JTextField field = (JTextField) temp[1];
        
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new File(field.getText()));
                chooser.setDialogTitle(dialogTitle);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                  field.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        
        return temp;
    }
    /**
     * initialises a directory selection field
     * @param title The label to use, shows nothing if null
     * @param dialogTitle The title for the FileChooser when opened
     * @param defaultPath The directory to start in, use "" for default value
     * @return [the JTextField to get the text]
     */
    public JTextField addDirectoryChooserField(String title, String dialogTitle, String defaultPath) {
        JComponent[] components = getDirectoryChooserField(title, dialogTitle, defaultPath);
        contentJPanel.add(components[0], c);
        return (JTextField) components[1];
    }
    
    /**
     * initialises a ComboBox field
     * @param title The label to use, shows nothing if null
     * @param options the options to include in the combo box
     * @param editable flag to set if the ComboBox can be edited
     * @return [The component to add to the frame, the ComboBox field to get the text]
     */
    public static JComponent[] getComboField(String title, String[] options, boolean editable) {
        JPanel out = new JPanel(new BorderLayout());
        
            JLabel label = new JLabel(title + ": ");
            out.add(label, BorderLayout.WEST);
            
            JComboBox field = new JComboBox(options);
            field.setEditable(editable);
            field.setMaximumSize(new Dimension(-1, 22));
            out.add(field, BorderLayout.CENTER);
        
        return new JComponent[] {out, field};
    }
    /**
     * initialises a ComboBox field
     * @param title The label to use, shows nothing if null
     * @param options the options to include in the combo box
     * @param editable flag to set if the ComboBox can be edited
     * @return [the ComboBox field to get the text]
     */
    public JComboBox addComboField(String title, String[] options, boolean editable) {
        JComponent[] components = getComboField(title, options, editable);
        contentJPanel.add(components[0], c);
        return (JComboBox) components[1];
    }
    
    /**
     * initialises a ComboBox field
     * @param title The label to use
     * @return [The component to add to the frame, the ComboBox field to get the text]
     */
    public static JComponent[] getCheckBoxField(String title) {
        JPanel out = new JPanel(new BorderLayout());
        
            JLabel label = new JLabel(title + ": ");
            out.add(label, BorderLayout.WEST);
            
            JCheckBox field = new JCheckBox();
            out.add(field, BorderLayout.CENTER);
        
        return new JComponent[] {out, field};
    }
    /**
     * initialises a ComboBox field
     * @param title The label to use
     * @return [the ComboBox field to get the text]
     */
    public JCheckBox addCheckBoxField(String title) {
        JComponent[] components = getCheckBoxField(title);
        contentJPanel.add(components[0], c);
        return (JCheckBox) components[1];
    }
    
    /**
     * Adds a JButton. Also adds a listener that closes the GetterFrame.
     * @param b The button to add.
     */
    public void addButton(JButton b, boolean closeOnClick) {
        if(closeOnClick) {
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.setVisible(false);
                    frame.dispose();
                    isComplete = true;
                }
            });
        }
        contentJPanel.add(b, c);
    }
    
    /**
     * adds a component
     * @param component The component to add.
     */
    public void addComponent(JComponent component) {
        contentJPanel.add(component, c);
    }
    
    public void repaint() {
        frame.repaint();
        frame.validate();
    }
    
    /*private JPanel addBoolPropertyField(String title, String key) {
        JPanel out = new JPanel(new BorderLayout());
        
            JLabel label = new JLabel(title + ": ");
            out.add(label, BorderLayout.WEST);
            
            JCheckBox box = new JCheckBox();
            box.setSelected(Boolean.valueOf(si.getProp(key)));
            box.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    si.putProp(key, box.isSelected()+"");
                }
            });
            out.add(box, BorderLayout.CENTER);
        
        return out;
    }
    
    private JPanel addLabelField(String title, String text) {
        JPanel out = new JPanel(new BorderLayout());
        
            JLabel label = new JLabel(title + ": ");
            out.add(label, BorderLayout.WEST);
            
            JLabel valueLabel = new JLabel(text);
            out.add(valueLabel, BorderLayout.CENTER);
        
        return out;
    }*/
    
}
