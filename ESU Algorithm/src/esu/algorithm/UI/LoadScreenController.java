/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esu.algorithm.UI;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
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
    private void showDoc(ActionEvent event) {
    }

    @FXML
    private void showTeam(ActionEvent event) {
        System.out.println("dawit");
    }
        
    
}
