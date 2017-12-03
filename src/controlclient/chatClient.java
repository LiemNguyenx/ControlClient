package controlclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class chatClient extends Thread {

    JTextArea chataArea = null;
    JButton chatButton = null;
    JTextField text = null;
    public String name = null;
//    public static void main(String args[]) throws Exception {  
// 
//        // The default port     
//        int clientport = 7777;
//        String host = "localhost";
// 
//        if (args.length < 1) {
//           System.out.println("Usage: UDPClient " + "Now using host = " + host + ", Port# = " + clientport);
//        } 
//        // Get the port number to use from the command line
//        else {      
//           //host = args[0];
//           clientport = Integer.valueOf(args[0]).intValue();
//           System.out.println("Usage: UDPClient " + "Now using host = " + host + ", Port# = " + clientport);
//        } 
// 
//        // Get the IP address of the local machine - we will use this as the address to send the data to
//        InetAddress ia = InetAddress.getByName(host);
// 
//        SenderThread sender = new SenderThread(ia, clientport);
//        sender.start();
//        ReceiverThread receiver = new ReceiverThread(sender.getSocket());
//        receiver.start();
//    }

    public chatClient(JTextArea area, JButton button, JTextField text,String name) {
        this.chataArea = area;
        this.chatButton = button;
        this.text = text;
        this.name = name;
        start();
    }

    public void run() {
        try {
            int clientport = 7777;
            String host = "localhost";
            System.out.println("Usage: UDPClient " + "Now using host = " + host + ", Port# = " + clientport);
            InetAddress ia = InetAddress.getByName(host);

            SenderThread sender = new SenderThread(ia, clientport, chataArea, chatButton, text, name);
            sender.start();
            ReceiverThread receiver = new ReceiverThread(sender.getSocket(),chataArea);
            receiver.start();
        } catch (UnknownHostException | SocketException ex) {
            Logger.getLogger(chatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class SenderThread extends Thread {

    private InetAddress serverIPAddress;
    private DatagramSocket udpClientSocket;
    private boolean stopped = false;
    private int serverport;
    private JTextArea chataArea = null;
    private JButton chatButton = null;
    private JTextField text = null;
    private String name = null;
    public SenderThread(InetAddress address, int serverport, JTextArea area, JButton button, JTextField text,String name) throws SocketException {
        //Set GUI elements
        this.chataArea = area;
        this.chatButton = button;
        this.text = text;
        //Set address
        this.serverIPAddress = address;
        this.serverport = serverport;
        // Create client DatagramSocket
        this.udpClientSocket = new DatagramSocket();
        this.udpClientSocket.connect(serverIPAddress, serverport);
        this.name = name;
    }

    public void halt() {
        this.stopped = true;
    }

    public DatagramSocket getSocket() {
        return this.udpClientSocket;
    }

    public void run() {
        try {
            //send blank message
            byte[] data = new byte[1024];
            String firtMessage = name +" "+"is connected !!!!";
            data = firtMessage.getBytes();
            DatagramPacket blankPacket = new DatagramPacket(data, data.length, serverIPAddress, serverport);
            udpClientSocket.send(blankPacket);
            chatButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        byte[] sendData = new byte[1024];
                        String sendMessage = name +": "+ text.getText();
                        // Put this message into our empty buffer/array of bytes
                        sendData = sendMessage.getBytes();
                        
                        // Create a DatagramPacket with the data, IP address and port number
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIPAddress, serverport);
                        
                        // Send the UDP packet to server
                        System.out.println("I just sent: " + text.getText());
                        udpClientSocket.send(sendPacket);
                        chataArea.append("ME: "+text.getText()+"\n");
                        text.setText("");
                        Thread.yield();
                    } catch (IOException ex) {
                        Logger.getLogger(SenderThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            // Create input stream
//            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
//            while (true) {
//                if (stopped) {
//                    return;
//                }
//
//                // Message to send
//                String clientMessage = inFromUser.readLine();
//
//                if (clientMessage.equals(".")) {
//                    break;
//                }
//
//                // Create byte buffer to hold the message to send
//                byte[] sendData = new byte[1024];
//
//                // Put this message into our empty buffer/array of bytes
//                sendData = clientMessage.getBytes();
//
//                // Create a DatagramPacket with the data, IP address and port number
//                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIPAddress, serverport);
//
//                // Send the UDP packet to server
//                System.out.println("I just sent: " + clientMessage);
//                udpClientSocket.send(sendPacket);
//
//                Thread.yield();
//            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}

class ReceiverThread extends Thread {

    private DatagramSocket udpClientSocket;
    private boolean stopped = false;
    private JTextArea chatArea = null;
    public ReceiverThread(DatagramSocket ds, JTextArea area) throws SocketException {
        this.udpClientSocket = ds;
        this.chatArea = area;
    }

    public void halt() {
        this.stopped = true;
    }

    public void run() {

        // Create a byte buffer/array for the receive Datagram packet
        byte[] receiveData = new byte[1024];

        while (true) {
            if (stopped) {
                return;
            }

            // Set up a DatagramPacket to receive the data into
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            System.out.println("I am in the reader!");
            try {
                // Receive a packet from the server (blocks until the packets are received)
                udpClientSocket.receive(receivePacket);
                System.out.println("Am i receiving?");
                // Extract the reply from the DatagramPacket      
                String serverReply = new String(receivePacket.getData(), 0, receivePacket.getLength());
                chatArea.append(serverReply+"\n");
                // print to the screen
                System.out.println("UDPClient: Response from Server: \"" + serverReply + "\"\n");

                Thread.yield();
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }
}
