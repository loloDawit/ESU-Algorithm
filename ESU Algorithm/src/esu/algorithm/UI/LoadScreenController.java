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
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author dawitabera
 */
public class LoadScreenController implements Initializable {
    ESUVisualizer openESUVisualizer = new ESUVisualizer();
    Stage stage;
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
                openESUVisualizer.start(stage);
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
        System.out.println("dawit");
    }
    private static void configureFileChooser(final FileChooser fileChooser){                           
        fileChooser.setTitle("View Graph Files");
        fileChooser.setInitialDirectory(
            new File(System.getProperty("user.home"))
        ); 
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF files (*.pdf)\", \"*.pdf"));
        
    }
        
    
}
