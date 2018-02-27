/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esu.algorithm.UI;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author dawitabera
 */
public class ESUDriver extends Application {
    ESUVisualizer open = new ESUVisualizer();
    
    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button();
        RadioButton undirectedButton = new RadioButton("Undirected Graph");
        TextField fileinputField = new TextField();
                  fileinputField.setPromptText("Select text from file");
                  
        btn.setText("Open text File");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent event) {
                System.out.println("file selected");
                if(undirectedButton.isSelected()){
                    try {
                    open.start(primaryStage);
                    primaryStage.close();
                } catch (Exception ex) {
                    Logger.getLogger(ESUDriver.class.getName()).log(Level.SEVERE, null, ex);
                }
                }else
                    Alerts.displaySelectGraph();
            }
        });
        VBox vBox = new VBox();
             vBox.setAlignment(Pos.CENTER);
             vBox.setSpacing(5);
        StackPane root = new StackPane();
        vBox.getChildren().addAll(undirectedButton,btn);
        root.getChildren().addAll(vBox);
        
        Scene scene = new Scene(root, 300, 250);
        
        primaryStage.setTitle("ESU V I S U A L I Z A T I O N");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
