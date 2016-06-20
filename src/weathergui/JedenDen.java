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
public class JedenDen {
    private final String datum;
    private LinkedList<Stanice> seznamstanic;
    
    public JedenDen(String inDate) {
        this.datum = inDate;
        seznamstanic = new LinkedList<>();
    }
    
    /**
     * @return the datum
     */
    public String getDatum() {
        return datum;
    }
    
    public void addStanici(String nazev, float data) {
        Stanice nova = new Stanice();
        nova.setNazev(nazev);
        nova.setSrazky(data);
        seznamstanic.add(nova);
    }

    /**
     * @return the seznamstanic
     */
    public LinkedList<Stanice> getSeznamstanic() {
        return seznamstanic;
    }
    
    public void printDay() {
        int size = seznamstanic.size();
        for (int i = 0; i < size; i++) {
            System.out.println(seznamstanic.get(i).getNazev()+": "+seznamstanic.get(i).getSrazky()+"mm");
        }
    }
}
