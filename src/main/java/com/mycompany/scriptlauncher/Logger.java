/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.scriptlauncher;

import java.awt.Graphics;
import java.util.HashMap;
import javax.swing.JTextPane;

/**
 *
 * @author dan
 */
public class Logger {
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
     * outputs the various types of messages to the status display.
     * 
     * format: 00000000 00:00.040 [COMPIL] message text
     * 
     * @param message   - the message contents
     */
    public static final void print(String message) {
        if (textPane != null && message != null && message.length() > 30) {
            // extract pertinant info from the message
            String countstr  = message.substring(0, 8);
            String timestamp = message.substring(9, 18);
            String typestr   = message.substring(19, 27);
            String content   = message.substring(28);

            //  timestamp might be missing on some msgs
            if (message.charAt(11) != ':') {
                timestamp = "         ";
                typestr = message.substring(9, 17);
                content   = message.substring(18);
            }
            
            // font settings for timestamp and data type
            FontInfo prefixFont = new FontInfo(FontInfo.TextColor.Black,
                                              FontInfo.FontType.Normal, FONT_SIZE, FONT_TYPE);
            FontInfo msgFont = fontInfoTbl.get(typestr);

            TextWriter.print(textPane, false, prefixFont, countstr + " ");
            TextWriter.print(textPane, false, prefixFont, timestamp + " ");
            TextWriter.print(textPane, true, msgFont, typestr + ": " + content);
        }
    }

    private static void setColors () {
        if (textPane != null) {
            // these are for public consumption
            setTypeColor ("[ERROR ]", FontInfo.TextColor.Red   , FontInfo.FontType.Bold);
            setTypeColor ("[WARN  ]", FontInfo.TextColor.LtRed , FontInfo.FontType.BoldItalic);
            setTypeColor ("[DEBUG ]", FontInfo.TextColor.Brown , FontInfo.FontType.Normal);
            setTypeColor ("[VARS  ]", FontInfo.TextColor.Blue  , FontInfo.FontType.Normal);
            setTypeColor ("[COMPIL]", FontInfo.TextColor.Green , FontInfo.FontType.Normal);
            setTypeColor ("[PROG  ]", FontInfo.TextColor.DkVio , FontInfo.FontType.Bold);
            setTypeColor ("[PROPS ]", FontInfo.TextColor.Orange, FontInfo.FontType.Normal);
            setTypeColor ("[INFO  ]", FontInfo.TextColor.Violet, FontInfo.FontType.Normal);
            setTypeColor ("[SSHEET]", FontInfo.TextColor.LtBlue, FontInfo.FontType.Normal);
            setTypeColor ("[PARSER]", FontInfo.TextColor.Blue  , FontInfo.FontType.Normal);
            setTypeColor ("[NORMAL]", FontInfo.TextColor.Black , FontInfo.FontType.Normal);
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
