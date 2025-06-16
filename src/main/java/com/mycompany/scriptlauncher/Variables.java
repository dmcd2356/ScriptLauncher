/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.scriptlauncher;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.JTextPane;

/**
 *
 * @author dan
 */
public class Variables {
    /*
    * This class handles receiving the Variables from the network (both Allocation
    *  definitions and the contents) and displaying the these entries on the Variables Pane.
    */
    
    private static JTextPane textPane = null;
    private static final HashMap<MessageType, FontInfo> fontInfoTbl = new HashMap<>();
    private static String section = null;

    private static ArrayList<VarAccess>     varReserved = new ArrayList<>();
    private static ArrayList<VarAccess>     varGlobal   = new ArrayList<>();
    private static ArrayList<VarAccess>     varLocal    = new ArrayList<>();
    private static ArrayList<VarAccess>     varLoop     = new ArrayList<>();
    private static HashMap<String, Integer> subroutines = new HashMap<>();
    
    private enum MessageType {
        Prefix,         // the line counter value
        Comment,        // a comment line
        Command,        // a command (part of a line)
        CmdOption,      // a command option (part of a line)
        Reference,      // a variable reference value (part of a line)
        Numeric,        // a numeric value (part of a line)
        Quoted,         // a quoted String value (part of a line)
        Normal,         // anything else
    }

    /**
     * initializes the pane info
     * 
     * @param textpane - the pane to initialize for writing
     */
    public static void init (JTextPane textpane) {
        textPane = textpane;
        setColors();
    }

    public static void addSubroutine (String name, Integer lineNum) {
        subroutines.put(name, lineNum);
    }

    /**
     * clears the display.
     */
    private static void reset() {
        // reset the variable info gathered from the server
        varReserved = new ArrayList<>();
        varGlobal   = new ArrayList<>();
        varLocal    = new ArrayList<>();
        varLoop     = new ArrayList<>();
        subroutines = new HashMap<>();
    }

    /**
     * updates the display immediately
     */
    public static final void updateDisplay () {
        if (textPane != null) {
            Graphics graphics = textPane.getGraphics();
            if (graphics != null) {
                textPane.update(graphics);
            }
        }
    }

    /**
     * extracts the next word in a string.
     * (includes any preceding space chars)
     * 
     * @param message - the message to parse
     * 
     * @return the next word from the string
     */
    private static String getNextWord (String message) {
        String word = "";
        int ix = 0;
        for(; ix < message.length(); ix++) {
            if (message.charAt(ix) == ' ') {
                word = word + " ";
            } else {
                break;
            }
        }
        for(; ix < message.length(); ix++) {
            if (message.charAt(ix) != ' ') {
                word = word + message.charAt(ix);
            } else {
                break;
            }
        }
        return word;
    }

    private static String addTabPadding (int tab, String line) {
        String padding = "                                        ";
        if (line.length() >= tab) {
            return line + " ";
        }
        line = line + padding;
        line = line.substring(0, tab);
        return line;
    }
    
