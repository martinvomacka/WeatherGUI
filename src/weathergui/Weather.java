/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weathergui;

import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Callback;

class MyThread extends Thread{
    Weather odkaz;

    public MyThread(Weather tu) {
        this.odkaz=tu;
    }
    @Override
    public void run() {
        odkaz.locked=true;
        odkaz.cKolo.setFill(Color.ORANGE);
        if(odkaz.itemlist.isEmpty())
            return;
        if(!odkaz.dny.isEmpty())
            odkaz.dny.clear();
        LocalDate dateOd = odkaz.dOd.getValue();
        LocalDate dateDo = odkaz.dDo.getValue();
        LocalDate dnes = LocalDate.now();
        //System.out.println(itemlist.size());
        int kolik_zpatky = Period.between(dateOd, dateDo).getDays()+1;
        int kolik_od_dnes = Period.between(dateDo, dnes).getDays();
        if((dateDo.isBefore(dnes) && !dateOd.isAfter(dateDo) || dateDo.isEqual(dnes))) {
            for (int j = 0; j < kolik_zpatky; j++) {
                JedenDen tempden = new JedenDen(odkaz.easy.format(odkaz.cal.getTime()));
                ArrayList<String> templist;
                for (int i = 0; i < 14; i++) {
                    int idkraje = odkaz.kraje.get(i).getIDkraje();
                    templist = odkaz.getKrajList(idkraje);
                    //System.out.println(templist);
                    if(!templist.isEmpty())
                        odkaz.fuck(j-kolik_od_dnes, templist, idkraje, tempden);
                }
                odkaz.dny.add(tempden);
                odkaz.cal.add(Calendar.DATE, -1);
            }
            odkaz.csvExport();
            odkaz.cKolo.setFill(Color.GREEN);
        }
        else
            odkaz.cKolo.setFill(Color.RED);
        
        odkaz.locked=false;
    }
}


/**
 *
 * @author Vomec
 */
public class Weather {
    public static URL webpage;
    public static BufferedReader in;
    public static Calendar cal;
    public static LinkedList<JedenDen> dny;
    public static SimpleDateFormat easy;
    public static LinkedList<Kraj> kraje;
    public ObservableList<String> ob;
    public ArrayList<String> itemlist;
    public boolean locked=false;
    
    public int pocitadloDnu;
    public int pocitadloStanic;
    
    private Application hajzl;
    
    @FXML
    public DatePicker dOd;
    @FXML
    public DatePicker dDo;
    @FXML
    public Circle cKolo;
    @FXML
    public Button bQuit;
    @FXML
    public Hyperlink hEmail;
    @FXML
    public TreeView<String> tSeznam;
    @FXML
    public ListView<String> lVybrane;
    
    public void bPressed(ActionEvent event) {
        if(!locked) {
            Thread t1 = new MyThread(this);
            t1.start();
        }
    }
    
