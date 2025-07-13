/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.scriptlauncher;

/**
 *
 * @author dan
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class TCPClient {
    /*
    * This class creates a TCP Client for sending and receiving data to a server
    *  that is running on the network. 
    * It runs in a seperate thread for waiting for a connection to be established,
    *  so the caller returns immediately rather than being hung waiting for the
    *  connection to be established.
    * It also runs a continuous loop for checking for input from the established
    *  connection and forwards those messages on to an external parser for handling
    *  the messages.
    * There is also a function included for sending messages to the connection.
    */

    private int tcpPort;
    private Socket socket = null;
    private PrintWriter out_socket = null;
    private BufferedReader in_socket = null;
    
    TCPClient (int port) throws IOException {

        if (port < 100 || port > 65535) {
            System.err.println("Port " + port + " is invalid");
            System.exit(1);
        }
        
        // Connect to the server at localhost on specified port
        tcpPort = port;
        PropertiesFile.setPropertiesItem("Port", Integer.toUnsignedString(port));
        NetComm.print("STATUS: Checking for TCP connection on port " + tcpPort + "...");
        connectThread.start();
    }
    
    public void sendMessage (String message) {
        if (out_socket != null) {
            out_socket.println(message);
        }
    }

    Thread connectThread = new Thread(new Runnable() {
        @Override
        public void run() {
            String message;
            while (true) {
                try {
                    socket = new Socket("localhost", tcpPort);
                    break;
                } catch (IOException exMsg) {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException ex) {
                        System.exit(0);
                    }
                }
            }
            NetComm.print("STATUS: Successfully connected to the TCP server on port " + tcpPort);
            GuiMain.serverConnected();

            // Set up input and output streams for communication
            try {
                in_socket  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out_socket = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            } catch (IOException exMsg) {
                exMsg.printStackTrace();
            }

            // start the listener thread
            runThread.start();
        }
    });

    Thread runThread = new Thread(new Runnable() {
        @Override
        public void run() {
            String message = "";
            try {
                while (message != null && socket != null && !socket.isClosed()) {
                    message = in_socket.readLine();
                    GuiMain.processMessage(message);
                }
                NetComm.print("STATUS: Port " + tcpPort + " has been closed.");
                GuiMain.serverDisconnected();
            } catch (IOException exMsg) {
                if (! socket.isClosed()) {
                    exMsg.printStackTrace();
                }
            }
        }
    });

    public void exit () throws IOException {
        // Close the connection
        socket.close();
        //socket = null;
        NetComm.print("STATUS: Port " + tcpPort + " closed successfully.");
    }

}
