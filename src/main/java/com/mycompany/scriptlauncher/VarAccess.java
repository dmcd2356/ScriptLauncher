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
    
    private String      varName;            // name of variable
    private VarType     varType;            // the variable data type
    private String      owner;              // owner (name of function that allocated)
    private String      writer;             // subroutine that last wrote to the variable
    private String      writeLine;          // script line number value of last writer to variable
    private String      writeTime;          // timestamp of last write
    private String      strValue;           // value for non-arrays
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
        this.varName   = varName;
        this.varType   = findVarType(varType);
        this.owner     = owner;  // (only for LOCAL & GLOBAL)

        // these hold who last wrote to the variable and when (only for LOCAL & GLOBAL)
        this.writer    = null;
        this.writeLine = null;
        this.writeTime = "---";

        // these will hold the data value
        this.strValue  = null;
        this.strArray  = null;

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
        for (VarType entry : VarType.values()) {
            if (entry.toString().contentEquals(type)) {
                return entry;
            }
        }
        // TODO: report invalid type
        return VarType.String;
    }
    
    // these functions get the access info of the variable
    public VarType getType () {
        return this.varType;
    }

    // these functions get the access info of the variable
    public String getName () {
        return this.varName;
    }
        
    // these are the function to get the variable values
    public String getValueString () {
        return this.strValue;
    }
        
    public ArrayList<String> getValueStrArray () {
        return this.strArray;
    }

    // indicates if the variable has not been written to since it was allocated
    public boolean isVarUninit () {
        return this.writer == null;
    }

    // returns the last writer (MAIN or subroutine) to the variable
    public String getWriter () {
        return (this.writer == null) ? "null" : this.writer;
    }
        
    // returns the line number of the script that was the last writer to the variable
    public String getWriterIndex () {
        return (this.writeLine == null) ? "null" : this.writeLine;
    }

    // returns the timestamp when the last writer wrote to the variable
    public String getWriterTime () {
        return this.writeTime;
    }
        
    public String getOwner () {
        return this.owner;
    }
        
    // these are the functions to set the value of the variable
    public void setValueString (String value, String subName, String line, String time) {
        switch (this.varType) {
            case String:
                break;
            case Integer:
            case Unsigned:
                // TODO: if not integer value, indicate error
                break;
            case Boolean:
                // TODO: if not boolean value, indicate error
                break;
            case StrArray:
            case IntArray:
                strArray.clear();
                if (value != null && ! value.isEmpty()) {
                    ArrayList<String> array = new ArrayList<String>(Arrays.asList(value.split(",")));
                    for (int ix = 0; ix < strArray.size(); ix++) {
                        strArray.add(array.get(ix).strip());
                    }
                }
                value = strArray.toString();
                break;
        }
        this.strValue  = value;
        this.writer    = subName;
        this.writeLine = line;
        this.writeTime = time;
    }

    /**
     * modifies the StrArray or IntArray value.
     * 
     * To replace an entry at index X, use: setValueArrayEntry (X, value)
     * To append  an entry at the end, use: setValueArrayEntry (X, value) where X >= array length
     * To remove  an entry at index X, use: setValueArrayEntry (X, null)
     * 
     * @param index - the index to change (if less than 0 insert value, if >= max size append value)
     * @param value - the value to replace, append, or remove (null to remove entry)
     */
    public void setValueArrayEntry (int index, String value) {
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
                // TODO: always indicate error
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
        if (value == null || index < 0) {
            // TODO: indicate error
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
                // TODO: indicate error
                break;
        }
    }
    
}

