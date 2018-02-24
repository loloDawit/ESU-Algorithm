/*
 * Class: ESUVisualizer.java
 * Purpose: A graphical user interface to dispaly the ESU graph. 
 * This class is reposible to read the graph text input and display a step by 
 * execution of the ESU algorithm.
 */
package esu.algorithm.UI;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import esu.*;
import esu.algorithm.*;
import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
/**
 *
 * @author BioHazard
 */
public class ESUVisualizer extends Application {
    private Desktop desktop = Desktop.getDesktop();
    final FileChooser fileChooser = new FileChooser();
    UndirectedGraph graph = new UndirectedGraph(101);
    
    ArrayList<Rectangle> r = new ArrayList<Rectangle>();
    private Rectangle box; 
    // controls 
    Button zoomInButton = new Button("+");
    Button zoomOutButton = new Button("-");
    Button resetButton = new Button("Reset");
    Button nextButton = new Button("Next");
    Button openFileButton = new Button("open File");
    TextField textField = new TextField();
    TextField sampleField = new TextField();
    
    // Containers 
    Group root = new Group();
    VBox vBox = new VBox();
    HBox menu = new HBox();
    StackPane nodeContainer = new StackPane();
    ScrollPane scrollPane = new ScrollPane();
    Pane pane = new Pane();
    ToolBar toolBar = new ToolBar();
    Canvas screen;
    
    //tree variables
    ArrayList<ESUTree> treeList;
    int currentIndex = -1;
    Rectangle treeSpace;
    ArrayList<Rectangle>[] rectangles;
    ArrayList<ESUNode>[] finalNodes;
    
    public ESUVisualizer(){
        super();
        UndirectedGraph graph = new UndirectedGraph(101);
        
        //**************    INSERT PATH TO GRAPH TEXT FILE **********************
        graph.fillGraph("/Users/dawitabera/Desktop/myGraph.txt");
        //***********************************************************************
        ESUTree tree = new ESUTree(graph, 4);
        treeList= new ArrayList<>();
        //step until done
        while(tree.step()){
            ESUTree tempTree = new ESUTree(tree);
            tree.clearStepLog();
            treeList.add(tempTree);
        }
        finalNodes = treeList.get(treeList.size()-1).getNodesByLevel();
        AuxilaryClass.setNodeDims(treeList.get(treeList.size()-1));
        treeSpace = AuxilaryClass.getTreeSpace(treeList.get(treeList.size()-1));
        rectangles = AuxilaryClass.getRectangles(treeList.get(treeList.size()-1));
        currentIndex = 0;
    }
    /**
     * Open graph from file 
     * @param file  
     */
    private void openGraphFile(File file){
        try {
            desktop.open(file);
        } catch (Exception e) {
            Logger.getLogger(ESUVisualizer.class.getName()).log(Level.SEVERE,null,e);
        }
        
    }
    /**
     * ConfigureFileChooer 
     * @param fileChooser only open text files 
     */
    private static void configureFileChooser(final FileChooser fileChooser){                           
        fileChooser.setTitle("View Graph Files");
        fileChooser.setInitialDirectory(
            new File(System.getProperty("user.home"))
        ); 
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt"));
        
    }
    /**
     * SetNodes
     * A simple method that sets and executes all action listeners.  
     */
    public void setNodes(){
        textField.setPromptText("open file");
        
        //screen = new Canvas();
        //screen.setHeight(treeSpace.getHeight());
        //screen.setWidth(treeSpace.getWidth());
        
        scrollPane.setTranslateX(7);
        scrollPane.setTranslateY(7);
        
        //scrollPane.setContent(screen);
        
        menu.setSpacing(5);
        menu.getChildren().addAll(zoomInButton,zoomOutButton,textField,
                openFileButton,resetButton,nextButton);
        
        // zoom in canvas content 
        zoomInButton.setOnAction((event) -> {
            nodeContainer.setPrefSize(Math.max(nodeContainer.getBoundsInParent().
                    getMaxX()*1.1, scrollPane.getViewportBounds().getWidth()),
            Math.max(nodeContainer.getBoundsInParent().getMaxY()*1.1, scrollPane.
                    getViewportBounds().getHeight())
            );
            nodeContainer.getChildren().add(pane);
            scrollPane.setContent(nodeContainer);
        });
        // zoomout canvas content 
        zoomInButton.setOnAction((event) -> {
            nodeContainer.setPrefSize(Math.max(nodeContainer.getBoundsInParent().
                    getMaxX()/1.1, scrollPane.getViewportBounds().getWidth()),
            Math.max(nodeContainer.getBoundsInParent().getMaxY()/1.1, scrollPane.
                    getViewportBounds().getHeight())
            );
            nodeContainer.getChildren().add(pane);
            scrollPane.setContent(nodeContainer);
            
        });
        
        nextButton.setOnAction((event) ->{
            if(currentIndex < 0){
                currentIndex = 0;
            }
            else if(currentIndex < treeList.size()-1){
                currentIndex++;
            }
            // here instead of show tree, we can call nextstep to show step 
            // by step execution of the tree. 
            showTree();
        });
        openFileButton.setOnAction(((event) -> {
            configureFileChooser(fileChooser);
            File file = fileChooser.showOpenDialog(new Stage());
            if(file !=null){
                openGraphFile(file);
                graph.fillGraph(file.getPath());
                showTree();
            }
            
        }));
        toolBar.getItems().addAll(menu);
        toolBar.setPrefWidth(800);
        menu.setPrefWidth(780);
        menu.setAlignment(Pos.CENTER);
        scrollPane.setPrefSize(60, 513);
        scrollPane.setMaxWidth(777);
        vBox.getChildren().addAll(toolBar,scrollPane);
        Pane pane = new Pane();
        
    }
    /**
     * 
     * @param primaryStage 
     */
    @Override
    public void start(Stage primaryStage) {
        setNodes();
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        
        stage.setWidth(800);
        stage.setHeight(600);
        root.getChildren().addAll(vBox);
        stage.setScene(scene);
        stage.setTitle("ESU Visualization Software");
        stage.show();
        
        
    }

    void showTree(){
        pane.getChildren().clear();
        pane.getChildren().addAll(0,AuxilaryClass.getPrintables(rectangles, 
                treeList.get(currentIndex).getNodesByLevel(), finalNodes));
        scrollPane.setContent(pane);
        
        // ************ @DEPRICATED ****************
        //AuxilaryClass.drawTo(screen.getGraphicsContext2D(), AuxilaryClass.getPrintables(rectangles, treeList.get(currentIndex).getNodesByLevel()));
        // *****************************************
    }
    /**
     * start the application 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
