/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.scriptlauncher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author dmcd2356
 */
public class GuiPanel {
    /*
    * This class handles creating and updating the Graphical User Interface.
    */

    private static final String CLASS_NAME = GuiPanel.class.getSimpleName();
    
    private final static GuiControls  guiControls = new GuiControls();
    private static PropertiesFile props;
    private static JTabbedPane    tabPanel;
    private static JTextPane      debugPane;
    private static JTextPane      scriptPane;
    private static JTextPane      varPane;
    private static JFileChooser   fileSelector;
    private static Timer          pktTimer;
    private static Timer          scriptTimer;
    private static TCPClient      tcpClient;
    private static boolean        connected;
    private static ProcState      procState;
    private static int            portConnection;

    private static enum ProcState {
        STARTUP,
        LOADED,
        COMPILED,
        RUNNING,
        PAUSED,
    }
    
    /**
     * creates a debug panel to display the Logger messages in.
     * 
     * @param panelName - name of the panel to display
     * @param port      - the TCP port to use for reading messages
     */  
      public void createDebugPanel(String panelName, int port) {
        // if a panel already exists, close the old one
        if (guiControls.isValidFrame()) {
            guiControls.close();
        }

        String portInfo = "TCP port " + port;
        connected = false;
        portConnection = port;

        // these just make the gui entries cleaner
        String panel;
        GuiControls.Orient LEFT = GuiControls.Orient.LEFT;
        GuiControls.Orient CENTER = GuiControls.Orient.CENTER;
        GuiControls.Orient RIGHT = GuiControls.Orient.RIGHT;

        // create the frame
        guiControls.newFrame(panelName, 1200, 600, false);

        // create the entries in the main frame
        guiControls.makePanel (null, "PNL_CONNECT"    , "Connection"  , LEFT , false);
        guiControls.makePanel (null, "PNL_SCRIPT"     , "Script File" , LEFT , true);
        guiControls.makePanel (null, "PNL_CONTROL"    , "Controls"    , LEFT , false);
        guiControls.makePanel (null, "PNL_CONTROL2"   , ""            , RIGHT, true);
        guiControls.makePanel (null, "PNL_STATUS"     , "Status"      , LEFT , true);
        var statPanel = guiControls.getPanel ("PNL_STATUS");
        statPanel.setMinimumSize(new Dimension(1200, 40));

        panel = "PNL_CONNECT";
        guiControls.makeButton   (panel, "BTN_CONNECT", "Connect"     , LEFT, false);
        guiControls.makeTextField(panel, "TXT_PORT"   , "", LEFT, false, 50, Integer.toString(port), true);
        guiControls.makeTextField(panel, "TXT_STATE"  , "", LEFT, true , 100, "", false);

        panel = "PNL_SCRIPT";
        guiControls.makeButton(panel, "BTN_LOAD"      , "Load"        , LEFT, false);
        guiControls.makeLabel (panel, "LBL_LOAD"      , ""            , LEFT, true);
        tabPanel = guiControls.makeTabbedPanel(null, "PNL_TABBED", "" , LEFT, true);

        panel = "PNL_CONTROL";
        guiControls.makeButton(panel, "BTN_COMPILE"  , "Compile"  , LEFT , false);
        guiControls.makeButton(panel, "BTN_RUN"      , "Run"      , LEFT , false);
        guiControls.makeButton(panel, "BTN_PAUSE"    , "Pause"    , LEFT , false);
        guiControls.makeButton(panel, "BTN_STEP"     , "Step"     , LEFT , false);
        guiControls.makeLabel (panel, ""             , "        " , LEFT , false); // dummy
        guiControls.makeButton(panel, "BTN_BREAKPT"  , "Breakpt"  , LEFT , true);
//        guiControls.makeButton(panel, "BTN_CLEAR"    , "Clear"  , LEFT, true);

        panel = "PNL_CONTROL2";
        guiControls.makeButton(panel, "BTN_EXIT"     , "Terminate", RIGHT, true);

        panel = "PNL_STATUS";
        guiControls.makeTextField(panel, "TXT_STATUS", "", LEFT, true, 1100, "", false);
        
        // add the Script panel to the tabs
        JScrollPane fileScrollPanel;
        scriptPane = new JTextPane();
        fileScrollPanel = new JScrollPane(scriptPane);
        fileScrollPanel.setBorder(BorderFactory.createTitledBorder(""));
        tabPanel.addTab("Script", fileScrollPanel);

        // add the Variables panel to the tabs
        varPane = new JTextPane();
        fileScrollPanel = new JScrollPane(varPane);
        fileScrollPanel.setBorder(BorderFactory.createTitledBorder(""));
        tabPanel.addTab("Variables", fileScrollPanel);

        // add the Debug message panel to the tabs
        debugPane = new JTextPane();
        fileScrollPanel = new JScrollPane(debugPane);
        fileScrollPanel.setBorder(BorderFactory.createTitledBorder(""));
        tabPanel.addTab("Debug Messages", fileScrollPanel);

        // we need a filechooser for the Save buttons
        fileSelector = new JFileChooser();

        // disable the buttons that require a connection
        procState = ProcState.STARTUP;
        setState (null);
        
        // setup the control actions
        guiControls.getFrame().addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        (guiControls.getButton("BTN_LOAD")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadScriptButtonActionPerformed(evt);
            }
        });
        (guiControls.getButton("BTN_COMPILE")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableUpdateTimers(false);
                sendMessage("COMPILE");
            }
        });
        (guiControls.getButton("BTN_RUN")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JButton runButton = guiControls.getButton("BTN_RUN");
                if (runButton.getText().equals("Run")) {
                    enableUpdateTimers(false);
                    runButton.setText("Stop");
                    sendMessage("RUN");
                } else {
                    enableUpdateTimers(true);
                    runButton.setText("Run");
                    sendMessage("STOP");
                }
            }
        });
        (guiControls.getButton("BTN_PAUSE")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JButton pauseButton = guiControls.getButton("BTN_PAUSE");
                if (pauseButton.getText().equals("Pause")) {
                    enableUpdateTimers(false);
                    pauseButton.setText("Resume");
                    sendMessage("PAUSE");
                } else {
                    enableUpdateTimers(true);
                    pauseButton.setText("Pause");
                    sendMessage("RESUME");
                }
            }
        });
        (guiControls.getButton("BTN_CONNECT")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JButton pauseButton = guiControls.getButton("BTN_CONNECT");
                if (pauseButton.getText().equals("Connect")) {
                    JTextField portField = guiControls.getTextField ("TXT_PORT");
                    String strPort = portField.getText();
                    try {
                        portConnection = Integer.parseInt(strPort);
                    } catch (NumberFormatException exMsg) {
                        setStatusText (true, "Invalid port selection: " + strPort);
                        return;
                    }
                    if (portConnection < 100 || portConnection > 65535) {
                        setStatusText (true, "Invalid port selection: " + strPort);
                        return;
                    }
                    setStatusText (true, "Waiting for connection on port " + portConnection + "...");
                    guiControls.update();

                    // start the TCP listener thread
                    try {
                        tcpClient = new TCPClient(portConnection);
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                        System.exit(1);
                    }
                    pauseButton.setText("Disconnect");
                } else {
                    pauseButton.setText("Connect");
                }
            }
        });
        (guiControls.getButton("BTN_STEP")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableUpdateTimers(false);
                sendMessage("STEP");
            }
        });
        (guiControls.getButton("BTN_EXIT")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableUpdateTimers(false);
                sendMessage("EXIT");
            }
        });
