/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.scriptlauncher;

import java.io.IOException;

/**
 *
 * @author dan
 */
public class ScriptLauncher {
    /*
    * This class contains the main() functioni that starts the application.
    */

    private static final String CLASS_NAME = ScriptLauncher.class.getSimpleName();
    
    // port to use if not found in PropertiesFile
    private static final int SERVER_PORT = 6000;

    /**
     * @param args the command line arguments (optional port selection)
     * 
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        PropertiesFile props = new PropertiesFile();
        String strPort = PropertiesFile.getPropertiesItem("Port", Integer.toUnsignedString(SERVER_PORT));
        int port = Integer.parseInt(strPort);
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid option: " + args[0]);
                System.exit(1);
            }
        }

        // start the debug message panel
        GuiMain gui = new GuiMain();
        gui.createDebugPanel(CLASS_NAME, port);
    }
  
}
