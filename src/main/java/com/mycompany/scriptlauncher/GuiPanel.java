/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.scriptlauncher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
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
        guiControls.makePanel (null, "PNL_CONNECT"    , "Connection"   , LEFT , false);
        guiControls.makePanel (null, "PNL_SCRIPT"     , "Script File"  , LEFT , true);
        guiControls.makePanel (null, "PNL_CONTROL"    , "Controls"     , LEFT , false);
        guiControls.makePanel (null, "PNL_CONTROL2"   , ""             , RIGHT, true);
        guiControls.makePanel (null, "PNL_STATUS"     , "Status"       , LEFT , true);
        guiControls.makePanel (null, "PNL_COMMAND"    , "Next Command" , LEFT , true);

        var statPanel = guiControls.getPanel ("PNL_STATUS");
        statPanel.setMinimumSize(new Dimension(1200, 40));
        var ctrlPanel = guiControls.getPanel ("PNL_COMMAND");
        ctrlPanel.setMinimumSize(new Dimension(1200, 40));

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
        String emptySpace = "                                                                                                                        ";
        guiControls.makeLabel (panel, "LBL_STATUS"   , "" , LEFT , false);
        guiControls.makeLabel (panel, ""             , emptySpace , RIGHT, true);
        JLabel textField = guiControls.getLabel("LBL_STATUS");
        textField.setMinimumSize(new Dimension(1200, 25));
        
        panel = "PNL_COMMAND";
        guiControls.makeLabel (panel, "LBL_COMMAND"  , "" , LEFT , false);
        guiControls.makeLabel (panel, ""             , emptySpace , RIGHT, true);
        textField = guiControls.getLabel("LBL_COMMAND");
        textField.setMinimumSize(new Dimension(1200, 25));
        
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
        setState ("STARTUP");
        
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
                clearStatusError();
            }
        });
        (guiControls.getButton("BTN_COMPILE")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendMessage("COMPILE");
                clearStatusError();
            }
        });
        (guiControls.getButton("BTN_RUN")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JButton runButton = guiControls.getButton("BTN_RUN");
                if (runButton.getText().equals("Run")) {
                    Variables.resetChanged();
                    runButton.setText("Stop");
                    sendMessage("RUN");
                } else {
                    runButton.setText("Run");
                    sendMessage("STOP");
                }
                clearStatusError();
            }
        });
        (guiControls.getButton("BTN_PAUSE")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JButton pauseButton = guiControls.getButton("BTN_PAUSE");
                if (pauseButton.getText().equals("Pause")) {
                    pauseButton.setText("Resume");
                    sendMessage("PAUSE");
                } else {
                    Variables.resetChanged();
                    pauseButton.setText("Pause");
                    sendMessage("RESUME");
                }
                clearStatusError();
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
                        setStatusError ("Invalid port selection: " + strPort);
                        return;
                    }
                    if (portConnection < 100 || portConnection > 65535) {
                        setStatusError ("Invalid port selection: " + strPort);
                        return;
                    }
                    setStatusError ("Waiting for connection on port " + portConnection + "...");
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
                clearStatusError();
            }
        });
        (guiControls.getButton("BTN_STEP")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Variables.resetChanged();
                sendMessage("STEP");
                clearStatusError();
            }
        });
        (guiControls.getButton("BTN_EXIT")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendMessage("EXIT");
                clearStatusError();
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
        Variables.init(varPane);

        // check for a properties file
        props = new PropertiesFile();
        String scrfileName = props.getPropertiesItem("ScriptFile", "");
        if (!scrfileName.isEmpty()) {
              fileSelector.setCurrentDirectory(new File(scrfileName));
        }
    }

    private static void formWindowClosing(java.awt.event.WindowEvent evt) {
        if (tcpClient != null)
        {
            try {
                tcpClient.exit();
            } catch (IOException exMsg) {
                // nothing to do.
            }
        }
        guiControls.close();
        System.exit(0);
    }

    public static boolean isDebugMsgTabSelected() {
        return tabPanel.getSelectedIndex() == 0;
    }
  
    public static boolean isScriptTabSelected() {
        return tabPanel.getSelectedIndex() == 1;
    }

    private static void enableButton (String buttonName) {
        JButton button = guiControls.getButton(buttonName);
        if (button == null) {
            setStatusError("Invalid label name: " + buttonName);
            return;
        }
        button.setEnabled(true);
    }

    private static void disableButton (String buttonName) {
        JButton button = guiControls.getButton(buttonName);
        if (button == null) {
            setStatusError("Invalid label name: " + buttonName);
            return;
        }
        button.setEnabled(false);
    }

    private static void setButtonText (String buttonName, String text) {
        if (text == null) {
            text = "";
        }
        JButton button = guiControls.getButton(buttonName);
        if (button == null) {
            setStatusError("Invalid label name: " + buttonName);
            return;
        }
        button.setText(text);
    }
    
    private static void setLabelText (String labelName, String text) {
        if (text == null) {
            text = "";
        }
        if (labelName == null) {
            setStatusError("Null label name");
            return;
        }
        JLabel label = guiControls.getLabel(labelName);
        if (label == null) {
            setStatusError("Invalid label name: " + labelName);
            return;
        }
        label.setText(text);
    }
    
    public static void clearStatusError () {
        JLabel textField = guiControls.getLabel("LBL_STATUS");
        if (textField != null) {
            textField.setText("");
        }
    }
    
    public static void setStatusError (String text) {
        JLabel textField = guiControls.getLabel("LBL_STATUS");
        if (textField != null) {
            textField.setForeground(Color.red);
            textField.setText(text);
        }
    }
    
    public static void clearCommandLine () {
        JLabel textField = guiControls.getLabel("LBL_COMMAND");
        if (textField != null) {
            textField.setText("");
        }
    }
    
    public static void setCommandLine (String text) {
        JLabel textField = guiControls.getLabel("LBL_COMMAND");
        if (textField != null) {
            textField.setForeground(Color.black);
            textField.setText(text);
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

        // check for special error cases
        if (state.startsWith("UNKNOWN: ")) {
            setStatusError("Invalid command sent to SERVER: " + state.substring(9));
            return;
        }
        if (state.startsWith("ERROR")) {
            setStatusError(state);
            state = "EOF";
        }
        
        JButton button;
        JLabel label;
        switch (state) {
            case "STARTUP":
                procState = ProcState.STARTUP;
                enableButton ("BTN_CONNECT");
                disableButton("BTN_LOAD");
                disableButton("BTN_COMPILE");
                disableButton("BTN_RUN");
                disableButton("BTN_PAUSE");
                disableButton("BTN_STEP");
                disableButton("BTN_BREAKPT");
                disableButton("BTN_EXIT");
                clearCommandLine();
                break;
            case "CONNECTED":
                connected = true;
                updateStateLabel (state);
                enableButton ("BTN_CONNECT");
                enableButton ("BTN_LOAD");
                disableButton("BTN_COMPILE");
                disableButton("BTN_RUN");
                disableButton("BTN_PAUSE");
                disableButton("BTN_STEP");
                disableButton("BTN_BREAKPT");
                enableButton ("BTN_EXIT");
                setButtonText("BTN_CONNECT", "Disconnect");
                setLabelText ("LBL_LOAD", "");
                clearStatusError ();
                clearCommandLine();
                break;
            case "DISCONNECTED":
                connected = false;
                updateStateLabel (state);
                enableButton ("BTN_CONNECT");
                disableButton("BTN_LOAD");
                disableButton("BTN_COMPILE");
                disableButton("BTN_RUN");
                disableButton("BTN_PAUSE");
                disableButton("BTN_STEP");
                disableButton("BTN_BREAKPT");
                disableButton("BTN_EXIT");
                setButtonText("BTN_CONNECT", "Connect");
                setLabelText ("LBL_LOAD", "");
                break;
            case "LOADED":
                procState = ProcState.LOADED;
                enableButton ("BTN_COMPILE");
                disableButton("BTN_RUN");
                disableButton("BTN_PAUSE");
                disableButton("BTN_STEP");
                disableButton("BTN_BREAKPT");
                updateStateLabel (state);
                clearCommandLine();
                break;
            case "COMPILED":
                procState = ProcState.COMPILED;
                updateStateLabel (state);
                enableButton("BTN_RUN");
                enableButton("BTN_STEP");
                enableButton("BTN_BREAKPT");
                setButtonText("BTN_RUN", "Run");
                clearCommandLine();
                break;
            case "EOF":
            case "STOPPED":
            case "STEPPED":
                procState = ProcState.COMPILED;
                updateStateLabel (state);
                Variables.print();
                enableButton ("BTN_RUN");
                enableButton ("BTN_STEP");
                disableButton("BTN_PAUSE");
                setButtonText("BTN_PAUSE", "Pause");
                setButtonText("BTN_RUN", "Run");
                // TODO: set highlighted line to line 1
                break;
            case "PAUSED":
                procState = ProcState.PAUSED;
                updateStateLabel (state);
                Variables.print();
                enableButton("BTN_RUN");
                enableButton("BTN_STEP");
                enableButton("BTN_PAUSE");
                setButtonText("BTN_PAUSE", "Resume");
                setButtonText("BTN_RUN", "Run");
                break;
            case "RESUMED":
                procState = ProcState.COMPILED;
                updateStateLabel (state);
                enableButton("BTN_RUN");
                enableButton("BTN_STEP");
                enableButton("BTN_PAUSE");
                setButtonText("BTN_PAUSE", "Pause");
                setButtonText("BTN_RUN", "Stop");
                break;
            default:
                setStatusError("Invalid STATUS command: " + state);
                break;
        }
    }
    
    public static void serverConnected() {
        setState ("CONNECTED");
    }

    public static void serverDisconnected() {
        setState ("DISCONNECTED");
    }

    private static void sendMessage (String message) {
        if (tcpClient == null || !connected) {
            setStatusError("Send to SERVER when Server not connected: " + message);
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
            // set the file to read from
            File file = fileSelector.getSelectedFile();
            Script.clear();
            Script.print(file);
            setLabelText("LBL_LOAD", file.getAbsolutePath());

            // send msg to AmazonReader to load the file
            sendMessage("LOAD " + file.getAbsolutePath());
            
            // update the directory selection in the Properties file
            props.setPropertiesItem("ScriptFile", file.getAbsolutePath());
            
            Logger.clear();
            Variables.clear();
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
//                basename = basename.substring(0, offset);
            }

            // remove any pre-existing file and save updated script
//            File scriptFile = new File(basename + ".scr");
//           scriptFile.delete();
//            Script.dataSave(graphFile);
        }
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
        // TODO: all System.out msgs should got to a new panel that shows activity
        
        switch (command) {
            case "STATUS:":
                setState(msgDisplay.substring(8));
                break;
            case "LINE:":
                if (words.size() < 2) {
                    setStatusError("Missing value for LINE message");
                    break;
                }
                String strValue = words.get(1);
                Integer lineNum = 1;
                try {
                    lineNum = Integer.valueOf(strValue);
                } catch (NumberFormatException ex) {
                    setStatusError("ERROR: Invalid Integer value for line: '" + strValue + "'");
                    break;
                }
                // TODO: highlight the line number
                String line = Script.getLine(lineNum);
                setCommandLine(line);
                break;
            case "LOGMSG:":
                // add the log info to the log screen
                if (msglen < 10) {
                    setStatusError("Invalid format for LOGMSG command");
                    break;
                }
                message = message.substring(8);
                Logger.print(message);
                break;
            case "ALLOC:":
                // extract the variable info and add to screen
                message = message.substring(7);
                Variables.allocationMessage(message);
                break;
            case "VARMSG:":
                // extract the variable info and modify displated entries in red
                message = message.substring(8);
                Variables.allocationMessage(message);
                break;
            default:
                System.out.println("GuiPanel.processMessage: Invalid command: " + message);
                break;
        }
    }

}
