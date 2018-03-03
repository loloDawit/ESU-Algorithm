/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esu.algorithm.UI;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * FXML Controller class
 *
 * @author dawitabera
 */
public class LoadScreenController implements Initializable {
    ESUVisualizer openESUVisualizer = new ESUVisualizer();
    
    @FXML
    private CheckBox checkUniGraph;

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void startApp(ActionEvent event) {
        
        if(checkUniGraph.isSelected()){
            try {
                openESUVisualizer.start(new Stage());
                closeStage();
            } catch (Exception e) {
            }
        }else
            Alerts.displaySelectGraph();
    }

    @FXML
    private void showDoc(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
              
        //Show open file dialog
        File pdfFile = fileChooser.showOpenDialog(new Stage());
        if(pdfFile == null){
            // show error 
            Alerts.displayFileNotFound();
        }else{
            // else open pdf file
            if(Desktop.isDesktopSupported()){
                Desktop.getDesktop().open(pdfFile.getAbsoluteFile());
            }
        }
            
            
    }

    @FXML
    private void showTeam(ActionEvent event) {
        loadWindow("/esu/algorithm/UI/showTeam.fxml", "Biohazard Team Members");
        
    }
    private static void configureFileChooser(final FileChooser fileChooser){                           
        fileChooser.setTitle("View Graph Files");
        fileChooser.setInitialDirectory(
            new File(System.getProperty("user.home"))
        ); 
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF files (*.pdf)\", \"*.pdf"));
        
    }
    public void closeStage() {
        ((Stage)checkUniGraph.getScene().getWindow()).close();
    }
    /**
     * 
     * @param loc
     * @param title 
     */
    void loadWindow(String loc, String title){
        try {
            Parent parent = FXMLLoader.load(getClass().getResource(loc));
            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle(title);
            stage.setScene(new Scene(parent));
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(LoadScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }  
    
}