    public ArrayList<String> getKrajList(int krajID) {
        int length = itemlist.size();
        if(length==0)
            return null;
        else {
            int j;
            for (j = 0; j < 14; j++) {
                if(kraje.get(j).getIDkraje()==krajID)
                    break;
            }
            ArrayList<String> outlist = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                if(kraje.get(j).findStanice(itemlist.get(i)))
                    outlist.add(itemlist.get(i));
            }
            return outlist;
        }
    }
    
    public void fuck(int offset_dnu, ArrayList<String> stanice, int krajID, JedenDen ukazatel) {
        //System.out.println(kolik);
        //System.out.println("O1 "+offset_dnu);
        //cal.add(Calendar.DATE, -offset_dnu);
        int offset=0-offset_dnu;
        //System.out.println("O2 "+offset);
        boolean ready=false;
        boolean staniceready=false;
        //System.out.println(easy.format(cal.getTime()));
        try {
            webpage = new URL("http://hydro.chmi.cz/hpps/hpps_act_rain.php?day_offset="+String.valueOf(offset)+"&fkraj="+krajID);
            in = new BufferedReader(new InputStreamReader(webpage.openStream(), "CP1250"));
            String inputLine;
            String stanicename="";
            String hodnota="";
            int i=0;
            while ((inputLine = in.readLine()) != null) {
                if(inputLine.contains(";return false;\"")) {
                    if(inputLine.contains("<img src"))
                        continue;
                    //System.out.println(inputLine);
                    stanicename = inputLine.substring(inputLine.indexOf(";\">")+3);
                    stanicename = stanicename.substring(0,stanicename.indexOf("</a>")).trim();
                    //System.out.println("Stanice: "+stanicename);
                    //System.out.println(inputLine);
                    if(stanice.contains(stanicename)) {
                        staniceready=true;
                        //System.out.println("jo");
                        continue;
                    }
                    else {
                        staniceready=false;
                        continue;
                    }

                }
                if(inputLine.contains("sdt") && staniceready){
                    i++;
                    if(i==2){
                        hodnota = inputLine.substring(16);
                        hodnota = hodnota.substring(0,hodnota.indexOf("<"));
                        System.out.println("24h: "+hodnota);
                        i=0;
                        ready=true;
                    }
                    //System.out.println(inputLine);
                }
                if(ready) {
                    ukazatel.addStanici(stanicename, Float.parseFloat(hodnota));
                    ready=false;
                    staniceready=false;
                }
            }
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(Weather.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*
        for (int i = dny.size(); i > 0; i--) {
            System.out.println(dny.get(i-1).getDatum());
            dny.get(i-1).printDay();
            System.out.println();
        }
        */
    }
    
    public void csvExport() {
        try
	{
            File file = new File("export.csv");
            if (file.exists()){
                file.delete();
            }
	    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file),"CP1250");
            writer.append("Datum;");
            int daycount=dny.size();
            LinkedList<Stanice> temp = dny.getFirst().getSeznamstanic();
            for (int i = 1; i < daycount; i++) {
                temp = dny.get(i).getSeznamstanic();
                if(temp.size()!=0)
                    break;
            }
            for (Stanice temp1 : temp) {
                writer.append(temp1.getNazev() + ";");
            }
	    writer.append('\n');
            for (int i = dny.size(); i > 0; i--) {
                writer.append(dny.get(i-1).getDatum()+";");
                temp = dny.get(i-1).getSeznamstanic();
                for (int j=0; j<temp.size();j++) {
                    //System.out.println(temp.get(j).getNazev()+" "+temp.get(j).getSrazky());
                    writer.append(String.valueOf(temp.get(j).getSrazky()).replace('.', ',')+ ";");
                    
                }
                writer.append('\n');
            }
			
	    //generate whatever data you want
			
	    writer.flush();
	    writer.close();
	}
	catch(IOException e)
	{
	     e.printStackTrace();
	} 
    }
    
    public void setHajzl(Application in) {
        this.hajzl=in;
    }
    
    public void initialize() {
        easy = new SimpleDateFormat("dd.MM.yyyy");
        cal = Calendar.getInstance();
        kraje = new LinkedList<>();
        itemlist = new ArrayList<>();
        dny=new LinkedList<>();
        pocitadloDnu=0;
        pocitadloStanic=0;
        final Callback<DatePicker, DateCell> dayCellFactory = 
            new Callback<DatePicker, DateCell>() {
                @Override
                public DateCell call(final DatePicker datePicker) {
                    return new DateCell() {
                        @Override
                        public void updateItem(LocalDate item, boolean empty) {
                            super.updateItem(item, empty);
                           
                            if (item.isBefore(dOd.getValue())
                                ) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;");
                            }
                            if (item.isAfter(LocalDate.now())
                                ) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;");
                            } 
                    }
                };
            }
        };
        final Callback<DatePicker, DateCell> day2CellFactory = 
            new Callback<DatePicker, DateCell>() {
                @Override
                public DateCell call(final DatePicker datePicker) {
                    return new DateCell() {
                        @Override
                        public void updateItem(LocalDate item, boolean empty) {
                            super.updateItem(item, empty);
                           
                            if (item.isBefore(LocalDate.now().minusDays(7))
                                ) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;");
                            }
                            if (item.isAfter(LocalDate.now())
                                ) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;");
                            } 
                    }
                };
            }
        };
        dOd.setDayCellFactory(day2CellFactory);
        dOd.setValue(LocalDate.now().minusDays(7));
        dDo.setDayCellFactory(dayCellFactory);
        dDo.setValue(LocalDate.now());
        initKraje();
        loadTreeItems();
    }  
    
    private void initKraje() {
        kraje.add(new Kraj("Hl. město Praha", 4));
        kraje.get(0).pridatStanici("Praha, Běchovice (HMP)");   
        kraje.get(0).pridatStanici("Praha, Břevnov");  
        kraje.get(0).pridatStanici("Praha, Břevnov (HMP)");  
        kraje.get(0).pridatStanici("Praha, Bubeneč (HMP)");  
        kraje.get(0).pridatStanici("Praha, Ďáblice (HMP)");  
        kraje.get(0).pridatStanici("Praha, Dubeč (HMP)"); 
        kraje.get(0).pridatStanici("Praha, Hlubočepy (HMP)");  
        kraje.get(0).pridatStanici("Praha, Horní Měcholupy (HMP)");  
        kraje.get(0).pridatStanici("Praha, Horní Počernice (HMP)");  
        kraje.get(0).pridatStanici("Praha, Chodov (HMP)");  
        kraje.get(0).pridatStanici("Praha, Jinonice (HMP)");  
        kraje.get(0).pridatStanici("Praha, Karlov");  
        kraje.get(0).pridatStanici("Praha, Karlov (HMP)");
        kraje.get(0).pridatStanici("Praha, Kbely");
        kraje.get(0).pridatStanici("Praha, Klementinum");  
        kraje.get(0).pridatStanici("Praha, Kyje (HMP)");  
        kraje.get(0).pridatStanici("Praha, Libuš");  
        kraje.get(0).pridatStanici("Praha, Michle (HMP)");  
        kraje.get(0).pridatStanici("Praha, Modřany sever II (HMP)");  
        kraje.get(0).pridatStanici("Praha, Radotín (HMP)");  
        kraje.get(0).pridatStanici("Praha, Ruzyně");  
        kraje.get(0).pridatStanici("Praha, Řepy (HMP)");  
        kraje.get(0).pridatStanici("Praha, Stodůlky (HMP)");  
        kraje.get(0).pridatStanici("Praha, Střešovice (HMP)"); 
        kraje.get(0).pridatStanici("Praha, Vinohrady (HMP)"); 
        kraje.get(0).pridatStanici("Praha, Žižkov (HMP)"); 
        
        kraje.add(new Kraj("Jihočeský", 3561));  
        kraje.get(1).pridatStanici("Bechyně"); 
        kraje.get(1).pridatStanici("Bernartice"); 
        kraje.get(1).pridatStanici("Borkovice"); 
        kraje.get(1).pridatStanici("Borová Lada"); 
        kraje.get(1).pridatStanici("Byňov"); 
        kraje.get(1).pridatStanici("Černá v Pošumaví"); 
        kraje.get(1).pridatStanici("České Budějovice, pobočka ČHMÚ"); 
        kraje.get(1).pridatStanici("Český Krumlov - Přísečná"); 
        kraje.get(1).pridatStanici("Český Rudolec");  
        kraje.get(1).pridatStanici("Hlasivo");
        kraje.get(1).pridatStanici("Hluboká nad Vltavou"); 
        kraje.get(1).pridatStanici("Husinec");   
        kraje.get(1).pridatStanici("Chelčice");
        kraje.get(1).pridatStanici("Chlum u Třeboně"); 
        kraje.get(1).pridatStanici("Churáňov");  
        kraje.get(1).pridatStanici("Jelení, Nová Pec"); 
        kraje.get(1).pridatStanici("Jindřichův Hradec - Děbolín"); 
        kraje.get(1).pridatStanici("Kestřany"); 
        kraje.get(1).pridatStanici("Kocelovice"); 
        kraje.get(1).pridatStanici("Křemže, Mříč"); 
        kraje.get(1).pridatStanici("Lenora - Houžná"); 
        kraje.get(1).pridatStanici("Mladá Vožice"); 
        kraje.get(1).pridatStanici("Nadějkov - Větrov"); 
        kraje.get(1).pridatStanici("Netřebices"); 
        kraje.get(1).pridatStanici("Nová Bystřice");  
        kraje.get(1).pridatStanici("Orlík nad Vltavou"); 
        kraje.get(1).pridatStanici("Paseky");   
        kraje.get(1).pridatStanici("Plechý");
        kraje.get(1).pridatStanici("Pohorská Ves - Terčí Dvůr"); 
        kraje.get(1).pridatStanici("Sedlice"); 
        kraje.get(1).pridatStanici("Staré Hutě (okr. Č.Budějovice)"); 
        kraje.get(1).pridatStanici("Strakonice"); 
        kraje.get(1).pridatStanici("Strmilov"); 
        kraje.get(1).pridatStanici("Tábor - Náchod"); 
        kraje.get(1).pridatStanici("Temelín"); 
        kraje.get(1).pridatStanici("Tisovka - Ktiš"); 
        kraje.get(1).pridatStanici("Trhové Sviny"); 
        kraje.get(1).pridatStanici("Třeboň - Lužnice"); 
        kraje.get(1).pridatStanici("Vimperk"); 
        kraje.get(1).pridatStanici("Volyně - Nihošovice"); 
        kraje.get(1).pridatStanici("Vráž u P."); 
        kraje.get(1).pridatStanici("Vyšší Brod"); 
        
        kraje.add(new Kraj("Jihomoravský", 15679));
        kraje.get(2).pridatStanici("Brno-Tuřany");
        kraje.get(2).pridatStanici("Brno-Žabovřesky");
        kraje.get(2).pridatStanici("Brod nad Dyjí");
        kraje.get(2).pridatStanici("Bukovinka");
        kraje.get(2).pridatStanici("Dolní Věstonice");
        kraje.get(2).pridatStanici("Dyjákovice");
        kraje.get(2).pridatStanici("Ivanovice na Hané");
        kraje.get(2).pridatStanici("Kobylí");
        kraje.get(2).pridatStanici("Kuchařovice");
        kraje.get(2).pridatStanici("Lednice");
        kraje.get(2).pridatStanici("Nemochovice");
        kraje.get(2).pridatStanici("Obora");
        kraje.get(2).pridatStanici("Olešnice");
        kraje.get(2).pridatStanici("Pohořelice");
        kraje.get(2).pridatStanici("Strážnice");
        kraje.get(2).pridatStanici("Tišnov, Hájek");
        kraje.get(2).pridatStanici("Troubsko");
        kraje.get(2).pridatStanici("Ždánice");

        kraje.add(new Kraj("Karlovarský", 7823));
        kraje.get(3).pridatStanici("Abertamy");
        kraje.get(3).pridatStanici("Aš");
        kraje.get(3).pridatStanici("Bečov nad Teplou");
        kraje.get(3).pridatStanici("Dyleň");
        kraje.get(3).pridatStanici("Cheb");
        kraje.get(3).pridatStanici("Karlovy Vary");
        kraje.get(3).pridatStanici("Klínovec");
        kraje.get(3).pridatStanici("Krásné Údolí");  
        kraje.get(3).pridatStanici("Luby");
        kraje.get(3).pridatStanici("Mariánské Lázně - vodárna");  
        kraje.get(3).pridatStanici("Přebuz");  
        kraje.get(3).pridatStanici("Sokolov");
        kraje.get(3).pridatStanici("Stráž nad Ohří");
        kraje.get(3).pridatStanici("Šindelová (Obora)");
        kraje.get(3).pridatStanici("Žlutice");

        kraje.add(new Kraj("Královéhradecký", 10838));
        kraje.get(4).pridatStanici("Adršpach - Horní Adršpach");
        kraje.get(4).pridatStanici("Borohrádek");
        kraje.get(4).pridatStanici("Broumov");
        kraje.get(4).pridatStanici("Černý Důl");
        kraje.get(4).pridatStanici("České Meziříčí");
        kraje.get(4).pridatStanici("Deštné v Orlic. horách");
        kraje.get(4).pridatStanici("Holovousy");
        kraje.get(4).pridatStanici("Horní Maršov");
        kraje.get(4).pridatStanici("Hradec Králové");
        kraje.get(4).pridatStanici("Jičín");
        kraje.get(4).pridatStanici("Labská bouda");
        kraje.get(4).pridatStanici("Lázně Bělohrad");
        kraje.get(4).pridatStanici("Luční bouda");
        kraje.get(4).pridatStanici("Luisino údolí, Deštné v Orl. horách");
        kraje.get(4).pridatStanici("Nový Bydžov");
        kraje.get(4).pridatStanici("Olešnice v Orl. horách");
        kraje.get(4).pridatStanici("Pec pod Sněžkou");
        kraje.get(4).pridatStanici("Police nad Metují");
        kraje.get(4).pridatStanici("Pomezní boudy, Horní Malá Úpa");
        kraje.get(4).pridatStanici("Rokytnice v Orlic.horách");
        kraje.get(4).pridatStanici("Rychnov nad Kněžnou");
        kraje.get(4).pridatStanici("Slatina nad Zdobnicí");
        kraje.get(4).pridatStanici("Slatiny, Milíčeves");
        kraje.get(4).pridatStanici("Svobodné Dvory");
        kraje.get(4).pridatStanici("Trutnov, ZŠ Komenského");
        kraje.get(4).pridatStanici("Úpice");
        kraje.get(4).pridatStanici("Velichovky");
        kraje.get(4).pridatStanici("Vrchlabí");
        kraje.get(4).pridatStanici("Zdobnice");
        
        kraje.add(new Kraj("Liberecký", 10077));
        kraje.get(5).pridatStanici("Bedřichov - přehrada");
        kraje.get(5).pridatStanici("Česká Lípa");
        kraje.get(5).pridatStanici("Český Dub, Modlibohov");
        kraje.get(5).pridatStanici("Desná, Souš");
        kraje.get(5).pridatStanici("Doksy");
        kraje.get(5).pridatStanici("Dvoračky");
        kraje.get(5).pridatStanici("Frýdlant");
        kraje.get(5).pridatStanici("Harrachov");
        kraje.get(5).pridatStanici("Hejnice");
        kraje.get(5).pridatStanici("Holenice");
        kraje.get(5).pridatStanici("Chrastava");
        kraje.get(5).pridatStanici("Jablonec nad Nisou");
        kraje.get(5).pridatStanici("Jablonné v Podještědí");
        kraje.get(5).pridatStanici("Jizerka - klimatická stanice");
        kraje.get(5).pridatStanici("Josefův Důl");
        kraje.get(5).pridatStanici("Křižany");
        kraje.get(5).pridatStanici("Liberec");
        kraje.get(5).pridatStanici("Lomnice nad Popelkou");
        kraje.get(5).pridatStanici("Mařenice");
        kraje.get(5).pridatStanici("Mimoň");
        kraje.get(5).pridatStanici("Nová Louka");
        kraje.get(5).pridatStanici("Nové Město pod Smrkem");
        kraje.get(5).pridatStanici("Nový Bor");
        kraje.get(5).pridatStanici("Smědava - klimatická stanice");
        kraje.get(5).pridatStanici("Stráž pod Ralskem");
        kraje.get(5).pridatStanici("Studenec");  
        kraje.get(5).pridatStanici("Turnov");
        kraje.get(5).pridatStanici("Višňová");
        kraje.get(5).pridatStanici("Vysoké nad Jizerou");
        kraje.get(5).pridatStanici("Zahrádky");

        kraje.add(new Kraj("Moravskoslezský", 19268));
        kraje.get(6).pridatStanici("Bělotín");
        kraje.get(6).pridatStanici("Bílá, Konečná");
        kraje.get(6).pridatStanici("Bohumín, Záblatí");
        kraje.get(6).pridatStanici("Červená u L.");
        kraje.get(6).pridatStanici("Frenštát pod Radhoštěm");  
        kraje.get(6).pridatStanici("Frýdek-Místek, Místek");
        kraje.get(6).pridatStanici("Hladké Životice");
        kraje.get(6).pridatStanici("Jablunkov, Návsí");
        kraje.get(6).pridatStanici("Karlova Studánka");
        kraje.get(6).pridatStanici("Karviná, Staré Město");
        kraje.get(6).pridatStanici("Krnov");
        kraje.get(6).pridatStanici("Lichnov");
        kraje.get(6).pridatStanici("Lomnice");
        kraje.get(6).pridatStanici("Lučina");
        kraje.get(6).pridatStanici("Lysá hora");
        kraje.get(6).pridatStanici("Město Albrechtice, Žáry");
        kraje.get(6).pridatStanici("Mořkov");
        kraje.get(6).pridatStanici("Mošnov");
        kraje.get(6).pridatStanici("Opava, Otice");
        kraje.get(6).pridatStanici("Osoblaha");
        kraje.get(6).pridatStanici("Ostrava-Poruba");
        kraje.get(6).pridatStanici("Ropice");
        kraje.get(6).pridatStanici("Rýmařov");
        kraje.get(6).pridatStanici("Slezská Harta");
        kraje.get(6).pridatStanici("Slezská Ostrava");
        kraje.get(6).pridatStanici("Světlá Hora");
        kraje.get(6).pridatStanici("Vítkov");

        kraje.add(new Kraj("Olomoucký", 17276));
        kraje.get(7).pridatStanici("Bělá pod Pradědem, Filipovice");
        kraje.get(7).pridatStanici("Dřevohostice");
        kraje.get(7).pridatStanici("Dubicko");
        kraje.get(7).pridatStanici("Hanušovice");
        kraje.get(7).pridatStanici("Hoštejn");
        kraje.get(7).pridatStanici("Javorník");
        kraje.get(7).pridatStanici("Jeseník");
        kraje.get(7).pridatStanici("Kozlov");
        kraje.get(7).pridatStanici("Luká");
        kraje.get(7).pridatStanici("Olomouc-Holice");
        kraje.get(7).pridatStanici("Paprsek");
        kraje.get(7).pridatStanici("Paseka");
        kraje.get(7).pridatStanici("Protivanov");
        kraje.get(7).pridatStanici("Přerov");
        kraje.get(7).pridatStanici("Šerák");
        kraje.get(7).pridatStanici("Šternberk");
        kraje.get(7).pridatStanici("Šumperk");
        kraje.get(7).pridatStanici("Velké Losiny");
        kraje.get(7).pridatStanici("Zlaté Hory");

        kraje.add(new Kraj("Pardubický", 12303));
        kraje.get(8).pridatStanici("Červená Voda");
        kraje.get(8).pridatStanici("Dolní Morava, Slaměnka");
        kraje.get(8).pridatStanici("Hradec nad Svitavou");
        kraje.get(8).pridatStanici("Hrochův Týnec");
        kraje.get(8).pridatStanici("Hrušová");
        kraje.get(8).pridatStanici("Choceň");
        kraje.get(8).pridatStanici("Janov-Gajer");
        kraje.get(8).pridatStanici("Jevíčko");
        kraje.get(8).pridatStanici("Králíky");
        kraje.get(8).pridatStanici("Lanškroun");
        kraje.get(8).pridatStanici("Lubná");
        kraje.get(8).pridatStanici("Mokošín");
        kraje.get(8).pridatStanici("Nedvězí");
        kraje.get(8).pridatStanici("Nové Hrady");
        kraje.get(8).pridatStanici("Seč");
        kraje.get(8).pridatStanici("Skuteč");
        kraje.get(8).pridatStanici("Svratouch");
        kraje.get(8).pridatStanici("Třebařov");
        kraje.get(8).pridatStanici("Ústí n. O.");
        kraje.get(8).pridatStanici("Žamberk");

        kraje.add(new Kraj("Plzeňský", 5865));
        kraje.get(9).pridatStanici("Bezvěrov");
        kraje.get(9).pridatStanici("Borovno, Míšov");
        kraje.get(9).pridatStanici("Čachrov");
        kraje.get(9).pridatStanici("Česká Kubice");
        kraje.get(9).pridatStanici("Domažlice");
        kraje.get(9).pridatStanici("Filipova Huť");
        kraje.get(9).pridatStanici("Horšovský Týn");
        kraje.get(9).pridatStanici("Chanovice");
        kraje.get(9).pridatStanici("Chudenice");
        kraje.get(9).pridatStanici("Kašperské Hory");
        kraje.get(9).pridatStanici("Klatovy");
        kraje.get(9).pridatStanici("Konstantinovy Lázně");
        kraje.get(9).pridatStanici("Kralovice");
        kraje.get(9).pridatStanici("Liblín");
        kraje.get(9).pridatStanici("Lovčice, Kvasetice");
        kraje.get(9).pridatStanici("Nepomuk");
        kraje.get(9).pridatStanici("Nezvěstice");
        kraje.get(9).pridatStanici("Planá");
        kraje.get(9).pridatStanici("Plzeň - Mikulka");
        kraje.get(9).pridatStanici("Plzeň-Bolevec");
        kraje.get(9).pridatStanici("Prášily");
        kraje.get(9).pridatStanici("Přimda");
        kraje.get(9).pridatStanici("Radošice");
        kraje.get(9).pridatStanici("Rokycany");
        kraje.get(9).pridatStanici("Staňkov");
        kraje.get(9).pridatStanici("Staré Sedlo, Darmyšl");
        kraje.get(9).pridatStanici("Stod");
        kraje.get(9).pridatStanici("Strašice");
        kraje.get(9).pridatStanici("Strašín");
        kraje.get(9).pridatStanici("Stříbro");
        kraje.get(9).pridatStanici("Tachov");
        kraje.get(9).pridatStanici("Terešov");
        kraje.get(9).pridatStanici("Trpísty");
        kraje.get(9).pridatStanici("Úlice");
        kraje.get(9).pridatStanici("Vlkonice");
        kraje.get(9).pridatStanici("Zámyšl");
        kraje.get(9).pridatStanici("Železná Ruda");
        kraje.get(9).pridatStanici("Železná Ruda - Hojsova Stráž");

        kraje.add(new Kraj("Středočeský", 222));
        kraje.get(10).pridatStanici("Bahno");
        kraje.get(10).pridatStanici("Boseň, Mužský");
        kraje.get(10).pridatStanici("Brandýs nad Labem");
        kraje.get(10).pridatStanici("Březnice");
        kraje.get(10).pridatStanici("Čáslav, Nové město");
        kraje.get(10).pridatStanici("Dobřichovice");
        kraje.get(10).pridatStanici("Dymokury");
        kraje.get(10).pridatStanici("Heřmanov");
        kraje.get(10).pridatStanici("Horoměřice (HMP)");
        kraje.get(10).pridatStanici("Hulice");
        kraje.get(10).pridatStanici("Husinec, Řež");
        kraje.get(10).pridatStanici("Hvozdec, Mrtník");
        kraje.get(10).pridatStanici("Kamýk");
        kraje.get(10).pridatStanici("Káraný");
        kraje.get(10).pridatStanici("Karlova Ves");
        kraje.get(10).pridatStanici("Konárovice");
        kraje.get(10).pridatStanici("Kounov");
        kraje.get(10).pridatStanici("Kralupy nad Vltavou");
        kraje.get(10).pridatStanici("Lány");
        kraje.get(10).pridatStanici("Mrzky");
        kraje.get(10).pridatStanici("Mšeno");
        kraje.get(10).pridatStanici("Nedrahovice, Rudolec");
        kraje.get(10).pridatStanici("Netvořice");
        kraje.get(10).pridatStanici("Neumětely");
        kraje.get(10).pridatStanici("Ondřejov");
        kraje.get(10).pridatStanici("Poděbrady");
        kraje.get(10).pridatStanici("Průhonice");
        kraje.get(10).pridatStanici("Příbram");
        kraje.get(10).pridatStanici("Radovesnice II");
        kraje.get(10).pridatStanici("Rožmitál pod Třemšínem");
        kraje.get(10).pridatStanici("Semčice");
        kraje.get(10).pridatStanici("Slaný");
        kraje.get(10).pridatStanici("Svatý Jan");
        kraje.get(10).pridatStanici("Tuhaň");
        kraje.get(10).pridatStanici("Vavřinec, Žíšov");
        kraje.get(10).pridatStanici("Veleň, PČOV Miškovice (HMP)");
        kraje.get(10).pridatStanici("Vlašim");
        kraje.get(10).pridatStanici("Voznice");
        kraje.get(10).pridatStanici("Zlonice");

        kraje.add(new Kraj("Ústecký", 8607));
        kraje.get(11).pridatStanici("Děčín");
        kraje.get(11).pridatStanici("Doksany");
        kraje.get(11).pridatStanici("Hrob, Křižanov");
        kraje.get(11).pridatStanici("Klíny");
        kraje.get(11).pridatStanici("Kopisty");
        kraje.get(11).pridatStanici("Mašťov");
        kraje.get(11).pridatStanici("Měděnec");
        kraje.get(11).pridatStanici("Milešov");
        kraje.get(11).pridatStanici("Milešovka");
        kraje.get(11).pridatStanici("Nová Ves v Horách");
        kraje.get(11).pridatStanici("Smolnice");
        kraje.get(11).pridatStanici("Sněžník");
        kraje.get(11).pridatStanici("Straškov-Vodochody");
        kraje.get(11).pridatStanici("Strojetice");
        kraje.get(11).pridatStanici("Šluknov");
        kraje.get(11).pridatStanici("Teplice");
        kraje.get(11).pridatStanici("Tisá");
        kraje.get(11).pridatStanici("Tokáň");
        kraje.get(11).pridatStanici("Tušimice");
        kraje.get(11).pridatStanici("Ústí n. L.-Kočkov");
        kraje.get(11).pridatStanici("Ústí nad Labem, Vaňov");
        kraje.get(11).pridatStanici("Varnsdorf");
        kraje.get(11).pridatStanici("Verneřice");
        kraje.get(11).pridatStanici("Žatec");

        kraje.add(new Kraj("Vysočina", 13600));
        kraje.get(12).pridatStanici("Brtnice");
        kraje.get(12).pridatStanici("Bystřice nad Pernštejnem");
        kraje.get(12).pridatStanici("Černovice - Dobešov");
        kraje.get(12).pridatStanici("Dukovany");
        kraje.get(12).pridatStanici("Havlíčkův Brod");
        kraje.get(12).pridatStanici("Hubenov");
        kraje.get(12).pridatStanici("Humpolec");
        kraje.get(12).pridatStanici("Jemnice");
        kraje.get(12).pridatStanici("Jihlava, Hruškové Dvory");
        kraje.get(12).pridatStanici("Kadov");
        kraje.get(12).pridatStanici("Kamenice nad Lipou - Vodná");
        kraje.get(12).pridatStanici("Kostelní Myslová");
        kraje.get(12).pridatStanici("Košetice");
        kraje.get(12).pridatStanici("Libice nad Doubravou");
        kraje.get(12).pridatStanici("Moravské Budějovice");
        kraje.get(12).pridatStanici("Náměšť nad Oslavou");
        kraje.get(12).pridatStanici("Nová Ves");
        kraje.get(12).pridatStanici("Nové Město na Moravě");
        kraje.get(12).pridatStanici("Nový Rychnov");
        kraje.get(12).pridatStanici("Přibyslav");
        kraje.get(12).pridatStanici("Radostín");
        kraje.get(12).pridatStanici("Sedlec");
        kraje.get(12).pridatStanici("Stržanov");
        kraje.get(12).pridatStanici("Štoky");
        kraje.get(12).pridatStanici("Třebíč");
        kraje.get(12).pridatStanici("Třešť");
        kraje.get(12).pridatStanici("Vatín");
        kraje.get(12).pridatStanici("Velká Bíteš");
        kraje.get(12).pridatStanici("Velké Meziříčí");
        kraje.get(12).pridatStanici("Vysoké Studnice");

        kraje.add(new Kraj("Zlínský", 18481));
        kraje.get(13).pridatStanici("Bojkovice");
        kraje.get(13).pridatStanici("Bystřice pod Hostýnem");
        kraje.get(13).pridatStanici("Hluk");
        kraje.get(13).pridatStanici("Holešov");
        kraje.get(13).pridatStanici("Horní Bečva");
        kraje.get(13).pridatStanici("Horní Lhota");
        kraje.get(13).pridatStanici("Hošťálková");
        kraje.get(13).pridatStanici("Huslenky");
        kraje.get(13).pridatStanici("Kateřinice");
        kraje.get(13).pridatStanici("Kroměříž");
        kraje.get(13).pridatStanici("Luhačovice, Kladná-Žilín");
        kraje.get(13).pridatStanici("Maruška");
        kraje.get(13).pridatStanici("Staré Hutě (okr. Uh.Hradiště)");
        kraje.get(13).pridatStanici("Staré Město");
        kraje.get(13).pridatStanici("Strání");
        kraje.get(13).pridatStanici("Štítná nad Vláří - Popov");
        kraje.get(13).pridatStanici("Valašská Bystřice");
        kraje.get(13).pridatStanici("Valašská Senice");
        kraje.get(13).pridatStanici("Valašské Meziříčí");
        kraje.get(13).pridatStanici("Vizovice");
        kraje.get(13).pridatStanici("Vsetín");
        
        /*
        int sum = 0;
        for (int i = 0; i < 14; i++) {
            sum+=kraje.get(i).getPocetStanic();
        }
        System.out.println("Pocet stanic celkem: "+sum);
        */
    }

    private void loadTreeItems() {
        TreeItem<String> root = new TreeItem<>("Kraje");
        root.setExpanded(true);

        tSeznam.setRoot(root);
        int size = kraje.size();
        int sizeKraj;
        for (int i = 0; i < size; i++) {
            TreeItem<String> temp = new TreeItem<>(kraje.get(i).getNazevKraje());
            sizeKraj=kraje.get(i).getPocetStanic();
            for(int j=0; j<sizeKraj;j++)
                temp.getChildren().add(new TreeItem<>(kraje.get(i).getStanice(j)));
            temp.setExpanded(false);
            root.getChildren().add(temp);
        }
    }
    
    private void addAllFromKraj(int index) {
        Kraj temp = kraje.get(index);
        int size = temp.getPocetStanic();
        String jmeno;
        for (int i = 0; i < size; i++) {
            jmeno=temp.getStanice(i);
            if(!itemlist.contains(jmeno))
                itemlist.add(jmeno);
        }
    }
    
    public void bPridatPressed(ActionEvent event) {
        if(tSeznam.getSelectionModel().selectedItemProperty().getValue().getValue()!=null) {
            boolean sub=false;
            String what = tSeznam.getSelectionModel().selectedItemProperty().getValue().getValue();
            for (int i = 0; i < 14; i++) {
                if(kraje.get(i).getNazevKraje().contains(what)) {
                    addAllFromKraj(i);
                    sub=true;
                }
            }
            if(!itemlist.contains(tSeznam.getSelectionModel().selectedItemProperty().getValue().getValue())) {
                itemlist.add(tSeznam.getSelectionModel().selectedItemProperty().getValue().getValue());
                ob = FXCollections.observableList(itemlist);
                lVybrane.setItems(ob);
            }
            if(sub)
                itemlist.remove(itemlist.size()-1);
        }
    }
    
    public void bOdebratPressed(ActionEvent event) {
        if(lVybrane.getSelectionModel().selectedItemProperty().getValue()!=null) {
            itemlist.remove(lVybrane.getSelectionModel().selectedItemProperty().getValue());
            ob = FXCollections.observableList(itemlist);
            lVybrane.setItems(ob);
        }
    }
    
    public void hEmailPressed(ActionEvent event) {
        hEmail.setVisited(false);
        HostServices services = hajzl.getHostServices();
        services.showDocument("mailto:mvomacka@seznam.cz?subject=CHMUtoCSV");
    }
    
    public void bOdebratVsePressed(ActionEvent event) {
        itemlist.clear();
        ob = FXCollections.observableList(itemlist);
        lVybrane.setItems(ob);
    }
    
    public void bQuitPressed(ActionEvent event) {
        Stage stage = (Stage) bQuit.getScene().getWindow();
        stage.close();
    }
}