    /**
     * displays the variables received.
     */
    public static final void print() {
        if (textPane == null) {
            return;
        }
        
        // first, clear the display the display
        textPane.setText("");

        // set the tab stops
        int tab1 = 25;  // handles variable names up to 24 in length
        int tab2 = 40;  // handles subroutines up to 14 in length
        
        // now display each section
        String title = addTabPadding (tab2, addTabPadding (tab1, "Variable name") + "Owner") + "Data type";
        if (! varReserved.isEmpty()) {
            printType(MessageType.Prefix, true, "=== RESERVED ============================================================");
            printType(MessageType.Prefix, true, title);
            printType(MessageType.Prefix, true, "_________________________________________________________________________");
            for (int ix = 0; ix < varReserved.size(); ix++) {
                VarAccess varInfo = varReserved.get(ix);
                String line = addTabPadding (tab2, addTabPadding (tab1, varInfo.getName()) + "----") + varInfo.getType();
                printType(MessageType.Prefix, true, line);
            }
        }
        if (! varGlobal.isEmpty()) {
            printType(MessageType.Prefix, true, "=== GLOBALS =============================================================");
            printType(MessageType.Prefix, true, title);
            printType(MessageType.Prefix, true, "_________________________________________________________________________");
            for (int ix = 0; ix < varGlobal.size(); ix++) {
                VarAccess varInfo = varGlobal.get(ix);
                String line = addTabPadding (tab2, addTabPadding (tab1, varInfo.getName()) + varInfo.getOwner()) + varInfo.getType();
                printType(MessageType.Prefix, true, line);
            }
        }
        if (! varLocal.isEmpty()) {
            printType(MessageType.Prefix, true, "=== LOCALS ==============================================================");
            printType(MessageType.Prefix, true, title);
            printType(MessageType.Prefix, true, "_________________________________________________________________________");
            for (int ix = 0; ix < varLocal.size(); ix++) {
                VarAccess varInfo = varLocal.get(ix);
                String line = addTabPadding (tab2, addTabPadding (tab1, varInfo.getName()) + varInfo.getOwner()) + varInfo.getType();
                printType(MessageType.Prefix, true, line);
            }
        }
        if (! varLoop.isEmpty()) {
            printType(MessageType.Prefix, true, "=== LOOPS ===============================================================");
            printType(MessageType.Prefix, true, title);
            printType(MessageType.Prefix, true, "_________________________________________________________________________");
            for (int ix = 0; ix < varLoop.size(); ix++) {
                VarAccess varInfo = varLoop.get(ix);
                String line = addTabPadding (tab2, addTabPadding (tab1, varInfo.getName()) + varInfo.getOwner()) + "Integer";
                printType(MessageType.Prefix, true, line);
            }
        }
    }
    
    /**
     * parses and saves the variable allocation information.
     * 
     * @param message   - the message contents
     */
    public static final void allocationMessage (String message) {
        if (textPane != null && message != null && ! message.isEmpty()) {
            if (message.charAt(0) == '<' && message.charAt(message.length()-1) == '>') {
                String entry = message.substring(1, message.length()-1);
                switch (entry) {
                    case "RESERVED":
                    case "GLOBAL":
                    case "LOCAL":
                    case "LOOP":
                        // save the section
                        section = entry;
                        break;
                    case "START":
                        reset();
                        break;
                    case "END":
                        // allocation info complete - now format and print it
                        section = null;
                        print();
                        break;
                    default:
                        section = null;
                        GuiPanel.setStatusText(true, "Invalid message received");
                }
                return;
            } else if (message.charAt(0) == '[' && message.charAt(message.length()-1) == ']') {
                if (section == null) {
                    GuiPanel.setStatusText(true, "Missing section name");
                    return;
                }
                message = message.substring(1, message.length()-1);
                var array = new ArrayList<String>(Arrays.asList(message.split(",")));
                allocationEntryMsg (array);
                return;
            }
        }
        GuiPanel.setStatusText(true, "Invalid message received");
    }

