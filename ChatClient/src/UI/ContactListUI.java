package UI;

import javax.swing.*;
import java.awt.event.*;
import java.util.ListIterator;
import java.util.Vector;

import Core.*;

public class ContactListUI extends JFrame implements Runnable, Opcode
{
    private JComboBox cStatus;
    private JList contactList;
    private JScrollPane contactListPane;
    
    private JLabel lblName;
    private JLabel lblPSM;
    private JLabel lblFList;
    
    private DefaultListModel model;
    
    private String[] status = {"Online", "Away", "Busy", "Appear Offline", "Logout"};
    
    private String accountTitle;
    private String accountPSM;
    
    static Vector<ChatUI> chatWindow;
    
    public ContactListUI(String accountTitle, String accountPSM, JFrame loginFrame)
    {
        setTitle(String.format("%s - %s", accountTitle, accountPSM));
        setLayout(null);
        
        this.accountTitle = accountTitle;
        this.accountPSM = accountPSM;
        
        lblName = new JLabel(accountTitle);
        lblPSM = new JLabel(accountPSM);
        
        cStatus = new JComboBox(status);
        
        model = new DefaultListModel();
        contactList = new JList(model);
        contactListPane = new JScrollPane(contactList);
        
        add(lblName);
        add(lblPSM);
        add(cStatus);
        add(contactListPane);
        
        lblName.setBounds(15, 10, 245, 25);
        lblPSM.setBounds(15, 35, 245, 25);
        cStatus.setBounds(10, 65, 245, 25);
        contactListPane.setBounds(10, 100, 245, 360);
        
        
        setSize(270, 500);
        setResizable(false);
        setLocationRelativeTo(loginFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        loginFrame.dispose();
        
        chatWindow = new Vector<ChatUI>();
        
        addWindowListener(winListener);
        contactList.addMouseListener(mouseListener);
        cStatus.addActionListener(actListener);
    }
    
    public void exit()
    {
        Main.m_session.writeByte(CMSG_LOGOUT);
        Main.m_session.flush();
            
        //close all existing ChatUI
        for (ListIterator<ChatUI> i = chatWindow.listIterator(); i.hasNext(); )
        {
            ChatUI ui = i.next();
            ui.dispose();
        }
            
        chatWindow = null;
        
        System.exit(0);
    }
    
    // Direct adding Contact instance, Contact.java toString() method show the correct contact detail instead of instance memory location.
    public void run()
    {
        try
        {
            //TODO: Can we separate socket receive thread to another file?
            Main.m_session.writeByte(CMSG_GET_CONTACT_LIST);
            Main.m_session.flush();
            
            byte b;
            
            while((b = Main.m_session.readByte()) == SMSG_CONTACT_DETAIL)
            {
                int guid = Main.m_session.readInt();
                String cUsername = String.format("%s", Main.m_session.readObject());
                String cTitle = String.format("%s", Main.m_session.readObject());
                String cPSM = String.format("%s", Main.m_session.readObject());
                
                Contact c = new Contact(guid, cUsername, cTitle, cPSM);
                
                model.addElement(c);
            }
            
            if (b != SMSG_CONTACT_LIST_ENDED)
                JOptionPane.showMessageDialog(this, "Fail to load contact list, your contact list may incomplete.", "Error", JOptionPane.WARNING_MESSAGE);
            
            while(true)
            {
                switch (Main.m_session.readByte())
                {
                    case SMSG_SEND_CHAT_MESSAGE:
                        int sender = Main.m_session.readInt();
                        String message = String.format("%s", Main.m_session.readObject());
                        
                        Contact s_contact = null;
                        int index = 0;
                        
                        //TODO: Need optimize
                        for (index = 0; index < model.getSize(); index++)
                        {
                            if (((Contact)model.elementAt(index)).getGuid() == sender)
                            {
                                s_contact = (Contact)model.elementAt(index);
                                break;
                            }
                        }
                        
                        if (s_contact == null)
                            break;
                        
                        ChatUI targetUI = null;
                        
                        for (ListIterator<ChatUI> i = chatWindow.listIterator(); i.hasNext(); )
                        {
                            ChatUI ui = i.next();
                    
                            if (ui.getContact().equals(s_contact))
                            {
                                chatWindow.elementAt(index).toFront();
                                chatWindow.elementAt(index).repaint();
                                targetUI = chatWindow.elementAt(index);
                                break;
                            }
                        }
                        
                        if (targetUI == null)
                        {
                            targetUI = new ChatUI(s_contact, accountTitle);
                            chatWindow.add(targetUI);
                        }
                        
                        message = message.replaceAll("\n", "\n     ");
                        targetUI.txtOutput.append(String.format("%s says:\n", s_contact.getTitle()));
                        targetUI.txtOutput.append(String.format("     %s\n", message));
                        
                        break;
                }
            }
        }
        catch(Exception e){}
    }
    
    ActionListener actListener = new ActionListener() 
    {
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource().equals(cStatus))
            {
                switch (cStatus.getSelectedIndex())
                {
                    case 0:                /* Online */
                        break;
                    case 1:                /* Away */
                        break;
                    case 2:                /* Busy */
                        break;
                    case 3:                /* Appear Offline */
                        break;
                    case 4:                /* Logout */
                        exit();
                        break;
                }
            }
        }
    };
    
    WindowListener winListener = new WindowAdapter()
    {
        public void windowClosing(WindowEvent e) 
        {
            exit();
        }
    };
    
    MouseListener mouseListener = new MouseAdapter()
    {
        public void mouseClicked(MouseEvent e)
        {
            if (e.getClickCount() == 2)
            {
                int index = contactList.locationToIndex(e.getPoint());
                Contact c =(Contact)model.getElementAt(index);
                
                for (ListIterator<ChatUI> i = chatWindow.listIterator(); i.hasNext(); )
                {
                    ChatUI ui = i.next();
                    
                    if (ui.getContact().equals(c))
                    {
                        chatWindow.elementAt(index).toFront();
                        chatWindow.elementAt(index).repaint();
                        return;
                    }
                }
                
                chatWindow.add(new ChatUI(c, accountTitle));
            }
        }
    };
}