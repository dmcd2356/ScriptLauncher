/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.scriptlauncher;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author dan
 */
public class VarAccess {
    
    private boolean     bInit;              // false if variable never written to
    private boolean     bChanged;           // true  if newly modified variable
    private String      varName;            // name of variable
    private VarType     varType;            // the variable data type
    private String      owner;              // owner (name of function that allocated)
    private String      writer;             // subroutine that last wrote to the variable
    private String      writeLine;          // script line number value of last writer to variable
    private String      writeTime;          // timestamp of last write
    private String      strValue;           // value for non-arrays
    private String      start;              // LOOP entry start value
    private String      end;                // LOOP entry end value
    private String      step;               // LOOP entry step value
    private String      incl;               // LOOP entry include end value in loop flag
    private String      comp;               // LOOP entry comparison sign
    private ArrayList<String> strArray;     // array value
        
    public enum VarType {
        Integer,
        Unsigned,
        Boolean,
        String,
        IntArray,
        StrArray,
    }

    // this is called during allocation to define the variable type and access info
    VarAccess (String varName, String varType, String owner) {
        this.bInit     = false;
        this.bChanged  = false;
        this.varName   = varName;
        this.varType   = findVarType(varType);
        this.owner     = owner;  // (only for LOCAL & GLOBAL)

        // these hold who last wrote to the variable and when (only for LOCAL & GLOBAL)
        this.writer    = null;
        this.writeLine = null;
        this.writeTime = null;

        // these will hold the data value
        this.strValue  = null;
        this.strArray  = null;

        // these hold loop-specific entries
        this.start     = null;
        this.end       = null;
        this.step      = null;
        this.incl      = null;
        this.comp      = null;
        
        // init the value of the chosen type
        switch (this.varType) {
            case Integer:
            case Unsigned:
                this.strValue = "0";
                break;
            case Boolean:
                this.strValue = "false";
                break;
            case String:
                this.strValue = "";
                break;
            case StrArray:
            case IntArray:
                this.strArray = new ArrayList<>();
                break;
        }
    }

    public static VarType findVarType (String type) {
        if (type != null) {
            for (VarType entry : VarType.values()) {
                if (entry.toString().contentEquals(type)) {
                    return entry;
                }
            }
        }
        // report invalid type
        GuiMain.setErrorStatus("VarAccess: Invalid variable type: " + type);
        return VarType.String;
    }
    
    public VarType getType () {
        return this.varType;
    }

    public String getName () {
        return this.varName;
    }
        
    public String getValueString () {
        return this.strValue;
    }
        
    public ArrayList<String> getValueStrArray () {
        return this.strArray;
    }

    // indicates if the variable has not been written to since it was allocated
    public boolean isVarInit () {
        return this.bInit;
    }

    // indicates if the variable has not been written to since it was allocated
    public boolean isVarChanged () {
        return this.bChanged;
    }

    // reset the changed flag for next pass
    public void resetVarChanged() {
        this.bChanged = false;
    }

    // re-init the data values & indicate data has changed
    public void resetVarInfo() {
        this.bInit     = false;
        this.bChanged  = true;

        // these hold who last wrote to the variable and when (only for LOCAL & GLOBAL)
        this.writer    = null;
        this.writeLine = null;
        this.writeTime = null;

        // these will hold the data value
        this.strValue  = null;
        this.strArray  = null;

        // these hold loop-specific entries
        this.start     = null;
        this.end       = null;
        this.step      = null;
        this.incl      = null;
        this.comp      = null;
        
        // init the value of the chosen type
        switch (this.varType) {
            case Integer:
            case Unsigned:
                this.strValue = "0";
                break;
            case Boolean:
                this.strValue = "false";
                break;
            case String:
                this.strValue = "";
                break;
            case StrArray:
            case IntArray:
                this.strArray = new ArrayList<>();
                break;
        }
    }
    
    // returns the last writer (MAIN or subroutine) to the variable
    public String getWriter () {
        return (this.writer == null) ? "----" : this.writer;
    }
        
    // returns the line number of the script that was the last writer to the variable
    public String getWriterIndex () {
        return (this.writeLine == null) ? "----" : this.writeLine;
    }

    // returns the timestamp when the last writer wrote to the variable
    public String getWriterTime () {
        return (this.writeTime == null) ? "----" : this.writeTime;
    }
        
    public String getOwner () {
        return this.owner;
    }
        
    public String getStartValue () {
        return this.start;
    }
        
    public String getEndValue () {
        return this.end;
    }
        
    public String getStepValue () {
        return this.step;
    }
        
