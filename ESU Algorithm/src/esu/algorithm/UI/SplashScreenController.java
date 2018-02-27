/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esu.algorithm.UI;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author dawitabera
 */
public class SplashScreenController implements Initializable {
    ;
    @FXML
    private ImageView sScreenImage;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private AnchorPane root;
    @FXML
    private ProgressIndicator pIndicator;

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       progressUpdate();
    } 
    private void progressUpdate(){
        final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for(int i=0; i<100; i++){
                    updateProgress(i+1, 100);
                    Thread.sleep(50);
                }
                return null;
            }
        };
        pIndicator.progressProperty().bind(task.progressProperty());
        pIndicator.progressProperty().addListener((observable) -> {
            if(pIndicator.getProgress() >= 1-0000005){
                pIndicator.setStyle("-fx-accent: forestgreen");
            }
        });
        progressBar.progressProperty().bind(task.progressProperty());
        progressBar.progressProperty().addListener((observable) -> {
            if(progressBar.getProgress() >= 1-0000005){
                progressBar.setStyle("-fx-accent: forestgreen");
            }
        });
        final  Thread thread = new Thread(task, "task-thread");
        thread.setDaemon(true);
        thread.start();
    }
    
    
        

    
}
