/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esu.algorithm.UI;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.InputMismatchException;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

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
        
        Exception ex = new FileNotFoundException("Could not find the file");
        // creating expandable ex
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ex.printStackTrace(printWriter);
        String exText = stringWriter.toString();
        Label showEx = new Label("The exception stactrace was:");
        
        TextArea exArea = new TextArea(exText);
                 exArea.setEditable(false);
                 exArea.setWrapText(true);
                 exArea.setMaxWidth(Double.MAX_VALUE);
                 exArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(exArea, Priority.ALWAYS);
        GridPane.setHgrow(exArea, Priority.ALWAYS);
        
        GridPane exContent = new GridPane();
                 exContent.setMaxWidth(Double.MAX_VALUE);
                 exContent.add(showEx, 0, 0);
                 exContent.add(exArea, 0, 1);
        alert.getDialogPane().setExpandableContent(exContent);
        alert.showAndWait();
    }
    public static void displayInputMismatch(){
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error opening file");
        alert.setContentText("Input Mismatch, Check the graph file");
        
        Exception ex = new InputMismatchException("Input Mismatch");
        // creating expandable ex
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ex.printStackTrace(printWriter);
        String exText = stringWriter.toString();
        Label showEx = new Label("The exception stactrace was:");
        
        TextArea exArea = new TextArea(exText);
                 exArea.setEditable(false);
                 exArea.setWrapText(true);
                 exArea.setMaxWidth(Double.MAX_VALUE);
                 exArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(exArea, Priority.ALWAYS);
        GridPane.setHgrow(exArea, Priority.ALWAYS);
        
        GridPane exContent = new GridPane();
                 exContent.setMaxWidth(Double.MAX_VALUE);
                 exContent.add(showEx, 0, 0);
                 exContent.add(exArea, 0, 1);
        alert.getDialogPane().setExpandableContent(exContent);
        alert.showAndWait();
    }
    public static void displaySelectGraph(){
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Make sure the graph is undirected graph");
        alert.setContentText("The software only supports undirected graphs for now");
        alert.show();
    }
    public static void displaySavedToFile(){
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText("File was successfully saved");
        alert.show();
    }
            
    
    
    
    
}
