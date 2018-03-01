/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esu.algorithm.UI;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author dawitabera
 */
public class Run extends Application{
    startESU open = new startESU();
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("splashScreen.fxml"));
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(5),root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setCycleCount(1);
        
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(3000),root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setCycleCount(1);
        
        fadeIn.play();
        
        fadeIn.setOnFinished((event)->{
            fadeOut.play();
            try {
                open.start(stage);
            } catch (Exception ex) {
                Logger.getLogger(Run.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        Scene scene = new Scene(root);
        stage.setTitle("Undirected Subgraph Enumeration Software");
        stage.setScene(scene);
        stage.show();
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