    /**
     * outputs the various types of messages to be saved in the variable listing.
     * 
     * @param contents   - the message contents
     */
    public static final void allocationEntryMsg (ArrayList<String> contents) {
        if (textPane != null && contents != null && ! contents.isEmpty()) {
            String name = null;
            String type = null;
            String owner = null;
            String value = null;
            String writer = null;
            String line = null;
            String time = null;
            for (int ix = 0; ix < contents.size(); ix++) {
                String entry = contents.get(ix).strip();
                int offset = entry.indexOf(' ');
                if (offset <= 0) {
                    GuiPanel.setStatusText(true, "Invalid message received");
                    return;
                }
                String key  = entry.substring(0, offset).strip();
                String item = entry.substring(offset).strip();
                switch (key) {
                    case "<name>":
                        name = item;
                        break;
                    case "<type>":
                        type = item;
                        break;
                    case "<owner>":
                        owner = item;
                        break;
                    case "<value>":
                        value = item;
                        break;
                    case "<writer>":
                        writer = item;
                        break;
                    case "<line>":
                        line = item;
                        break;
                    case "<time>":
                        time = item;
                        break;
                    default:
                        GuiPanel.setStatusText(true, "Invalid message received");
                        return;
                }
            }
            if (name == null) {
                GuiPanel.setStatusText(true, "Invalid message received");
                return;
            }

            if (value == null) {
                // new allocation
                VarAccess varInfo = new VarAccess(name, type, owner);
                switch (section) {
                    case "RESERVED":
                        varReserved.add(varInfo);
                        break;
                    case "GLOBAL":
                        varGlobal.add(varInfo);
                        break;
                    case "LOCAL":
                        varLocal.add(varInfo);
                        break;
                    case "LOOP":
                        varLoop.add(varInfo);
                        break;
                    default:
                        section = null;
                        GuiPanel.setStatusText(true, "Invalid message received");
                }
            } else {
                // variable value changed
                boolean bFound = false;
                switch (section) {
                    case "RESERVED":
                        for (int varIx = 0; varIx < varReserved.size(); varIx++) {
                            VarAccess varInfo = varReserved.get(varIx);
                            if (varInfo.getName().contentEquals(name)) {
                                varInfo.setValueString(value, writer, line, time);
                                varReserved.set(varIx, varInfo);
                                bFound = true;
                                break;
                            }
                        }
                        break;
                    case "GLOBAL":
                        for (int varIx = 0; varIx < varGlobal.size(); varIx++) {
                            VarAccess varInfo = varGlobal.get(varIx);
                            if (varInfo.getName().contentEquals(name)) {
                                varInfo.setValueString(value, writer, line, time);
                                varGlobal.set(varIx, varInfo);
                                bFound = true;
                                break;
                            }
                        }
                        break;
                    case "LOCAL":
                        for (int varIx = 0; varIx < varLocal.size(); varIx++) {
                            VarAccess varInfo = varLocal.get(varIx);
                            if (varInfo.getName().contentEquals(name)) {
                                varInfo.setValueString(value, writer, line, time);
                                varLocal.set(varIx, varInfo);
                                bFound = true;
                                break;
                            }
                        }
                        break;
                    case "LOOP":
                        for (int varIx = 0; varIx < varLoop.size(); varIx++) {
                            VarAccess varInfo = varLoop.get(varIx);
                            if (varInfo.getName().contentEquals(name)) {
                                varInfo.setValueString(value, writer, line, time);
                                varLoop.set(varIx, varInfo);
                                bFound = true;
                                break;
                            }
                        }
                        break;
                    default:
                        section = null;
                        GuiPanel.setStatusText(true, "Invalid message received");
                }
                if (! bFound) {
                    GuiPanel.setStatusText(true, section + " variable not found: " + name);
                }
            }
        }
    }

    private static void printType (MessageType type, boolean term, String message) {
        FontInfo fontInfo = fontInfoTbl.get(type);
        if (fontInfo == null) {
            fontInfo = new FontInfo(FontInfo.TextColor.Black,
                                   FontInfo.FontType.Normal, 11, "Courier");
        }

        TextWriter.print(textPane, term, fontInfo, message);
    }
    
    private static void setColors () {
        if (textPane != null) {
            // these are for public consumption
            setTypeColor (MessageType.Prefix   , FontInfo.TextColor.Black , FontInfo.FontType.Normal);
            setTypeColor (MessageType.Comment  , FontInfo.TextColor.Green , FontInfo.FontType.Italic);
            setTypeColor (MessageType.Command  , FontInfo.TextColor.Red   , FontInfo.FontType.Normal);
            setTypeColor (MessageType.CmdOption, FontInfo.TextColor.Orange, FontInfo.FontType.Normal);
            setTypeColor (MessageType.Reference, FontInfo.TextColor.Violet, FontInfo.FontType.Italic);
            setTypeColor (MessageType.Numeric  , FontInfo.TextColor.Blue  , FontInfo.FontType.Normal);
            setTypeColor (MessageType.Quoted   , FontInfo.TextColor.Blue  , FontInfo.FontType.Normal);
            setTypeColor (MessageType.Normal   , FontInfo.TextColor.Brown , FontInfo.FontType.Normal);
        }
    }
  
    /**
     * sets the association between a type of message and the font characteristics.
     * 
     * @param type  - the type to associate with the font characteristics
     * @param color - the color to assign to the type
     * @param ftype - the font attributes to associate with the type
     */
    private static void setTypeColor (MessageType type, FontInfo.TextColor color, FontInfo.FontType ftype) {
        int size = 11;
        String font = "Courier";
        FontInfo fontinfo = new FontInfo(color, ftype, size, font);
        if (fontInfoTbl.containsKey(type)) {
            fontInfoTbl.replace(type, fontinfo);
        }
        else {
            fontInfoTbl.put(type, fontinfo);
        }
    }
    
}