//        (guiControls.getButton("BTN_CLEAR")).addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                resetCapturedInput();
//            }
//        });

        // display the frame
        guiControls.display();

        // now init the debug message handler and the script file handler
        Logger.init(debugPane);
        Script.init(scriptPane);

        // check for a properties file
        props = new PropertiesFile();
        String scrfileName = props.getPropertiesItem("ScriptFile", "");
        if (!scrfileName.isEmpty()) {
              fileSelector.setCurrentDirectory(new File(scrfileName));
        }
    }

    public static boolean isDebugMsgTabSelected() {
        return tabPanel.getSelectedIndex() == 0;
    }
  
    public static boolean isScriptTabSelected() {
        return tabPanel.getSelectedIndex() == 1;
    }

    private static void enableButton (boolean status, String buttonName) {
        JButton button = guiControls.getButton(buttonName);
        button.setEnabled(status);
    }
    
    private static void setLabelText (String labelName, String text) {
        if (labelName == null) {
            // TODO: display error
            return;
        }
        JLabel label = guiControls.getLabel(labelName);
        if (label == null) {
            // TODO: display label name not found error
            return;
        }
        label.setText(text);
    }
    
    public static void setStatusText (boolean error, String text) {
        JTextField textField = guiControls.getTextField("TXT_STATUS");
        if (textField != null) {
            if (text == null || text.isEmpty()) {
                textField.setForeground(Color.black);
                textField.setText("");
            } else if (error) {
                textField.setForeground(Color.red);
                textField.setText(text);
            } else {
                textField.setForeground(Color.black);
                textField.setText(text);
            }
        }
    }
    
    private static void updateStateLabel (String text) {
        // update display
        if (! text.isEmpty()) {
            JTextField textField = guiControls.getTextField("TXT_STATE");
            if (textField != null) {
                textField.setForeground(Color.black);
                textField.setText(text);
            }
        }
    }
    
    private static void setState (String state) {
        if (state == null || state.isBlank()) {
            return;
        }
        
        JButton button;
        if (state == null) {
            switch (procState) {
                case STARTUP:
                    enableButton(true , "BTN_CONNECT");
                    enableButton(false, "BTN_LOAD");
                    enableButton(false, "BTN_COMPILE");
                    enableButton(false, "BTN_RUN");
                    enableButton(false, "BTN_PAUSE");
                    enableButton(false, "BTN_STEP");
                    enableButton(false, "BTN_BREAKPT");
                    enableButton(false, "BTN_EXIT");
                    break;
                default:
                    break;
            }
            return;
        }
        switch (state) {
            case "CONNECTED":
                connected = true;
                updateStateLabel (state);
                enableButton(true , "BTN_CONNECT");
                enableButton(true , "BTN_LOAD");
                enableButton(false, "BTN_COMPILE");
                enableButton(false, "BTN_RUN");
                enableButton(false, "BTN_PAUSE");
                enableButton(false, "BTN_STEP");
                enableButton(false, "BTN_BREAKPT");
                enableButton(true , "BTN_EXIT");
                button = guiControls.getButton("BTN_CONNECT");
                button.setText("Connect");
                setStatusText (false, null);
                break;
            case "DISCONNECTED":
                connected = false;
                updateStateLabel (state);
                enableButton(true , "BTN_CONNECT");
                enableButton(false, "BTN_LOAD");
                enableButton(false, "BTN_COMPILE");
                enableButton(false, "BTN_RUN");
                enableButton(false, "BTN_PAUSE");
                enableButton(false, "BTN_STEP");
                enableButton(false, "BTN_BREAKPT");
                enableButton(false, "BTN_EXIT");
                button = guiControls.getButton("BTN_CONNECT");
                button.setText("Connect");
                break;
            case "LOADED":
                procState = ProcState.LOADED;
                enableButton(true , "BTN_COMPILE");
                updateStateLabel (state);
                break;
            case "COMPILED":
                procState = ProcState.COMPILED;
                updateStateLabel (state);
                enableButton(true , "BTN_RUN");
                enableButton(true , "BTN_STEP");
                enableButton(true , "BTN_BREAKPT");
                button = guiControls.getButton("BTN_RUN");
                button.setText("Run");
                break;
            case "EOF":
            case "STEPPED":
            case "RESUMED":
                procState = ProcState.COMPILED;
                updateStateLabel (state);
                enableButton(true , "BTN_RUN");
                enableButton(true , "BTN_STEP");
                enableButton(false, "BTN_PAUSE");
                button = guiControls.getButton("BTN_PAUSE");
                button.setText("Pause");
                button = guiControls.getButton("BTN_RUN");
                button.setText("Run");
                // TODO: set highlighted line to line 1
                enableUpdateTimers(false);
                break;
            case "PAUSED":
            case "STOPPED":
                procState = ProcState.PAUSED;
                updateStateLabel (state);
                button = guiControls.getButton("BTN_PAUSE");
                button.setText("Resume");
                button = guiControls.getButton("BTN_RUN");
                button.setText("Run");
                break;
            default:
                setStatusText(true, "Invalid STATUS command: " + state);
                break;
        }
    }
    
    public static void serverConnected() {
        setState ("CONNECTED");
    }

    public static void serverDisconnected() {
        setState ("DISCONNECTED");
    }

    private static void enableUpdateTimers(boolean enable) {
        if (pktTimer != null) {
            if (enable) {
                pktTimer.start();
            } else {
                pktTimer.stop();
            }
        }
        if (scriptTimer != null) {
            if (enable) {
                scriptTimer.start();
            } else {
                scriptTimer.stop();
            }
        }
    }
  
    private static void sendMessage (String message) {
        if (tcpClient == null || !connected) {
            setStatusText(true, "Send to SERVER when Server not connected: " + message);
        } else {
            System.out.println("Send to SERVER: " + message);
            tcpClient.sendMessage(message);
        }
    }
    
    private static void loadScriptButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String defaultName = "program";
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Script Files", "scr");
        fileSelector.setFileFilter(filter);
        fileSelector.setSelectedFile(new File(defaultName + ".scr"));
        fileSelector.setMultiSelectionEnabled(false);
        fileSelector.setApproveButtonText("Load");
        int retVal = fileSelector.showOpenDialog(guiControls.getFrame());
        if (retVal == JFileChooser.APPROVE_OPTION) {
            // stop the timers from updating the display
            enableUpdateTimers(false);

            // set the file to read from
            File file = fileSelector.getSelectedFile();
            Script.print(file);
            setLabelText("LBL_LOAD", file.getAbsolutePath());

            // send msg to AmazonReader to load the file
            sendMessage("LOAD " + file.getAbsolutePath());
            
            // update the directory selection in the Properties file
            props.setPropertiesItem("ScriptFile", file.getAbsolutePath());
            
            // now restart the update timers
            enableUpdateTimers(true);
        }
    }
  
    private static void saveScriptButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String defaultName = "program";
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Script Files", "scr");
        fileSelector.setFileFilter(filter);
        fileSelector.setApproveButtonText("Save");
        fileSelector.setMultiSelectionEnabled(false);
        fileSelector.setSelectedFile(new File(defaultName + ".scr"));
        int retVal = fileSelector.showOpenDialog(guiControls.getFrame());
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileSelector.getSelectedFile();
            // get the base name without extension so we can create matching json and png files
            String basename = file.getAbsolutePath();
            int offset = basename.lastIndexOf('.');
            if (offset > 0) {
                basename = basename.substring(0, offset);
            }

            // remove any pre-existing file and save updated script
