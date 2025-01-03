
package tkm.ui;

import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * @file ChatPanel.java
 * @date 10/19/2024
 * @author Mike
 * 
 * This is a JPanel that implements a chat box, with an input area and a send button.
 */

public class ChatPanel extends JPanel{
    
    private JTextArea chatArea;
    private JScrollPane chatScrollPane;
    private JTextField chatInput;
    private JButton send;
    
    public ChatPanel() {
        this.initComponents();
        this.setLayout(new GridLayout(0, 1, 5, 5));
        this.add(chatScrollPane);
        
        JPanel sendPanel = new JPanel();
        
        sendPanel.add(chatInput);
        sendPanel.add(send);
        this.add(sendPanel);
    }
    
    // initialize panel components
    private void initComponents() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);        // So users cant edit the chatbox
        chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setBorder(BorderFactory.createTitledBorder("Chat"));
        chatInput = new JTextField(25);
        send = new JButton("Send");
    }
    
    /**
     * 
     * Mainly so App can access these fields, Maybe make protected 
     */
    public JTextField getChatInput() {
        return chatInput;
    }
    
    public JTextArea getChatArea() {
        return chatArea;
    }
    
    public JButton getSendButton() {
        return send;
    }
    
} // end ChatPanel