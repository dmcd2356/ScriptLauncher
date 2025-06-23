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
    
    // the chars used to seperate entries in reporting variable contents to the client
    private static final String DATA_SEP = "::";
    
    private static JTextPane textPane = null;
    private static final HashMap<MessageType, FontInfo> fontInfoTbl = new HashMap<>();
    private static String section = null;

    private static ArrayList<VarAccess>     varReserved = new ArrayList<>();
    private static ArrayList<VarAccess>     varGlobal   = new ArrayList<>();
    private static ArrayList<VarAccess>     varLocal    = new ArrayList<>();
    private static ArrayList<VarAccess>     varLoop     = new ArrayList<>();
    private static HashMap<String, Integer> subroutines = new HashMap<>();
    
    private enum MessageType {
        Title,          // header entry
        Normal,         // unchanged variable entry
        Changed,        // variable that has changed
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

    public static void clear () {
        if (textPane != null) {
            textPane.setText("");
        }
    }

    public static void addSubroutine (String name, Integer lineNum) {
        subroutines.put(name, lineNum);
    }

    /**
     * clears the display.
     */
    private static void resetVariables() {
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
     * this resets the changed status for each variable and updates the display.
     * (this should be called at the start of a RUN or STEP command)
     */
    public static void resetChanged () {
        // reset all variable change flags to "not changed"
        for (int ix = 0; ix < varReserved.size(); ix++) {
            varReserved.get(ix).resetVarChanged();
        }
        for (int ix = 0; ix < varGlobal.size(); ix++) {
            varGlobal.get(ix).resetVarChanged();
        }
        for (int ix = 0; ix < varLocal.size(); ix++) {
            varLocal.get(ix).resetVarChanged();
        }
        for (int ix = 0; ix < varLoop.size(); ix++) {
            varLoop.get(ix).resetVarChanged();
        }
        
        // re-draw the variables
        print();
    }
    
    /**
     * displays the variables received.
     */
    public static void print() {
        if (textPane == null) {
            return;
        }
        
        // first, clear the display the display
        textPane.setText("");

        // set the tab stops for Loops (NAME is at offset 0)
        int tab1 = 25;          // OWNER offset: handles variable names up to 24 in length
        int tab2 = tab1 + 15;   // TYPE  offset: handles subroutines up to 14 in length
        int tab3 = tab2 + 12;   // VALUE offset: data type is ALWAYS Integer = 7
        int tab4 = tab3 + 8;    // START offset: max expected length of numeric value is 6
        int tab5 = tab4 + 8;    // STOP  offset: max expected length of numeric value is 6
        int tab6 = tab5 + 8;    // STEP  offset: max expected length of numeric value is 6
        int tab7 = tab6 + 8;    // INCL  offset: max expected length of numeric value is 6
        int tab8 = tab7 + 8;    // COMP  offset: max length of Incl is 5 (true or false)
        
        // setup the header for Loops
        String title = "Variable name";
        title = addTabPadding (tab1, title) + "Owner";
        title = addTabPadding (tab2, title) + "Data type";
        title = addTabPadding (tab3, title) + "Value";
        title = addTabPadding (tab4, title) + "Start";
        title = addTabPadding (tab5, title) + "Stop";
        title = addTabPadding (tab6, title) + "Step";
        title = addTabPadding (tab7, title) + "Incl";
        title = addTabPadding (tab8, title) + "Comp";
        
        if (! varLoop.isEmpty()) {
            printType(MessageType.Title, true, "=== LOOPS ===============================================================================================");
            printType(MessageType.Title, true, title);
            printType(MessageType.Title, true, "_________________________________________________________________________________________________________");
            for (int ix = 0; ix < varLoop.size(); ix++) {
                VarAccess varInfo = varLoop.get(ix);
                String value = varInfo.getValueString();
                if (value == null || value.isEmpty()) {
                    value = "----";
                }
                String line = varInfo.getName();
                line = addTabPadding (tab1, line) + varInfo.getOwner();
                line = addTabPadding (tab2, line) + "Integer";
                line = addTabPadding (tab3, line) + value;
                line = addTabPadding (tab4, line) + varInfo.getStartValue();
                line = addTabPadding (tab5, line) + varInfo.getEndValue();
                line = addTabPadding (tab6, line) + varInfo.getStepValue();
                line = addTabPadding (tab7, line) + varInfo.getIncl();
                line = addTabPadding (tab8, line) + varInfo.getCompSign();
                if (varInfo.isVarChanged()) {
                    printType(MessageType.Changed, true, line);
                } else {
                    printType(MessageType.Normal, true, line);
                }
            }
            printlf();
        }

        // update tab stops and title bar for all other parameters
        tab1 = 25;          // OWNER  offset: handles variable names up to 24 in length
        tab2 = tab1 + 15;   // TYPE   offset: handles subroutines up to 14 in length
        tab3 = tab2 + 12;   // WRITER offset: max length of type is 8
        tab4 = tab3 + 15;   // LINE   offset: handles subroutines up to 14 in length
        tab5 = tab4 + 8;    // TIME   offset: max line number of 4
        tab6 = tab5 + 12;   // VALUE offset: time is always 9 chars long:  00:00.000
        
        title = "Variable name";
        title = addTabPadding (tab1, title) + "Owner";
        title = addTabPadding (tab2, title) + "Data type";
        title = addTabPadding (tab3, title) + "Writer";
        title = addTabPadding (tab4, title) + "Line";
        title = addTabPadding (tab5, title) + "Time";
        title = addTabPadding (tab6, title) + "Value";

        if (! varReserved.isEmpty()) {
            printType(MessageType.Title, true, "=== RESERVED ============================================================================================");
            printType(MessageType.Title, true, title);
            printType(MessageType.Title, true, "_________________________________________________________________________________________________________");
            for (int ix = 0; ix < varReserved.size(); ix++) {
                VarAccess varInfo = varReserved.get(ix);
                String value = varInfo.getValueString();
                if (value == null || value.isEmpty()) {
                    value = "----";
                }
                String line = varInfo.getName();
                line = addTabPadding (tab1, line) + "----";
                line = addTabPadding (tab2, line) + varInfo.getType().toString();
                line = addTabPadding (tab3, line) + varInfo.getWriter();
                line = addTabPadding (tab4, line) + varInfo.getWriterIndex();
                line = addTabPadding (tab5, line) + varInfo.getWriterTime();
                line = addTabPadding (tab6, line) + value;
                if (varInfo.isVarChanged()) {
                    printType(MessageType.Changed, true, line);
                } else {
                    printType(MessageType.Normal, true, line);
                }
            }
            printlf();
        }
        if (! varGlobal.isEmpty()) {
            printType(MessageType.Title, true, "=== GLOBALS =============================================================================================");
            printType(MessageType.Title, true, title);
            printType(MessageType.Title, true, "_________________________________________________________________________________________________________");
            for (int ix = 0; ix < varGlobal.size(); ix++) {
                VarAccess varInfo = varGlobal.get(ix);
                String value = varInfo.getValueString();
                if (value == null || value.isEmpty()) {
                    value = "----";
                }
                String line = varInfo.getName();
                line = addTabPadding (tab1, line) + varInfo.getOwner();
                line = addTabPadding (tab2, line) + varInfo.getType().toString();
                line = addTabPadding (tab3, line) + varInfo.getWriter();
                line = addTabPadding (tab4, line) + varInfo.getWriterIndex();
                line = addTabPadding (tab5, line) + varInfo.getWriterTime();
                line = addTabPadding (tab6, line) + value;
                if (varInfo.isVarChanged()) {
                    printType(MessageType.Changed, true, line);
                } else {
                    printType(MessageType.Normal, true, line);
                }
            }
            printlf();
        }
        if (! varLocal.isEmpty()) {
            printType(MessageType.Title, true, "=== LOCALS ==============================================================================================");
            printType(MessageType.Title, true, title);
            printType(MessageType.Title, true, "_________________________________________________________________________________________________________");
            for (int ix = 0; ix < varLocal.size(); ix++) {
                VarAccess varInfo = varLocal.get(ix);
                String value = varInfo.getValueString();
                if (value == null || value.isEmpty()) {
                    value = "----";
                }
                String line = varInfo.getName();
                line = addTabPadding (tab1, line) + varInfo.getOwner();
                line = addTabPadding (tab2, line) + varInfo.getType().toString();
                line = addTabPadding (tab3, line) + varInfo.getWriter();
                line = addTabPadding (tab4, line) + varInfo.getWriterIndex();
                line = addTabPadding (tab5, line) + varInfo.getWriterTime();
                line = addTabPadding (tab6, line) + value;
                if (varInfo.isVarChanged()) {
                    printType(MessageType.Changed, true, line);
                } else {
                    printType(MessageType.Normal, true, line);
                }
            }
            printlf();
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
                        resetVariables();
                        break;
                    case "END":
                        // allocation info complete - now format and print it
                        section = null;
                        print();
                        break;
                    default:
                        section = null;
                        GuiPanel.setStatusError("ALLOC command - Invalid type " + entry);
                }
                return;
            } else if (message.charAt(0) == '[' && message.charAt(message.length()-1) == ']') {
                message = message.substring(1, message.length()-1);
                var array = new ArrayList<String>(Arrays.asList(message.split(DATA_SEP)));
                allocationEntryMsg (array);
                return;
            }
        }
        GuiPanel.setStatusError("ALLOC command - Invalid format: " + message);
    }

    /**
     * outputs the various types of messages to be saved in the variable listing.
     * 
     * @param contents   - the message contents
     */
    public static final void allocationEntryMsg (ArrayList<String> contents) {
        if (textPane != null && contents != null && ! contents.isEmpty()) {
            String sect = null;
            String name = null;
            String type = null;
            String owner = null;
            String value = null;
            String writer = null;
            String line = null;
            String time = null;
            String start = null;
            String end = null;
            String step = null;
            String incl = null;
            String comp = null;
            for (int ix = 0; ix < contents.size(); ix++) {
                String entry = contents.get(ix).strip();
                int offset = entry.indexOf(' ');
                if (offset <= 0) {
                    GuiPanel.setStatusError("VARMSG command - invalid entry format: " + entry);
                    return;
                }
                String key  = entry.substring(0, offset).strip();
                String item = entry.substring(offset).strip();
                switch (key) {
                    case "<section>":
                        sect = item;
                        break;
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
                    case "<start>":
                        start = item;
                        break;
                    case "<end>":
                        end = item;
                        break;
                    case "<step>":
                        step = item;
                        break;
                    case "<incl>":
                        incl = item;
                        break;
                    case "<comp>":
                        comp = item;
                        break;
                    default:
                        GuiPanel.setStatusError("VARMSG command - Invalid key: " + key);
                        return;
                }
            }
            if (name == null) {
                GuiPanel.setStatusError("VARMSG command - missing <name> entry");
                return;
            }

            // changed variables issue a section name in the entry.
            // allocation omits this and just provides a header preceding the variables
            //  that define the applicable section, which is saved in the global 'section'.
            // if neither are found, we have an error.
            if (value == null || sect == null) {
                // new allocation
                if (section == null) {
                    GuiPanel.setStatusError("Missing section header");
                    return;
                }
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
                        GuiPanel.setStatusError("Invalid section name: " + section);
                }
            } else {
                // variable value changed
                boolean bFound = false;
                switch (sect) {
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
                        if (! bFound) {
                            VarAccess varInfo = new VarAccess(name, type, owner);
                            varInfo.setValueString(value, writer, line, time);
                            varLocal.add(varInfo);
                            bFound = true;
                        }
                        break;
                    case "LOOP":
                        for (int varIx = 0; varIx < varLoop.size(); varIx++) {
                            VarAccess varInfo = varLoop.get(varIx);
                            if (varInfo.getName().contentEquals(name)) {
                                varInfo.setValueLoop (value, start, end, step, incl, comp);
                                varLoop.set(varIx, varInfo);
                                bFound = true;
                                break;
                            }
                        }
                        if (! bFound) {
                            VarAccess varInfo = new VarAccess(name, type, owner);
                            varInfo.setValueLoop (value, start, end, step, incl, comp);
                            varLoop.add(varInfo);
                            bFound = true;
                        }
                        break;
                    default:
                        section = null;
                        GuiPanel.setStatusError("VARMSG command - Invalid section: " + sect);
                }
                if (! bFound) {
                    GuiPanel.setStatusError(sect + " variable not found: " + name);
                }
            }
        }
    }

    private static void printType (MessageType type, boolean term, String message) {
        FontInfo fontInfo = fontInfoTbl.get(type);
        if (fontInfo == null) {
            fontInfo = new FontInfo(FontInfo.TextColor.Black,
                                   FontInfo.FontType.Normal, 14, "Courier");
        }

        TextWriter.print(textPane, term, fontInfo, message);
    }

    private static void printlf() {
        TextWriter.printlf(textPane);
    }
    
    private static void setColors () {
        if (textPane != null) {
            // these are for public consumption
            setTypeColor (MessageType.Title    , FontInfo.TextColor.Blue  , FontInfo.FontType.Italic);
            setTypeColor (MessageType.Normal   , FontInfo.TextColor.Black , FontInfo.FontType.Normal);
            setTypeColor (MessageType.Changed  , FontInfo.TextColor.Red   , FontInfo.FontType.Normal);
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
        int size = 14;
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
