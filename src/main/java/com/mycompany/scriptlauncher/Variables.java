/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.scriptlauncher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.JScrollPane;
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
    
    private static final int    FONT_SIZE = 14;
    private static final String FONT_TYPE = "Courier";
    
    private static JTextPane textPane = null;
    private static JScrollPane scrollPane = null;
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
     * @param textpane   - the pane to initialize for writing
     * @param scrollpane - the scroll pane it is embedded in
     */
    public static void init (JTextPane textpane, JScrollPane scrollpane) {
        textPane = textpane;
        scrollPane = scrollpane;
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
    public static void clearVariables() {
        // reset the variable info gathered from the server
        varReserved = new ArrayList<>();
        varGlobal   = new ArrayList<>();
        varLocal    = new ArrayList<>();
        varLoop     = new ArrayList<>();
        subroutines = new HashMap<>();
    }

    /**
     * clears the display.
     */
    public static void resetVariables() {
        for (int ix = 0; ix < varReserved.size(); ix++) {
            varReserved.get(ix).resetVarInfo();
        }
        for (int ix = 0; ix < varGlobal.size(); ix++) {
            varGlobal.get(ix).resetVarInfo();
        }
        for (int ix = 0; ix < varLocal.size(); ix++) {
            varLocal.get(ix).resetVarInfo();
        }
        for (int ix = 0; ix < varLoop.size(); ix++) {
            varLoop.get(ix).resetVarInfo();
        }
        
        // re-draw the variables
        print();

        // reset to top of list
        if (textPane != null && scrollPane != null) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() { 
                    scrollPane.getVerticalScrollBar().setValue(0);
                }
            });
        }
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

        // reset to top of list
        if (textPane != null && scrollPane != null) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() { 
                    scrollPane.getVerticalScrollBar().setValue(0);
                }
            });
        }
    }
    
    /**
     * updates the display immediately
     */
