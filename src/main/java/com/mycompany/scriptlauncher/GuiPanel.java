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

    // the chars used to seperate entries in reporting variable contents to the client
    public static final String DATA_SEP = "::";
    
    private final static GuiControls  guiControls = new GuiControls();
    private static PropertiesFile props;
    private static JTabbedPane    tabPanel;
    private static JTextPane      debugPane;
    private static JTextPane      scriptPane;
    private static JTextPane      varPane;
    private static JTextPane      outputPane;
    private static JTextPane      networkPane;
    private static JFileChooser   fileSelector;
    private static TCPClient      tcpClient;
    private static boolean        connected;
    private static ProcState      procState;
    private static int            portConnection;
    private static ArrayList<String> panelId = new ArrayList<>();

    private static enum ProcState {
        STARTUP,
        LOADED,
        COMPILED,
        RUNNING,
        PAUSED,
    }
    
    private static enum TabSelect {
        SCRIPT,
        VARIABLES,
        OUTPUT,
        LOGGER,
        NETCOMM,
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
        JLabel textField;
        GuiControls.Orient LEFT = GuiControls.Orient.LEFT;
        GuiControls.Orient CENTER = GuiControls.Orient.CENTER;
        GuiControls.Orient RIGHT = GuiControls.Orient.RIGHT;

        // create the frame
        guiControls.newFrame(panelName, 1200, 900, false);

        // create the entries in the main frame
        guiControls.makePanel (null, "PNL_CONNECT"    , "Connection"   , LEFT  , false);
        guiControls.makePanel (null, "PNL_SCRIPT"     , "Script File"  , LEFT  , true);
        guiControls.makePanel (null, "PNL_CONTROL"    , "Controls"     , LEFT  , false);
        guiControls.makePanel (null, "PNL_BREAKPT"    , "Breakpoints"  , CENTER, false);
        guiControls.makePanel (null, "PNL_TERMINATE"  , ""             , RIGHT , true);
        guiControls.makePanel (null, "PNL_ERROR"      , "Error msgs"   , LEFT  , true);
        guiControls.makePanel (null, "PNL_SUBSTACK"   , "Sub stack"    , LEFT  , true);
        guiControls.makePanel (null, "PNL_COMMAND"    , "Next Command" , LEFT  , true);

        var statPanel = guiControls.getPanel ("PNL_ERROR");
        statPanel.setMinimumSize(new Dimension(1200, 40));
        var ctrlPanel = guiControls.getPanel ("PNL_COMMAND");
        ctrlPanel.setMinimumSize(new Dimension(1200, 40));

        panel = "PNL_CONNECT";
        guiControls.makeButton   (panel, "BTN_CONNECT", "Connect"     , LEFT, false);
        guiControls.makeTextField(panel, "TXT_PORT"   , "", LEFT, false, 50, Integer.toString(port), true);
        guiControls.makeTextField(panel, "TXT_STATE"  , "", LEFT, true , 160, "", false);

        panel = "PNL_SCRIPT";
        guiControls.makeButton(panel, "BTN_LOAD"      , "Load"        , LEFT, false);
        guiControls.makeLabel (panel, "LBL_LOAD"      , ""            , LEFT, true);
        tabPanel = guiControls.makeTabbedPanel(null, "PNL_TABBED", "" , LEFT, true);

        panel = "PNL_CONTROL";
        guiControls.makeButton(panel, "BTN_COMPILE"  , "Compile"  , LEFT , false);
        guiControls.makeButton(panel, "BTN_RUN"      , "Run"      , LEFT , false);
        guiControls.makeButton(panel, "BTN_PAUSE"    , "Pause"    , LEFT , false);
        guiControls.makeButton(panel, "BTN_STEP"     , "Step"     , LEFT , true);

        panel = "PNL_BREAKPT";
        guiControls.makeButton(panel, "BTN_BREAKPT"  , "Enable"   , LEFT , false);
        guiControls.makeTextField(panel, "TXT_BREAKPT", ""        , LEFT , false, 30, "", true);
        guiControls.makeLabel (panel, "LBL_BREAKPT"  , "OFF"      , LEFT , true);

        panel = "PNL_TERMINATE";
        guiControls.makeButton(panel, "BTN_CLEAR"    , "Clear",     RIGHT, false);
        guiControls.makeButton(panel, "BTN_EXIT"     , "Terminate", RIGHT, true);

        panel = "PNL_ERROR";
        String emptySpace = "                                                                                                                        ";
        guiControls.makeLabel (panel, "LBL_ERROR"    , "" , LEFT , false);
        guiControls.makeLabel (panel, ""             , emptySpace , RIGHT, true);
        textField = guiControls.getLabel("LBL_ERROR");
        textField.setMinimumSize(new Dimension(1200, 25));
        
        panel = "PNL_SUBSTACK";
        guiControls.makeLabel (panel, "LBL_SUBSTACK" , "" , LEFT , false);
        guiControls.makeLabel (panel, ""             , emptySpace , RIGHT, true);
        
        panel = "PNL_COMMAND";
        guiControls.makeLabel (panel, "LBL_COMMAND"  , "" , LEFT , false);
        guiControls.makeLabel (panel, ""             , emptySpace , RIGHT, true);
        textField = guiControls.getLabel("LBL_COMMAND");
        textField.setMinimumSize(new Dimension(1200, 25));
        
        // add the Script panel to the tabs
        JScrollPane fileScrollPanel, scriptScrollPanel, varScrollPanel;
        String title = "Script";
        scriptPane = new JTextPane();
        fileScrollPanel = new JScrollPane(scriptPane);
        fileScrollPanel.setBorder(BorderFactory.createTitledBorder(""));
        tabPanel.addTab(title, fileScrollPanel);
        panelId.add(title);
        scriptScrollPanel = fileScrollPanel; // save for later

        // add the Variables panel to the tabs
        title = "Variables";
        varPane = new JTextPane();
        fileScrollPanel = new JScrollPane(varPane);
        fileScrollPanel.setBorder(BorderFactory.createTitledBorder(""));
        tabPanel.addTab(title, fileScrollPanel);
        panelId.add(title);
        varScrollPanel = fileScrollPanel; // save for later

        // add the Network Communication message panel to the tabs
        title = "User Output";
        outputPane = new JTextPane();
        fileScrollPanel = new JScrollPane(outputPane);
        fileScrollPanel.setBorder(BorderFactory.createTitledBorder(""));
        tabPanel.addTab(title, fileScrollPanel);
        panelId.add(title);

        // add the Debug message panel to the tabs
        title = "Debug log";
        debugPane = new JTextPane();
        fileScrollPanel = new JScrollPane(debugPane);
        fileScrollPanel.setBorder(BorderFactory.createTitledBorder(""));
        tabPanel.addTab(title, fileScrollPanel);
        panelId.add(title);

        // add the Network Communication message panel to the tabs
        title = "Network Comm";
        networkPane = new JTextPane();
        fileScrollPanel = new JScrollPane(networkPane);
        fileScrollPanel.setBorder(BorderFactory.createTitledBorder(""));
        tabPanel.addTab(title, fileScrollPanel);
        panelId.add(title);

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
                NetComm.print("STATUS: LOAD button pressed");
                loadScriptButtonActionPerformed(evt);
                clearErrorStatus();
            }
        });
        (guiControls.getButton("BTN_COMPILE")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NetComm.print("STATUS: COMPILE button pressed");
                sendMessage("COMPILE");
                clearErrorStatus();
            }
        });
        (guiControls.getButton("BTN_RUN")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JButton runButton = guiControls.getButton("BTN_RUN");
                String curButton = runButton.getText();
                switch (curButton) {
                    case "Run":
                        NetComm.print("STATUS: RUN button pressed");
                        Variables.resetChanged();
                        runButton.setText("Stop");
                        updateStateLabel ("RUNNING...");
                        procState = ProcState.RUNNING;
                        sendMessage("RUN");
                        break;
                    case "Stop":
                        NetComm.print("STATUS: STOP button pressed");
                        runButton.setText("Reset");
                        buttonInProcess();  // wait for stop to be acknowledged
                        setButtonText("BTN_PAUSE", "Pause");
                        sendMessage("STOP");
                        break;
                    case "Reset":
                        NetComm.print("STATUS: RESET button pressed");
                        runButton.setText("Run");
                        JButton stepButton = guiControls.getButton("BTN_STEP");
                        stepButton.setEnabled(true);
                        Variables.resetVariables();
                        sendMessage("RESET");
                        break;
                    default:
                        break;
                }
                clearErrorStatus();
            }
        });
        (guiControls.getButton("BTN_PAUSE")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JButton pauseButton = guiControls.getButton("BTN_PAUSE");
                if (pauseButton.getText().equals("Pause")) {
                    NetComm.print("STATUS: PAUSE button pressed");
                    pauseButton.setText("Resume");
                    buttonInProcess();  // wait for pause to be acknowledged
                    sendMessage("PAUSE");
                } else {
                    NetComm.print("STATUS: RESUME button pressed");
                    Variables.resetChanged();
                    pauseButton.setText("Pause");
                    sendMessage("RESUME");
                }
                clearErrorStatus();
            }
        });
        (guiControls.getButton("BTN_CONNECT")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JButton pauseButton = guiControls.getButton("BTN_CONNECT");
                if (pauseButton.getText().equals("Connect")) {
                    NetComm.print("STATUS: CONNECT button pressed");
                    JTextField portField = guiControls.getTextField ("TXT_PORT");
                    String strPort = portField.getText();
                    try {
                        portConnection = Integer.parseInt(strPort);
                    } catch (NumberFormatException exMsg) {
                        setErrorStatus ("Invalid port selection: " + strPort);
                        return;
                    }
                    if (portConnection < 100 || portConnection > 65535) {
                        setErrorStatus ("Invalid port selection: " + strPort);
                        return;
                    }
                    updateStateLabel ("Waiting for connection");
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
                    NetComm.print("STATUS: DISCONNECT button pressed");
                    sendMessage("DISCONNECT");
                    pauseButton.setText("Connect");
                }
            }
        });
        (guiControls.getButton("BTN_STEP")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NetComm.print("STATUS: STEP button pressed");
                Variables.resetChanged();
                sendMessage("STEP");
                clearErrorStatus();
            }
        });
        (guiControls.getButton("BTN_BREAKPT")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JButton breakButton = guiControls.getButton("BTN_BREAKPT");
                if (breakButton.getText().equals("Enable")) {
                    breakpointSet();
                } else {
                    breakpointUnset();
                }
                clearErrorStatus();
            }
        });
        (guiControls.getButton("BTN_CLEAR")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (isTabSelected(TabSelect.OUTPUT)) {
                    Output.clear();
                } else if (isTabSelected(TabSelect.LOGGER)) {
                    Logger.clear();
                } else if (isTabSelected(TabSelect.NETCOMM)) {
                    NetComm.clear();
                }
            }
        });
        (guiControls.getButton("BTN_EXIT")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NetComm.print("STATUS: EXIT button pressed");
                sendMessage("EXIT");
                clearErrorStatus();
            }
        });

        // display the frame
        guiControls.display();

        // now init the debug message handler and the script file handler
        Logger.init(debugPane);
        NetComm.init(networkPane);
        Output.init(outputPane);
        Script.init(scriptPane, scriptScrollPanel);
        Variables.init(varPane, varScrollPanel);

        // check for a properties file
        props = new PropertiesFile();
        String scrfileName = props.getPropertiesItem("ScriptFile", "");
        if (!scrfileName.isEmpty()) {
              fileSelector.setCurrentDirectory(new File(scrfileName));
        }
    }

    private static void formWindowClosing(java.awt.event.WindowEvent evt) {
        if (tcpClient != null && connected)
        {
            sendMessage("DISCONNECT");
            try {
                tcpClient.exit();
            } catch (IOException exMsg) {
                // nothing to do.
            }
        }
        guiControls.close();
        System.exit(0);
    }

    private static void sendMessage (String message) {
        if (tcpClient == null || !connected) {
            setErrorStatus("Send to SERVER when Server not connected: " + message);
        } else {
            NetComm.print("CLIENT: Send to SERVER: " + message);
            tcpClient.sendMessage(message);
        }
    }
    
    public static boolean isTabSelected(TabSelect tab) {
        boolean status = false;
        int ix = tabPanel.getSelectedIndex();
        String name = "";
        switch (tab) {
            case SCRIPT:
                name = "Script";
                break;
            case VARIABLES:
                name = "Variables";
                break;
            case OUTPUT:
                name = "User Output";
                break;
            case LOGGER:
                name = "Debug log";
                break;
            case NETCOMM:
                name = "Network Comm";
                break;
        }
        if (ix >= 0 && ix < panelId.size()) {
            status = panelId.get(ix).contentEquals(name);
        }
        return status;
    }

    private static void enableButton (String buttonName) {
        JButton button = guiControls.getButton(buttonName);
        if (button == null) {
            setErrorStatus("GUI: Invalid button name: " + buttonName);
            return;
        }
        button.setEnabled(true);
    }

    private static void buttonInProcess() {
        disableButton("BTN_CONNECT");
        disableButton("BTN_LOAD");
        disableButton("BTN_COMPILE");
        disableButton("BTN_RUN");
        disableButton("BTN_PAUSE");
        disableButton("BTN_STEP");
        disableButton("BTN_BREAKPT");
        disableButton("BTN_EXIT");
    }
    
    private static void buttonComplete() {
        enableButton("BTN_CONNECT");
        enableButton("BTN_LOAD");
        enableButton("BTN_COMPILE");
        enableButton("BTN_EXIT");
    }
    
    private static void disableButton (String buttonName) {
        JButton button = guiControls.getButton(buttonName);
        if (button == null) {
            setErrorStatus("GUI: Invalid button name: " + buttonName);
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
            setErrorStatus("GUI: Invalid button name: " + buttonName);
            return;
        }
        button.setText(text);
    }
    
    private static void setLabelText (String labelName, String text) {
        if (text == null) {
            text = "";
        }
        if (labelName == null) {
            setErrorStatus("GUI: Null label name");
            return;
        }
        JLabel label = guiControls.getLabel(labelName);
        if (label == null) {
            setErrorStatus("GUI: Invalid label name: " + labelName);
            return;
        }
        label.setText(text);
    }
    private static void setState (String state) {
        if (state == null || state.isBlank()) {
            return;
        }

        // check for special error cases
        if (state.startsWith("UNKNOWN: ")) {
            setErrorStatus("Invalid command sent to SERVER: " + state.substring(9));
            return;
        }
        if (state.startsWith("ERROR: ")) {
            setErrorStatus(state.substring(7));
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
                disableButton("BTN_EXIT");
                breakpointEnable(false);
                setButtonText("BTN_CONNECT", "Connect");
                setLabelText ("LBL_LOAD", "");
                clearCommandLine();
                clearSubStack();
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
                enableButton ("BTN_EXIT");
                breakpointEnable(false);
                setButtonText("BTN_CONNECT", "Disconnect");
                setLabelText ("LBL_LOAD", "");
                clearErrorStatus ();
                clearCommandLine();
                clearSubStack();
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
                disableButton("BTN_EXIT");
                breakpointEnable(false);
                setButtonText("BTN_CONNECT", "Connect");
                setLabelText ("LBL_LOAD", "");
                clearCommandLine();
                clearSubStack();
                break;
            case "LOADED":
                procState = ProcState.LOADED;
                enableButton ("BTN_COMPILE");
                disableButton("BTN_RUN");
                disableButton("BTN_PAUSE");
                disableButton("BTN_STEP");
                breakpointEnable(false);
                clearCommandLine();
                clearSubStack();
                Script.setCurrentLine(-1);
                breakpointUnset();
                updateStateLabel (state);
                break;
            case "COMPILED":
                procState = ProcState.COMPILED;
                updateStateLabel (state);
                enableButton("BTN_RUN");
                enableButton("BTN_STEP");
                breakpointEnable(true);
                setButtonText("BTN_RUN", "Run");
                clearCommandLine();
                clearSubStack();
                updateStateLabel (state);
                break;
            case "EOF":
            case "STOPPED":
                procState = ProcState.COMPILED;
                updateStateLabel (state);
                Variables.print();
                enableButton ("BTN_RUN");
                disableButton("BTN_STEP");
                disableButton("BTN_PAUSE");
                setButtonText("BTN_PAUSE", "Pause");
                setButtonText("BTN_RUN", "Reset");
                buttonComplete();
                clearCommandLine();
                clearSubStack();
                break;
            case "PAUSED":
            case "STEPPED":
            case "BREAK":
                procState = ProcState.PAUSED;
                updateStateLabel (state);
                Variables.print();
                enableButton("BTN_RUN");
                enableButton("BTN_STEP");
                enableButton("BTN_PAUSE");
                setButtonText("BTN_PAUSE", "Resume");
                setButtonText("BTN_RUN", "Stop");
                buttonComplete();
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
            case "BREAKPT SET":
                break;
            case "BREAKPT INVALID":
                breakpointUnset();
                break;
            case "ERROR":
                if (procState == ProcState.RUNNING) {
                    procState = ProcState.COMPILED;
                    updateStateLabel (state);
                    Variables.print();
                    enableButton ("BTN_RUN");
                    disableButton("BTN_STEP");
                    disableButton("BTN_PAUSE");
                    setButtonText("BTN_PAUSE", "Pause");
                    setButtonText("BTN_RUN", "Run");
                    buttonComplete();
                    clearCommandLine();
                    clearSubStack();
                }
                break;
            default:
                setErrorStatus("Invalid STATUS command: " + state);
                break;
        }
    }
    
    private static void loadScriptButtonActionPerformed(java.awt.event.ActionEvent evt) {
//        String defaultName = "program";
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Script Files", "scr");
        fileSelector.setFileFilter(filter);
//        fileSelector.setSelectedFile(new File(defaultName + ".scr"));
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
  
//    private static void saveScriptButtonActionPerformed(java.awt.event.ActionEvent evt) {
//        String defaultName = "program";
//        FileNameExtensionFilter filter = new FileNameExtensionFilter("Script Files", "scr");
//        fileSelector.setFileFilter(filter);
//        fileSelector.setApproveButtonText("Save");
//        fileSelector.setMultiSelectionEnabled(false);
//        fileSelector.setSelectedFile(new File(defaultName + ".scr"));
//        int retVal = fileSelector.showOpenDialog(guiControls.getFrame());
//        if (retVal == JFileChooser.APPROVE_OPTION) {
//            File file = fileSelector.getSelectedFile();
//            // get the base name without extension so we can create matching json and png files
//            String basename = file.getAbsolutePath();
//            int offset = basename.lastIndexOf('.');
//            if (offset > 0) {
//                basename = basename.substring(0, offset);
//            }
//
//            // remove any pre-existing file and save updated script
//            File scriptFile = new File(basename + ".scr");
//           scriptFile.delete();
//            Script.dataSave(graphFile);
//        }
//    }
  

    private static void breakpointEnable (boolean enable) {
        JButton    breakButton = guiControls.getButton("BTN_BREAKPT");
        JLabel     breakLabel  = guiControls.getLabel("LBL_BREAKPT");
        JTextField breakLine   = guiControls.getTextField("TXT_BREAKPT");
        breakButton.setEnabled(enable);
        breakLabel.setEnabled(enable);
        breakLine.setEnabled(enable);
        breakButton.setText("Enable");
        breakLabel.setText("OFF");
        breakLine.setText("");
        
        if (! enable) {
            Script.setBreakpointLine(-1);
            Script.refresh();
        }
    }
    
    private static void breakpointSet () {
        JButton    breakButton = guiControls.getButton("BTN_BREAKPT");
        JLabel     breakLabel  = guiControls.getLabel("LBL_BREAKPT");
        JTextField breakLine   = guiControls.getTextField("TXT_BREAKPT");

        Integer line;
        String strLine = breakLine.getText();
        try {
            line = Integer.valueOf(strLine);
        } catch (NumberFormatException exMsg) {
            breakLine.setText("");
            NetComm.print("ERROR: Invalid breakpoint entry: " + strLine);
            return;
        }

        Script.setBreakpointLine(line);
        Script.refresh();

        breakButton.setText("Disable");
        breakLabel.setText("ON");
        breakLine.setEnabled(false);

        NetComm.print("STATUS: BREAKPT ON button pressed: line " + strLine);
        sendMessage("BREAKPT " + strLine);
    }
    
    private static void breakpointUnset () {
        JButton    breakButton = guiControls.getButton("BTN_BREAKPT");
        JLabel     breakLabel  = guiControls.getLabel("LBL_BREAKPT");
        JTextField breakLine   = guiControls.getTextField("TXT_BREAKPT");

        Script.setBreakpointLine(-1);
        Script.refresh();

        breakButton.setText("Enable");
        breakLabel.setText("OFF");
        breakLine.setText("");
        breakLine.setEnabled(true);

        NetComm.print("STATUS: BREAKPT OFF button pressed");
        sendMessage("BREAKPT OFF");
    }
    
    private static void clearCommandLine () {
        String name = "LBL_COMMAND";
        JLabel label = guiControls.getLabel(name);
        if (label == null) {
            NetComm.print("ERROR: GUI: Invalid label name: " + name);
        } else {
            label.setText("");
        }
    }
    
    private static void setCommandLine (String text) {
        String name = "LBL_COMMAND";
        JLabel label = guiControls.getLabel(name);
        if (label == null) {
            NetComm.print("ERROR: GUI: Invalid label name: " + name);
        } else {
            label.setForeground(Color.black);
            label.setText(text);
        }
    }
    
    private static void clearSubStack () {
        String name = "LBL_SUBSTACK";
        JLabel label = guiControls.getLabel(name);
        if (label == null) {
            NetComm.print("ERROR: GUI: Invalid label name: " + name);
        } else {
            label.setText("");
        }
    }
    
    private static void setSubStack (String text) {
        String name = "LBL_SUBSTACK";
        if (text == null || text.isEmpty()) {
            return;
        }
        JLabel label = guiControls.getLabel(name);
        if (label == null) {
            NetComm.print("ERROR: GUI: Invalid label name: " + text);
        } else {
            label.setForeground(Color.black);
            var array = new ArrayList<String>(Arrays.asList(text.split(DATA_SEP)));
            if (array.size() == 1) {
                label.setText(text);
            } else {
                String response = array.getFirst();
                for (int ix = 1; ix < array.size(); ix++) {
                    response += " -> " + array.get(ix);
                }
                label.setText(response);
            }
        }
    }
    
    private static void updateStateLabel (String text) {
        String name = "TXT_STATE";
        if (! text.isEmpty()) {
            JTextField textField = guiControls.getTextField(name);
            if (textField == null) {
                NetComm.print("ERROR: GUI: Invalid textField name: " + name);
            } else {
                textField.setForeground(Color.black);
                textField.setText(text);
            }
        }
    }
    
    private static void clearErrorStatus () {
        String name = "LBL_ERROR";
        JLabel label = guiControls.getLabel(name);
        if (label == null) {
            NetComm.print("ERROR: GUI: Invalid label name: " + name);
        } else {
            label.setText("");
        }
    }
    
    public static void setErrorStatus (String text) {
        String name = "LBL_ERROR";
        text = "ERROR: " + text;
        JLabel label = guiControls.getLabel(name);
        if (label == null) {
            NetComm.print("ERROR: GUI: Invalid label name: " + name);
        } else {
            label.setForeground(Color.red);
            label.setText(text);
        }
        NetComm.print(text);
    }
    
    public static void serverConnected() {
        setState ("CONNECTED");
    }

    public static void serverDisconnected() {
        setState ("DISCONNECTED");
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
        // output all server messages that do not go to the Debug log window to the Output window.
        if (! command.contentEquals("LOGMSG:")) {
            NetComm.print("SERVER: " + message);
        }
        
        switch (command) {
            case "STATUS:":
                setState(msgDisplay.substring(8));
                break;
            case "SUBSTACK:":
                message = message.substring(command.length()).strip();
                setSubStack(message);
                break;
            case "LINE:":
                if (words.size() < 2) {
                    setErrorStatus("Missing value for LINE message");
                    break;
                }
                String strValue = words.get(1);
                Integer lineNum = 1;
                try {
                    lineNum = Integer.valueOf(strValue);
                } catch (NumberFormatException ex) {
                    setErrorStatus("Invalid Integer value for line: '" + strValue + "'");
                    break;
                }
                // TODO: highlight the line number
                if (lineNum < 0) {
                    setCommandLine("END");
                } else {
                    String line = Script.getLine(lineNum);
                    setCommandLine(line);
                    Script.setCurrentLine(lineNum);
                    Script.refresh();
                }
                break;
            case "LOGMSG:":
                // add the log info to the log screen
                if (msglen < 10) {
                    setErrorStatus("Invalid format for LOGMSG command");
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
            case "OUTPUT:":
                message = message.substring(command.length()).strip();
                Output.print(message);
                break;
            default:
                Logger.printError(message);
                break;
        }
    }

}
