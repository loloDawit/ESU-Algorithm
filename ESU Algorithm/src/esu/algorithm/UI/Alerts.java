/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esu.algorithm.UI;

import javafx.scene.control.Alert;

/**
 *
 * @author dawitabera
 */
public class Alerts {
    public static Alert alert;

    public Alerts() {
        
    }
    /**
     * display error message 
     * error type: file not found 
     */
    public static void displayFileNotFound(){
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error opening file");
        alert.setContentText("File not found");
        alert.show();
    }
    public static void displayInputMismatch(){
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error opening file");
        alert.setContentText("Input Mismatch, Check the graph file");
        alert.show();
    }
    public static void displaySelectGraph(){
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Make sure the graph is undirected graph");
        alert.setContentText("The software only supports undirected graphs for now");
        alert.show();
    }
            
    
    
    
    
}
