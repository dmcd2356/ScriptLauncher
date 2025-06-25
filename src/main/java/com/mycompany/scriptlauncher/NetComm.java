/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.scriptlauncher;

import java.awt.Graphics;
import java.util.HashMap;
import javax.swing.JTextPane;

/**
 *
 * @author dan
 */
public class NetComm {
    
    /*
    * This class handles receiving the Log messages from the network and displaying
    *  the contents on the Log Pane.
    */
    
    private static final int    FONT_SIZE = 11;
    private static final String FONT_TYPE = "Courier";
    
    private static JTextPane textPane = null;
    private static final HashMap<String, FontInfo> fontInfoTbl = new HashMap<>();

    /**
     * initializes the pane info
     * 
     * @param textpane - the pane to initialize for writing
     */
    public static void init (JTextPane textpane) {
        textPane = textpane;
        setColors();
    }
    
    /**
     * clears the display.
     */
    public static final void clear() {
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

    /**
     * outputs the various types of messages to the Output display.
     * 
     * format of message: "type: message"
     * 
     * @param message   - the message contents
     */
    public static final void print(String message) {
        if (textPane != null && message != null) {
            FontInfo msgFont = new FontInfo(FontInfo.TextColor.Black,
                                              FontInfo.FontType.Normal, FONT_SIZE, FONT_TYPE);
            // extract pertinant info from the message
            int offset = message.indexOf(": ");
            if (offset > 0) {
                String typestr = message.substring(0, offset);
                msgFont = fontInfoTbl.get(typestr);
            }

            TextWriter.print(textPane, true, msgFont, message);
        }
    }

    private static void setColors () {
        if (textPane != null) {
            // these are for public consumption
            setTypeColor ("ERROR" , FontInfo.TextColor.Red   , FontInfo.FontType.Bold);
            setTypeColor ("STATUS", FontInfo.TextColor.Black , FontInfo.FontType.Italic);
            setTypeColor ("CLIENT", FontInfo.TextColor.Blue  , FontInfo.FontType.Normal);
            setTypeColor ("SERVER", FontInfo.TextColor.Green , FontInfo.FontType.Normal);
        }
    }
  
    /**
     * sets the association between a type of message and the font characteristics.
     * 
     * @param type  - the type to associate with the font characteristics
     * @param color - the color to assign to the type
     * @param ftype - the font attributes to associate with the type
     */
    private static void setTypeColor (String type, FontInfo.TextColor color, FontInfo.FontType ftype) {
        FontInfo fontinfo = new FontInfo(color, ftype, FONT_SIZE, FONT_TYPE);
        if (fontInfoTbl.containsKey(type)) {
            fontInfoTbl.replace(type, fontinfo);
        }
        else {
            fontInfoTbl.put(type, fontinfo);
        }
    }
    
}