    public String getIncl () {
        return this.incl;
    }
        
    public String getCompSign () {
        return this.comp;
    }
        
    // these are the functions to set the value of the variable
    public void setValueString (String value, String subName, String line, String time) {
        switch (this.varType) {
            case String:
                break;
            case Integer:
            case Unsigned:
                // if value not integer value, indicate error and exit
                try {
                    Integer.valueOf(value);
                } catch (NumberFormatException exMsg) {
                    GuiMain.setErrorStatus("VarAccess: Variable '" + this.varName + "' value is not valid Integer: " + value);
                    return;
                }
                break;
            case Boolean:
                // if value not boolean value, indicate error and exit
                if (! value.equalsIgnoreCase("TRUE") && ! value.equalsIgnoreCase("FALSE")) {
                    GuiMain.setErrorStatus("VarAccess: Variable '" + this.varName + "' value is not valid Integer: " + value);
                    return;
                }
                break;
            case StrArray:
            case IntArray:
                strArray.clear();
                if (value != null && ! value.isEmpty()) {
                    // if array enclosed in brackets (which it should be) remove them
                    if (value.charAt(0) == '[' && value.charAt(value.length()-1) == ']') {
                        value = value.substring(1, value.length()-1);
                    }
                    ArrayList<String> array = new ArrayList<>(Arrays.asList(value.split(",")));
                    for (int ix = 0; ix < array.size(); ix++) {
                        strArray.add(array.get(ix).strip());
                    }
                }
                value = strArray.toString();
                break;
        }
        this.bChanged  = true;
        this.strValue  = value;
        this.writer    = subName;
        this.writeLine = line;
        this.writeTime = time;
        this.bInit     = true;
    }

    // these are the functions to set the value of the loop variables
    public void setValueLoop (String value, String start, String end, String step, String incl, String comp) {
        try {
            Integer.valueOf(value);
            Integer.valueOf(start);
            Integer.valueOf(end);
            Integer.valueOf(step);
        } catch (NumberFormatException exMsg) {
            GuiMain.setErrorStatus("VarAccess: Variable '" + this.varName + "' contains non-Integer value");
            return;
        }
        this.bChanged  = true;
        this.strValue  = value;
        this.start     = start;
        this.end       = end;
        this.step      = step;
        this.incl      = incl;
        this.comp      = comp;
        this.bInit     = true;
    }

    /**
     * modifies the StrArray or IntArray value.
     * 
     * (This would only be called if the user makes a change to an entry in the array)
     * 
     * To replace an entry at index X, use: setValueArrayEntry (X, value)
     * To append  an entry at the end, use: setValueArrayEntry (X, value) where X >= array length
     * To remove  an entry at index X, use: setValueArrayEntry (X, null)
     * 
     * @param index - the index to change (if less than 0 insert value, if >= max size append value)
     * @param value - the value to replace, append, or remove (null to remove entry)
     */
    public void setValueArrayEntry (int index, String value) {
        if (index < 0) {
            GuiMain.setErrorStatus("VarAccess: negative index value set for parameter: " + this.varName);
            return;
        }
        switch (this.varType) {
            case StrArray:
            case IntArray:
                if (index >= this.strArray.size()) {
                    if (value == null) {
                        this.strArray.removeLast();
                    } else {
                        this.strArray.addLast(value);
                    }
                } else if (value == null) {
                    this.strArray.remove(index);
                } else {
                    this.strArray.set(index, value);
                }
                this.strValue = strArray.toString();
                break;
            default:
                GuiMain.setErrorStatus("VarAccess: setValueArrayEntry() called on non-array parameter: " + this.varType);
                break;
        }
    }
        
    /**
     * inserts a value into the StrArray or IntArray.
     * 
     * @param index - the index to insert the value at (if greater than array size, it will be appended)
     * @param value - the value to insert
     */
    public void insertValueArrayEntry (int index, String value) {
        if (value == null) {
            GuiMain.setErrorStatus("VarAccess: null val set for parameter: " + this.varName);
            return;
        }
        if (index < 0) {
            GuiMain.setErrorStatus("VarAccess: negative index value set for parameter: " + this.varName);
            return;
        }
        switch (this.varType) {
            case StrArray:
            case IntArray:
                if (index >= this.strArray.size()) {
                    this.strArray.addLast(value);
                } else {
                    this.strArray.add(index, value);
                }
                this.strValue = strArray.toString();
                break;
            default:
                GuiMain.setErrorStatus("VarAccess: insertValueArrayEntry() called on non-array parameter: " + this.varType);
                break;
        }
    }
    
}

