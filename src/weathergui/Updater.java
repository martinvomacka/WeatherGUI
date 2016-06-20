/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weathergui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vomec
 */
public class Updater {
    private final URL webpage;

    public Updater(String initURL) throws MalformedURLException {
        this.webpage = new URL(initURL);
    }
    
    public boolean checkUpdate() {
        try {
            BufferedReader webpagePlaintext = new BufferedReader(new InputStreamReader(webpage.openStream(), Charset.defaultCharset()));
            String inputLine;
            while ((inputLine = webpagePlaintext.readLine()) != null) {
                if(inputLine.contains("option value="))
                    System.out.println(inputLine);
            }
            webpagePlaintext.close();
        } catch (IOException ex) {
            Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
}