//    public static final void updateDisplay () {
//        if (textPane != null) {
//            Graphics graphics = textPane.getGraphics();
//            if (graphics != null) {
//                textPane.update(graphics);
//            }
//        }
//    }

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
     * finds the max string length of string entries in the variables.
     * 
     * @param section - section to examine
     * @param entry   - type of entry to check
     * 
     * @return 
     */
    public static int getMaxLength (String section, String entry) {
        int maxSize = 0;
        String strval = "";
        int length = 0;
        switch (section) {
            case "LOOPS":
                for (int ix = 0; ix < varLoop.size(); ix++) {
                    switch (entry) {
                        case "NAME":
                            strval = varLoop.get(ix).getName();
                            break;
                        default:
                            break;
                    }
                    if (strval == null) {
                        length = 8;
                    } else {
                        length = strval.length();
                    }
                    if (length > maxSize) {
                        maxSize = length;
                    }
                }
                break;
            case "RESERVED":
                for (int ix = 0; ix < varReserved.size(); ix++) {
                    switch (entry) {
                        case "NAME":
                            strval = varReserved.get(ix).getName();
                            break;
                        case "OWNER":
                            strval = varReserved.get(ix).getOwner();
                            break;
                        case "WRITER":
                            strval = varReserved.get(ix).getWriter();
                            break;
                        default:
                            break;
                    }
                    if (strval == null) {
                        length = 8;
                    } else {
                        length = strval.length();
                    }
                    if (length > maxSize) {
                        maxSize = length;
                    }
                }
                break;
            case "GLOBALS":
                for (int ix = 0; ix < varGlobal.size(); ix++) {
                    switch (entry) {
                        case "NAME":
                            strval = varGlobal.get(ix).getName();
                            break;
                        case "OWNER":
                            strval = varGlobal.get(ix).getOwner();
                            break;
                        case "WRITER":
                            strval = varGlobal.get(ix).getWriter();
                            break;
                        default:
                            break;
                    }
                    if (strval == null) {
                        length = 8;
                    } else {
                        length = strval.length();
                    }
                    if (length > maxSize) {
                        maxSize = length;
                    }
                }
                break;
            case "LOCALS":
                for (int ix = 0; ix < varLocal.size(); ix++) {
                    switch (entry) {
                        case "NAME":
                            strval = varLocal.get(ix).getName();
                            break;
                        case "OWNER":
                            strval = varLocal.get(ix).getOwner();
                            break;
                        case "WRITER":
                            strval = varLocal.get(ix).getWriter();
                            break;
                        default:
                            break;
                    }
                    if (strval == null) {
                        length = 8;
                    } else {
                        length = strval.length();
                    }
                    if (length > maxSize) {
                        maxSize = length;
                    }
                }
                break;
            default:
                break;
        }
        // limit to a range of 8 to 25 in length and then add 4 extra spaces of padding
        if (maxSize < 8)
            maxSize = 8;
        else if (maxSize > 25)
            maxSize = 25;
        return maxSize + 4;
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

        ArrayList<Integer> tabs = new ArrayList<>();
        int taboff, lenName, lenOwner, lenWriter;
        String sect;
        
        if (! varLoop.isEmpty()) {
            // set the tab stops for Loops
            sect = "LOOPS";
            lenName = getMaxLength (sect, "NAME");

            taboff = 0;                               // VAR NAME
            taboff += lenName;  tabs.add(taboff);   // OWNER
            taboff += 15;       tabs.add(taboff);   // TYPE
            taboff += 12;       tabs.add(taboff);   // VALUE
            taboff += 8;        tabs.add(taboff);   // START
            taboff += 8;        tabs.add(taboff);   // STOP
            taboff += 8;        tabs.add(taboff);   // STEP
            taboff += 8;        tabs.add(taboff);   // INCL
            taboff += 8;        tabs.add(taboff);   // COMP

            int ix = 0;
            String title = "Variable";
            title = addTabPadding (tabs.get(ix++), title) + "Owner";
            title = addTabPadding (tabs.get(ix++), title) + "Data type";
            title = addTabPadding (tabs.get(ix++), title) + "Value";
            title = addTabPadding (tabs.get(ix++), title) + "Start";
            title = addTabPadding (tabs.get(ix++), title) + "Stop";
            title = addTabPadding (tabs.get(ix++), title) + "Step";
            title = addTabPadding (tabs.get(ix++), title) + "Incl";
            title = addTabPadding (tabs.get(ix++), title) + "Comp";
        
            printType(MessageType.Title, true, "=== LOOPS ===============================================================================================");
            printType(MessageType.Title, true, title);
            printType(MessageType.Title, true, "_________________________________________________________________________________________________________");
            for (int var = 0; var < varLoop.size(); var++) {
                VarAccess varInfo = varLoop.get(var);
                String value = varInfo.getValueString();
                if (value == null || value.isEmpty()) {
                    value = "----";
                }
                ix = 0;
                String line = varInfo.getName();
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getOwner();
                line = addTabPadding (tabs.get(ix++), line) + "Integer";
                line = addTabPadding (tabs.get(ix++), line) + value;
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getStartValue();
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getEndValue();
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getStepValue();
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getIncl();
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getCompSign();
                if (varInfo.isVarChanged()) {
                    printType(MessageType.Changed, true, line);
                } else {
                    printType(MessageType.Normal, true, line);
                }
            }
            printlf();
        }

        if (! varReserved.isEmpty()) {
            // update tab stops and title bar for all other parameters
            sect = "RESERVED";
            lenName   = getMaxLength (sect, "NAME");
            lenOwner  = getMaxLength (sect, "OWNER");
            lenWriter = getMaxLength (sect, "WRITER");

            // set the tab stops for this section
            tabs.clear();
            taboff = 0;                                   // VAR NAME
            taboff += lenName;      tabs.add(taboff);   // OWNER
            taboff += lenOwner;     tabs.add(taboff);   // TYPE
            taboff += 12;           tabs.add(taboff);   // WRITER
            taboff += lenWriter;    tabs.add(taboff);   // LINE
            taboff += 8;            tabs.add(taboff);   // TIME
            taboff += 12;           tabs.add(taboff);   // VALUE

            // setup the header for Loops
            int ix = 0;
            String title = "Variable";
            title = addTabPadding (tabs.get(ix++), title) + "Owner";
            title = addTabPadding (tabs.get(ix++), title) + "Data type";
            title = addTabPadding (tabs.get(ix++), title) + "Writer";
            title = addTabPadding (tabs.get(ix++), title) + "Line";
            title = addTabPadding (tabs.get(ix++), title) + "Time";
            title = addTabPadding (tabs.get(ix++), title) + "Value";
        
            printType(MessageType.Title, true, "=== RESERVED ============================================================================================");
            printType(MessageType.Title, true, title);
            printType(MessageType.Title, true, "_________________________________________________________________________________________________________");
            for (int var = 0; var < varReserved.size(); var++) {
                VarAccess varInfo = varReserved.get(var);
                String value = varInfo.getValueString();
                if (value == null || value.isEmpty()) {
                    value = "----";
                }
                ix = 0;
                String line = varInfo.getName();
                line = addTabPadding (tabs.get(ix++), line) + "----";
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getType().toString();
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getWriter();
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getWriterIndex();
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getWriterTime();
                line = addTabPadding (tabs.get(ix++), line) + value;
                if (varInfo.isVarChanged()) {
                    printType(MessageType.Changed, true, line);
                } else {
                    printType(MessageType.Normal, true, line);
                }
            }
            printlf();
        }

        if (! varGlobal.isEmpty()) {
            // update tab stops and title bar for all other parameters
            sect = "GLOBALS";
            lenName   = getMaxLength (sect, "NAME");
            lenOwner  = getMaxLength (sect, "OWNER");
            lenWriter = getMaxLength (sect, "WRITER");

            // set the tab stops for this section
            tabs.clear();
            taboff = 0;                                   // VAR NAME
            taboff += lenName;      tabs.add(taboff);   // OWNER
            taboff += lenOwner;     tabs.add(taboff);   // TYPE
            taboff += 12;           tabs.add(taboff);   // WRITER
            taboff += lenWriter;    tabs.add(taboff);   // LINE
            taboff += 8;            tabs.add(taboff);   // TIME
            taboff += 12;           tabs.add(taboff);   // VALUE

            // setup the header for Loops
            int ix = 0;
            String title = "Variable";
            title = addTabPadding (tabs.get(ix++), title) + "Owner";
            title = addTabPadding (tabs.get(ix++), title) + "Data type";
            title = addTabPadding (tabs.get(ix++), title) + "Writer";
            title = addTabPadding (tabs.get(ix++), title) + "Line";
            title = addTabPadding (tabs.get(ix++), title) + "Time";
            title = addTabPadding (tabs.get(ix++), title) + "Value";
        
            printType(MessageType.Title, true, "=== GLOBALS =============================================================================================");
            printType(MessageType.Title, true, title);
            printType(MessageType.Title, true, "_________________________________________________________________________________________________________");
            for (int var = 0; var < varGlobal.size(); var++) {
                VarAccess varInfo = varGlobal.get(var);
                String value = varInfo.getValueString();
                if (value == null || value.isEmpty()) {
                    value = "----";
                }
                ix = 0;
                String line = varInfo.getName();
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getOwner();
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getType().toString();
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getWriter();
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getWriterIndex();
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getWriterTime();
                line = addTabPadding (tabs.get(ix++), line) + value;
                if (varInfo.isVarChanged()) {
                    printType(MessageType.Changed, true, line);
                } else {
                    printType(MessageType.Normal, true, line);
                }
            }
            printlf();
        }

        if (! varLocal.isEmpty()) {
            // update tab stops and title bar for all other parameters
            sect = "LOCALS";
            lenName   = getMaxLength (sect, "NAME");
            lenOwner  = getMaxLength (sect, "OWNER");
            lenWriter = getMaxLength (sect, "WRITER");

            // set the tab stops for this section
            tabs.clear();
            taboff = 0;                                   // VAR NAME
            taboff += lenName;      tabs.add(taboff);   // OWNER
            taboff += lenOwner;     tabs.add(taboff);   // TYPE
            taboff += 12;           tabs.add(taboff);   // WRITER
            taboff += lenWriter;    tabs.add(taboff);   // LINE
            taboff += 8;            tabs.add(taboff);   // TIME
            taboff += 12;           tabs.add(taboff);   // VALUE

            // setup the header for Loops
            int ix = 0;
            String title = "Variable";
            title = addTabPadding (tabs.get(ix++), title) + "Owner";
            title = addTabPadding (tabs.get(ix++), title) + "Data type";
            title = addTabPadding (tabs.get(ix++), title) + "Writer";
            title = addTabPadding (tabs.get(ix++), title) + "Line";
            title = addTabPadding (tabs.get(ix++), title) + "Time";
            title = addTabPadding (tabs.get(ix++), title) + "Value";
        
            printType(MessageType.Title, true, "=== LOCALS ==============================================================================================");
            printType(MessageType.Title, true, title);
            printType(MessageType.Title, true, "_________________________________________________________________________________________________________");
            for (int var = 0; var < varLocal.size(); var++) {
                VarAccess varInfo = varLocal.get(var);
                String value = varInfo.getValueString();
                if (value == null || value.isEmpty()) {
                    value = "----";
                }
                ix = 0;
                String line = varInfo.getName();
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getOwner();
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getType().toString();
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getWriter();
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getWriterIndex();
                line = addTabPadding (tabs.get(ix++), line) + varInfo.getWriterTime();
                line = addTabPadding (tabs.get(ix++), line) + value;
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
                        clearVariables();
                        break;
                    case "END":
                        // allocation info complete - now format and print it
                        section = null;
                        print();
                        break;
                    default:
                        section = null;
                        GuiMain.setErrorStatus("ALLOC command - Invalid type " + entry);
                }
                return;
            } else if (message.charAt(0) == '[' && message.charAt(message.length()-1) == ']') {
                message = message.substring(1, message.length()-1);
                var array = new ArrayList<String>(Arrays.asList(message.split(GuiMain.DATA_SEP)));
                allocationEntryMsg (array);
                return;
            }
        }
        GuiMain.setErrorStatus("ALLOC command - Invalid format: " + message);
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
                    GuiMain.setErrorStatus("VARMSG command - invalid entry format: " + entry);
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
                        GuiMain.setErrorStatus("VARMSG command - Invalid key: " + key);
                        return;
                }
            }
            if (name == null) {
                GuiMain.setErrorStatus("VARMSG command - missing <name> entry");
                return;
            }

            // changed variables issue a section name in the entry.
            // allocation omits this and just provides a header preceding the variables
            //  that define the applicable section, which is saved in the global 'section'.
            // if neither are found, we have an error.
            if (value == null || sect == null) {
                // new allocation
                if (section == null) {
                    GuiMain.setErrorStatus("Missing section header");
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
                        GuiMain.setErrorStatus("Invalid section name: " + section);
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
                        GuiMain.setErrorStatus("VARMSG command - Invalid section: " + sect);
                }
                if (! bFound) {
                    GuiMain.setErrorStatus(sect + " variable not found: " + name);
                }
            }
        }
    }

    private static void printType (MessageType type, boolean term, String message) {
        FontInfo fontInfo = fontInfoTbl.get(type);
        if (fontInfo == null) {
            fontInfo = new FontInfo(FontInfo.TextColor.Black,
                                   FontInfo.FontType.Normal, FONT_SIZE, FONT_TYPE);
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
        FontInfo fontinfo = new FontInfo(color, ftype, FONT_SIZE, FONT_TYPE);
        if (fontInfoTbl.containsKey(type)) {
            fontInfoTbl.replace(type, fontinfo);
        }
        else {
            fontInfoTbl.put(type, fontinfo);
        }
    }
    
}
