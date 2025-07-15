/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.dmcd.scriptlauncher;

import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 *
 * @author dan
 */
public class Script {
    /*
    * This class handles receiving the contents of the Script file and displaying
    *  the contents on the Script Pane.
    */
    
    private static final int    FONT_SIZE = 14;
    private static final String FONT_TYPE = "Courier";
    
    private static JTextPane   textPane = null;
    private static JScrollPane scrollPane = null;
    private static final HashMap<MessageType, FontInfo> fontInfoTbl = new HashMap<>();
    private static Integer curLine = -1;        // current line to execute
    private static Integer breakptLine = -1;    // breakpoint line

    private static ArrayList<String> scriptFile = new ArrayList<>();
    
    private enum MessageType {
        Prefix,         // the line counter value
        CurLine,        // the line of the current line selection
        BreakLine,      // the line of the breakpoint selection
        BreakReached,   // the line of the breakpoint selection when equal to current line
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
     * @param textpane   - the pane to initialize for writing
     * @param scrollpane - the scroll pane it is embedded in
     */
    public static void init (JTextPane textpane, JScrollPane scrollpane) {
        textPane = textpane;
        scrollPane = scrollpane;
        setColors();
    }
    
    /**
     * clears the display.
     */
    private static final void clear() {
        if (textPane != null) {
            textPane.setText("");
        }
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

    public static String getLine (int lineNum) {
        String line = "";
        
        if (lineNum >= 1 && lineNum < scriptFile.size()) {
            line = scriptFile.get(lineNum - 1);
        }
        return lineNum + ": " + line;
    }

    public static void setCurrentLine (Integer line) {
        curLine = line;
    }
    
    public static void setBreakpointLine (Integer line) {
        breakptLine = line;
    }
    
    // re-draws the screen
    public static void refresh() {
        if (textPane != null && scrollPane != null) {
            clear();
            for (int ix = 0; ix < scriptFile.size(); ix++) {
                print(ix + 1, scriptFile.get(ix));
            }
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() { 
                    scrollPane.getVerticalScrollBar().setValue(0);
                }
            });
        }
    }
    
    /**
     * outputs the script file contents to the Script display.
     * 
     * @param file   - the script file contents to display
     */
    public static final void print(File file) {
        // clear the display contents
        scriptFile.clear();
        
        try {
            // read file into array
            FileReader fReader = new FileReader(file);
            BufferedReader fileReader = new BufferedReader(fReader);
            String message;
            int lineNum = 1;
            while ((message = fileReader.readLine()) != null) {
                // save line in array
                scriptFile.add(message);
                
                // output line to display
                print(lineNum, message);
                lineNum++;
            }

            // read file and display contents to Screen Pane
                
            // now send file to AmazonReader
            //TODO:
        } catch (FileNotFoundException exMsg) {
            // TODO: display error
        } catch (IOException exMsg) {
            // TODO: display error
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

    /**
     * outputs the various types of messages to the Script display.
     * 
     * @param linenum   - the message counter
     * @param message   - the message contents
     */
    public static final void print(int linenum, String message) {
        if (textPane != null && message != null) {
            // extract the packet count, elapsed time, and message type from the string
            String countstr = "000" + Integer.toString(linenum);
            countstr = countstr.substring(countstr.length() - 3);
            MessageType curType;
            String msgClone = message.strip();
            
            // font settings for line number
            // set special characteristics for current line and breakpoint line
            MessageType prefix = MessageType.Prefix;
            if (linenum == curLine) {
                if (linenum == breakptLine) {
                    prefix = MessageType.BreakReached;
                } else {
                    prefix = MessageType.CurLine;
                }
            } else if (linenum == breakptLine) {
                prefix = MessageType.BreakLine;
            }
            printType (prefix, msgClone.isEmpty(), countstr + "   ");

            // get 1st non-space char
            if (msgClone.startsWith("#")) {
                printType(MessageType.Comment, true, message);
                return;
            }
            
            // break line into words
            int ix = 0;
            while (! message.isBlank()) {
                String nextWord = getNextWord(message);
                message = message.substring(nextWord.length());
                if (ix == 0) {
                    if (nextWord.startsWith("-")) {
                        curType = MessageType.CmdOption;
                        printType(curType, message.isBlank(), nextWord);
                    } else if (CommandList.isValidCommand(nextWord.strip()) != null) {
                        curType = MessageType.Command;
                        printType(curType, message.isBlank(), nextWord);
                    }else {
                        curType = MessageType.Reference;
                        printType(curType, message.isBlank(), nextWord);
                    }
                } else if (nextWord.strip().startsWith("$")) {
                    curType = MessageType.Reference;
                    printType(curType, message.isBlank(), nextWord);
                } else {
                    curType = MessageType.Normal;
                    printType(curType, message.isBlank(), nextWord);
                }
                ix++;
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
    
    private static void setColors () {
        if (textPane != null) {
            // these are for public consumption
            setTypeColor (MessageType.Prefix      , FontInfo.TextColor.Black , FontInfo.FontType.Normal);
            setTypeColor (MessageType.CurLine     , FontInfo.TextColor.Blue  , FontInfo.FontType.Bold);
            setTypeColor (MessageType.BreakLine   , FontInfo.TextColor.Red   , FontInfo.FontType.Bold);
            setTypeColor (MessageType.BreakReached, FontInfo.TextColor.Violet, FontInfo.FontType.Bold);
            setTypeColor (MessageType.Comment     , FontInfo.TextColor.Green , FontInfo.FontType.Italic);
            setTypeColor (MessageType.Command     , FontInfo.TextColor.Red   , FontInfo.FontType.Normal);
            setTypeColor (MessageType.CmdOption   , FontInfo.TextColor.Orange, FontInfo.FontType.Normal);
            setTypeColor (MessageType.Reference   , FontInfo.TextColor.Violet, FontInfo.FontType.Italic);
            setTypeColor (MessageType.Numeric     , FontInfo.TextColor.Blue  , FontInfo.FontType.Normal);
            setTypeColor (MessageType.Quoted      , FontInfo.TextColor.Blue  , FontInfo.FontType.Normal);
            setTypeColor (MessageType.Normal      , FontInfo.TextColor.Brown , FontInfo.FontType.Normal);
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
