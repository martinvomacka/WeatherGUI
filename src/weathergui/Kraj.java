/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weathergui;

import java.util.LinkedList;

/**
 *
 * @author Vomec
 */
public class Kraj {
    private final String nazevKraje;
    private final int IDkraje;
    private int pocetStanic=0;
    private final LinkedList<String> stanice;
    
    public Kraj(String nazev, int id) {
        this.nazevKraje=nazev;
        this.IDkraje=id;
        this.stanice = new LinkedList<>();
    }
    
    public void pridatStanici(String jmeno) {
        if(!findStanice(jmeno)) {
            this.stanice.add(jmeno);
            pocetStanic++;
        }
    }
    
    public boolean findStanice(String jaka) {
        for (int i = 0; i < this.pocetStanic; i++) {
            if(stanice.get(i).contains(jaka))
                return true;
        }
            return false;
    }

    /**
     * @return the nazevKraje
     */
    public String getNazevKraje() {
        return nazevKraje;
    }

    /**
     * @return the pocetStanic
     */
    public int getPocetStanic() {
        return pocetStanic;
    }

    /**
     * @param index
     * @return the stanice
     */
    public String getStanice(int index) {
        return stanice.get(index);
    }

    /**
     * @return the IDkraje
     */
    public int getIDkraje() {
        return IDkraje;
    }
}
