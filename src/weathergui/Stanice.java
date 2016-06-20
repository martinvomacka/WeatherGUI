/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weathergui;

/**
 *
 * @author Vomec
 */
public class Stanice {
    private String nazev;
    private float srazky;

    /**
     * @return the nazev
     */
    public String getNazev() {
        return nazev;
    }

    /**
     * @return the srazky
     */
    public float getSrazky() {
        return srazky;
    }

    /**
     * @param nazev the nazev to set
     */
    public void setNazev(String nazev) {
        this.nazev = nazev;
    }

    /**
     * @param srazky the srazky to set
     */
    public void setSrazky(float srazky) {
        this.srazky = srazky;
    }
}