//            File scriptFile = new File(basename + ".scr");
//           scriptFile.delete();
//            Script.dataSave(graphFile);
        }
    }
  
    private static void formWindowClosing(java.awt.event.WindowEvent evt) {
        if (scriptTimer != null) {
            scriptTimer.stop();
        }
        if (pktTimer != null) {
            pktTimer.stop();
        }
//        tcpThread.exit();
        guiControls.close();
        System.exit(0);
    }

    private static void resetCapturedInput() {
        // clear the packet buffer and statistics
//        tcpThread.clear();

        // clear the text panel
        Logger.clear();
    }

    public static void processMessage(String message) {
        // seperate message into the message type and the message content
        if (message == null || message.isBlank()) {
            return;
        }
        ArrayList<String> words = new ArrayList<>(Arrays.asList(message.split(" ")));
        String command = words.getFirst();
        int msglen = message.length();

        String msgDisplay = message;
        if (msgDisplay.length() > 120) {
            msgDisplay = msgDisplay.substring(0, 120) + "...";
        }
        System.out.println("SERVER: " + message);
        setStatusText(false, "SERVER: " + msgDisplay);
        
        switch (command) {
            case "STATUS:":
                setState(words.get(1));
                break;
            case "LINE:":
                if (words.size() < 2) {
                    setStatusText(true, "Missing value for LINE message");
                    break;
                }
                String strValue = words.get(1);
                try {
                    Integer intValue = Integer.valueOf(strValue);
                } catch (NumberFormatException ex) {
                    setStatusText(true, "Invalid Integer value: " + strValue);
                    break;
                }
                // TODO: highlight the line number
                break;
            case "LOGMSG:":
                // add the log info to the log screen
                if (msglen < 10) {
                    setStatusText(true, "Invalid format for LOGMSG command");
                    break;
                }
                message = message.substring(7);
                Logger.print(message);
                break;
            case "ALLOC:":
                // extract the variable info and add to screen
                // TODO:
                break;
            case "VARMSG:":
                // extract the variable info and add to screen
                // TODO:
                break;
            default:
                setStatusText(true, "Invalid command: " + command);
                break;
        }
    }

}
