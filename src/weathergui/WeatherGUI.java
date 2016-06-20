/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weathergui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author Vomec
 */
public class WeatherGUI extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoe = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
        Parent root = fxmlLoe.load();
        Weather myController = (Weather)fxmlLoe.getController();
        myController.setHajzl(this);
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.setTitle("ČHMÚ HPPS to CSV - ©Martin Vomáčka 2016");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("cloud.png")));
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
