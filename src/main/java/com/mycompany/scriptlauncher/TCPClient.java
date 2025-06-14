/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.scriptlauncher;

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
    *  that is running of the network. It has a function for sending messages
    *  as well as a seperate thread that runs continuously looking for input from
    *  the server and sending it to an external function to process the data.
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
        System.out.println("Checking for TCP connection on port " + tcpPort + "...");
        connectThread.start();
//        while (true) {
//            try {
//                socket = new Socket("localhost", tcpPort);
//                break;
//            } catch (IOException exMsg) {
//                try {
//                    TimeUnit.SECONDS.sleep(5);
//                } catch (InterruptedException ex) {
//                    System.exit(0);
//                }
//            }
//        }
//        System.out.println("Successfully connected to the TCP server on port " + tcpPort);
//        GuiPanel.serverConnected();
//
//        // Set up input and output streams for communication
//        in_socket  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        out_socket = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
//
//        // start the listener thread
//        runThread.start();
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
            System.out.println("Successfully connected to the TCP server on port " + tcpPort);
            GuiPanel.serverConnected();

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
            String message;
            try {
                while ((message = in_socket.readLine()) != null) {
                    GuiPanel.processMessage(message);
                }
                System.out.println("Port " + tcpPort + " has been closed.");
                GuiPanel.serverDisconnected();
            } catch (IOException exMsg) {
                exMsg.printStackTrace();
            }
        }
    });

    public void exit () throws IOException {
        // Close the connection
        socket.close();
        socket = null;
        System.out.println("Port " + tcpPort + " closed successfully.");
    }

}
