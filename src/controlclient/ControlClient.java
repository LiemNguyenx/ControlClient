/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controlclient;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Scrollbar;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author LiemNguyen
 */
public class ControlClient {
    public Socket socket = null;
    public JFrame frame = new JFrame();
    public JPanel cPanel = new JPanel(new GridLayout(0, 1));
    public JPanel screenPanel = new JPanel();
    public JPanel chatPanel = new JPanel();
    public JPanel controlPanel = new JPanel();
    
    
    public JTextArea chatArea = new JTextArea();
    public JScrollPane cScrol = new JScrollPane(chatArea);
    public JPanel areaChatPanel = new JPanel();

    public JTextField chatTextField = new JTextField();
    public JPanel textSendPanel = new JPanel();
    public JButton chatButton = new JButton("Gửi");
    public Vector cliens = new Vector();
    public String name = null;
    public static void main(String[] args) {
        String ip = JOptionPane.showInputDialog("Please enter server IP","localhost");
        String port = JOptionPane.showInputDialog("Please enter server port","1234");
        String name = JOptionPane.showInputDialog("Please enter client name","st");
        new ControlClient().initialize(ip, Integer.parseInt(port),name);
    }

    public void initialize(String ip, int port,String name) {
        this.name = name;
        Robot robot = null; //Used to capture the screen
        Rectangle rectangle = null; //Used to represent screen dimensions

        try {
            System.out.println("Connecting to server ..........");
            socket = new Socket(ip, port);
            System.out.println("Connection Established.");

            //Get default screen device
            GraphicsEnvironment gEnv=GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gDev=gEnv.getDefaultScreenDevice();

            //Get screen dimensions
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            rectangle = new Rectangle(dim);

            //Prepare Robot object
            robot = new Robot(gDev);

            //draw client gui
            drawGUI();
            //ScreenSpyer sends screenshots of the client screen
            new ScreenSpyer(socket,robot,rectangle);
            new chatClient(chatArea, chatButton, chatTextField,name);
            //ServerDelegate recieves server commands and execute them
            new ServerDelegate(socket,robot);
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (AWTException ex) {
                ex.printStackTrace();
        }
    }

    public void drawGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(ControlClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        cScrol.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        cScrol.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        cScrol.add(cPanel);

        cPanel.setBackground(Color.red);
        screenPanel.setBackground(Color.blue);
        //chatPanel.setBackground(Color.green);
        controlPanel.setBackground(Color.black);

        cPanel.setPreferredSize(new Dimension(200, 100));
        chatPanel.setPreferredSize(new Dimension(300, 100));
        controlPanel.setPreferredSize(new Dimension(200, 150));
        chatArea.setPreferredSize(new Dimension(300, 1000));
        chatArea.setEditable(false);
        chatTextField.setPreferredSize(new Dimension(245, 35));
        GridBagConstraints gbC1 = new GridBagConstraints();
//        GridBagConstraints gbC2 = new GridBagConstraints();
//        gbC1.gridx=0; gbC1.gridy=0; gbC1.gridwidth=3;
        //chatPanel.setLayout(new GridBagLayout());

        textSendPanel.setLayout(new GridBagLayout());
        textSendPanel.add(chatTextField);
        textSendPanel.add(chatButton);

        //chatPanel.add(areaChatPanel);
        chatPanel.add(textSendPanel);
        chatPanel.add(chatArea);

        frame.add(cScrol);
        frame.add(cPanel, BorderLayout.WEST);
        frame.add(screenPanel, BorderLayout.CENTER);
        frame.add(chatPanel, BorderLayout.EAST);
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Show the frame in a maximized state
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(1600, 800));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
