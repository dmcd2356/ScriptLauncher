package com.mycompany.scriptlauncher;


import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author dan
 */
public class TextWriter {
    /*
    * This class defines the utility functions for writing text to a scrollable
    *  Pane on the GUI using various fonts. It handles buffering the data displayed
    *  outside the Pane boundary so that when the pane is scrolled, the text can
    *  be pulled from the buffer to scroll up or down. It has limits defined for
    *  how much buffer space will be used, and will truncate the oldest lines of text.
    *  This is to prevent extremly large amounts of text (such as continuous log files)
    *  from taking a toll on the system resources. This will, of course, prevent
    *  the user from being able to scroll beyond the oldest text that is saved.
    *  This is used by all the handlers of writing to scrollable Panes.
    */
    
    // these are used for limiting the amount of text displayed in the logger display to limit
    // memory use. MAX_TEXT_BUFFER_SIZE defines the upper memory usage when the reduction takes
    // place and REDUCE_BUFFER_SIZE is the minimum number of bytes to reduce it by (it will look
    // for the next NEWLINE char).
    private final static int MAX_TEXT_BUFFER_SIZE = 2000000;
    private final static int REDUCE_BUFFER_SIZE   = 200000;

    private static final String NEWLINE = System.getProperty("line.separator");

    /**
     * displays a message in the debug window.
     * 
     * @param textPane - the panel to write to
     * @param newLine  - true if the text is to be ended with a NEWLINE character.
     * @param fontInfo - the font characteristics for the message
     * @param text     - message contents to display
     */
    public static void print (JTextPane textPane, boolean newLine, FontInfo fontInfo, String text) {
        if (text != null && !text.isEmpty()) {
            if (textPane == null) {
                System.out.print(text);
                return;
            }

            // add newline if 
            if (newLine && !text.contains(TextWriter.NEWLINE)) {
                text = text + TextWriter.NEWLINE;
            }
            
            // set default values (if type was not found)
            FontInfo.TextColor color = FontInfo.TextColor.Black;
            FontInfo.FontType  ftype = FontInfo.FontType.Normal;
            String font = "Courier";
            int size = 11;

            // get the color and font for the specified type
            if (fontInfo != null) {
                color = fontInfo.color;
                ftype = fontInfo.fonttype;
                font  = fontInfo.font;
                size  = fontInfo.size;
            }

            appendToPane(textPane, text, color, font, size, ftype);
        }
    }

    /**
     * A generic function for appending formatted text to a JTextPane.
     * 
     * @param tp    - the TextPane to append to
     * @param msg   - message contents to write
     * @param color - color of text
     * @param font  - the font selection
     * @param size  - the font point size
     * @param ftype - type of font style
     */
    private static void appendToPane(JTextPane textPane, String msg, FontInfo.TextColor color,
                                     String font, int size, FontInfo.FontType ftype) {
        if (textPane == null) {
            return;
        }

        AttributeSet aset = setTextAttr(color, font, size, ftype);
        int len = textPane.getDocument().getLength();

        // trim off earlier data to reduce memory usage if we exceed our bounds
        if (len > MAX_TEXT_BUFFER_SIZE) {
            try {
                int oldlen = len;
                int start = REDUCE_BUFFER_SIZE;
                String text = textPane.getDocument().getText(start, 500);
                int offset = text.indexOf(NEWLINE);
                if (offset >= 0) {
                    start += offset + 1;
                }
                textPane.getDocument().remove(0, start);
                len = textPane.getDocument().getLength();
                System.out.println("Reduced text from " + oldlen + " to " + len);
            } catch (BadLocationException ex) {
                System.out.println(ex.getMessage());
            }
        }

        textPane.setCaretPosition(len);
        textPane.setCharacterAttributes(aset, false);
        textPane.replaceSelection(msg);
    }

    /**
     * A generic function for appending formatted text to a JTextPane.
     * 
     * @param color - color of text
     * @param font  - the font selection
     * @param size  - the font point size
     * @param ftype - type of font style
     * @return the attribute set
     */
    private static AttributeSet setTextAttr(FontInfo.TextColor color, String font, int size, FontInfo.FontType ftype) {
        boolean bItalic = false;
        boolean bBold = false;
        if (ftype == FontInfo.FontType.Italic || ftype == FontInfo.FontType.BoldItalic) {
            bItalic = true;
        }
        if (ftype == FontInfo.FontType.Bold || ftype == FontInfo.FontType.BoldItalic) {
            bBold = true;
        }

        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground,
                                            FontInfo.generateColor(color));

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, font);
        aset = sc.addAttribute(aset, StyleConstants.FontSize, size);
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        aset = sc.addAttribute(aset, StyleConstants.Italic, bItalic);
        aset = sc.addAttribute(aset, StyleConstants.Bold, bBold);
        return aset;
    }

}
