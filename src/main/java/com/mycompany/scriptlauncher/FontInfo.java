/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.scriptlauncher;

import java.awt.Color;

/**
 *
 * @author dan
 */
public class FontInfo {
    /*
    * This class defines the characteristics of the fonts displayed on the GUI Panes.
    */
    
    public enum FontType {
        Normal, Bold, Italic, BoldItalic;
    }

    public enum TextColor {
        Black, DkGrey, DkRed, Red, LtRed, Orange, Brown, Gold, Green, Cyan,
        LtBlue, Blue, Violet, DkVio;
    }

    public TextColor  color;      // the font color
    public FontType   fonttype;   // the font attributes (e.g. Italics, Bold,..)
    public String     font;       // the font family (e.g. Courier)
    public int        size;       // the font size

    FontInfo (TextColor col, FontType type, int fsize, String fontname) {
        color = col;
        fonttype = type;
        font = fontname;
        size = fsize;
    }
    
    /**
     * generates the specified text color for the debug display.
     * 
     * @param colorName - name of the color to generate
     * @return corresponding Color value representation
     */
    public static Color generateColor (FontInfo.TextColor colorName) {
        float hue, sat, bright;
        switch (colorName) {
            default:
            case Black:
                return Color.BLACK;
            case DkGrey:
                return Color.DARK_GRAY;
            case DkRed:
                hue    = (float)0;
                sat    = (float)100;
                bright = (float)66;
                break;
            case Red:
                hue    = (float)0;
                sat    = (float)100;
                bright = (float)90;
                break;
            case LtRed:
                hue    = (float)0;
                sat    = (float)60;
                bright = (float)100;
                break;
            case Orange:
                hue    = (float)20;
                sat    = (float)100;
                bright = (float)100;
                break;
            case Brown:
                hue    = (float)20;
                sat    = (float)80;
                bright = (float)66;
                break;
            case Gold:
                hue    = (float)40;
                sat    = (float)100;
                bright = (float)90;
                break;
            case Green:
                hue    = (float)128;
                sat    = (float)100;
                bright = (float)45;
                break;
            case Cyan:
                hue    = (float)190;
                sat    = (float)80;
                bright = (float)45;
                break;
            case LtBlue:
                hue    = (float)210;
                sat    = (float)100;
                bright = (float)90;
                break;
            case Blue:
                hue    = (float)240;
                sat    = (float)100;
                bright = (float)100;
                break;
            case Violet:
                hue    = (float)267;
                sat    = (float)100;
                bright = (float)100;
                break;
            case DkVio:
                hue    = (float)267;
                sat    = (float)100;
                bright = (float)66;
                break;
        }
        hue /= (float)360.0;
        sat /= (float)100.0;
        bright /= (float) 100.0;
        return Color.getHSBColor(hue, sat, bright);
    }
    
}
