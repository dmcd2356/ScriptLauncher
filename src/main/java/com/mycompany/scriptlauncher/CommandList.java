/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.scriptlauncher;

/**
 *
 * @author dan
 */
public class CommandList {
    /*
    * This class defines the valid commands for the Script so that these terms
    *  can be identified and highlighted when displayed in the Script pane.
    */
    
    public static enum CommandTable {
        EXIT,       // this command is added automatically by the compiler
        STARTUP,    // begining of startup commands that run during pre-compile
        ENDSTARTUP, // end of startup commands
        TESTPATH,   // sets the base path to use for basing relative paths from
        LOGFILE,    // specifies the log file location and characteristics
        RUN,        // this command is for running the command-line commands
        PRINT,      // outputs text to console
        DIRECTORY,  // text file access functions
        FEXISTS,    //  "       "       "
        CD,         //  "       "       "
        MKDIR,      //  "       "       "
        RMDIR,      //  "       "       "
        FDELETE,    //  "       "       "
        FCREATE,    //  "       "       "
        FOPEN,      //  "       "       "
        FCLOSE,     //  "       "       "
        FREAD,      //  "       "       "
        FWRITE,     //  "       "       "
        FGETSIZE,   //  "       "       "
        FGETLINES,  //  "       "       "
        OCRSCAN,    // this does an OCR scan of the specified PDF file
        ALLOCATE,   // this allocates the parameters used by the program
        SET,        // this sets the value of the parameters
        IF,         // these handles the conditional IF-ELSEIF-ELSE-ENDIF
        ELSE,       //  "       "       "
        ELSEIF,     //  "       "       "
        ENDIF,      //  "       "       "
        FOR,        // these handle the FOR loop
        BREAK,      //  "       "       "
        BREAKIF,    //  "       "       "
        SKIP,       //  "       "       "
        SKIPIF,     //  "       "       "
        NEXT,       //  "       "       "
        ENDFOR,     // (not a user command, but inserted by compiler)
        ENDMAIN,    // marks end of MAIN program so subroutines can be defined
        SUB,        // defines the start of a subroutine
        ENDSUB,     // defines the end   of a subroutine
        GOSUB,      // calls a subroutine
        RETURN,     // returns from a subroutine
        INSERT,     // these are Array commands only
        APPEND,     //  "       "       "
        MODIFY,     //  "       "       "
        REMOVE,     //  "       "       "
        TRUNCATE,   //  "       "       "
        POP,        //  "       "       "
        CLEAR,      //  "       "       "
        FILTER,     //  "       "       "
        RESET,      // (not a command, but used as argument for FILTER)
    };

    /**
     * checks if a string is one of the reserved command values
     * 
     * @param strValue - the string to check
     * 
     * @return corresponding enum value
     */
    public static CommandTable isValidCommand (String strValue) {
        for(CommandTable entry : CommandTable.values()){
            if( entry.toString().equals(strValue)){
                return entry;
            }
        }
        return null;
    }

}
