/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dmcd.scriptlauncher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author dan
 */
public class FileSaver extends Thread {
    /*
    * This class is handles saving network input to a temp file to allow a
    *  faster response on the TCP handshaking, which would otherwise cause the
    *  external program to slow down or have to buffer its output excessively
    *  when messages are generated very rapidly.
    */

    private static PrintWriter writer;
    private static BufferedReader reader;
    private static LinkedBlockingQueue<String> queue;

    public FileSaver(String fname, LinkedBlockingQueue<String> recvBuffer) {
        queue = recvBuffer;
        reader = null;
        writer = null;

        // setup the file to save the data in
        setFile(fname);
        NetComm.print("STATUS: FileSaver: started");
    }
    
    @Override
    public void run() {
        // wait for input and copy to file
        while(true) {
            if (writer != null && queue != null) {
                try {
                    // read next message from input buffer
                    String message = queue.take();

                    // append message to file
                    writer.write(message + System.getProperty("line.separator"));
                    writer.flush();
                } catch (InterruptedException ex) {
                    GuiMain.setErrorStatus ("FileSaver: InterruptedException");
                    NetComm.print("ERROR: FileSaver: " + ex.getMessage());
                }
            }
        }
    }

    public void exit() {
        if (writer != null) {
            NetComm.print("STATUS: FileSaver: closing");
            writer.close();
        }
    }
    
    public String getNextMessage() {
        String message = null;
        if (reader != null) {
            try {
                message = reader.readLine();
            } catch (IOException ex) {
                NetComm.print("ERROR: getNextMessage: " + ex.getMessage());
            }
        }

      return message;
    }

    public static void setFile(String fname) {
        // ignore if name was not changed
        if (fname == null || fname.isEmpty()) {
            return;
        }

        // remove any existing file
        File file = new File(fname);
        if (file.isFile()) {
            file.delete();
        }

        try {
            // close any current writer
            reader = null;
            if (writer != null) {
                NetComm.print("STATUS: FileSaver: closing writer");
                writer.close();
            }

            // set the buffer file to use for capturing input
            NetComm.print("STATUS: FileSaver: writing to " + fname);
            writer = new PrintWriter(new FileWriter(fname, true));

            // output time log started
            String message = "# Logfile started: " + LocalDateTime.now();
            writer.write(message + System.getProperty("line.separator"));
            writer.flush();

            // attach new file reader for output to gui
            reader = new BufferedReader(new FileReader(new File(fname)));
            NetComm.print("STATUS: FileSaver: reader status: " + reader.ready());

        } catch (IOException ex) {  // includes FileNotFoundException
            NetComm.print("ERROR: " + ex.getMessage());
        }
    }

  }  
