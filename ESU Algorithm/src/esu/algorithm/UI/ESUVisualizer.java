/*
 * Class: ESUVisualizer.java
 * Purpose: A graphical user interface to dispaly the ESU graph. 
 * This class is reposible to read the graph text input and display a step by 
 * execution of the ESU algorithm.
 */
package esu.algorithm.UI;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import esu.algorithm.*;
import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.StageStyle;
import jdk.jfr.events.FileWriteEvent;
/**
 *
 * @author BioHazard
 */
public class ESUVisualizer extends Application {
    MainGraph graphApp = new MainGraph();
    private final Desktop desktop = Desktop.getDesktop();
    final FileChooser fileChooser = new FileChooser();
    UndirectedGraph graph = null;
    // controls 
    Button zoomInButton = new Button("+");
    Button zoomOutButton = new Button("-");
    Button resetButton = new Button("Reset");
    Button nextButton = new Button("Next");
    Button prevButton = new Button("Prev");
    ToggleButton scaleButton = new ToggleButton("Scale");
    Button graphButton = new Button("Graph");
    Button openFileButton = new Button("open File");
    TextField textField = new TextField();
    TextField sampleField = new TextField();
    Label showProgressLabel = new Label("Progress: ");
    Button saveButton = new Button("Save");
    ListView<String> showProgress = new ListView<>();
    Button backButton = new Button("Back");
    Button showFinalTree = new Button("FinalTree");
    
    // Containers 
    BorderPane root = new BorderPane();
    VBox vBox = new VBox();
    StackPane nodeContainer = new StackPane();
    Slider slider = new Slider(0.5,2,1);
    
    Pane pane = new Pane();
    ScrollPane scrollPane = new ScrollPane();
    ToolBar toolBar = new ToolBar();
    ToolBar toolBar2 = new ToolBar();
    ToolBar toolBar3 = new ToolBar();
    ToolBar toolBar4 = new ToolBar();
    Node node = pane;
    
    //tree variables
    ArrayList<ESUTree> treeList = null;
    int currentIndex = -1;
    Rectangle treeSpace;
    ArrayList<Rectangle>[] rectangles;
    ArrayList<ESUNode>[] finalNodes;
    int leaves = 0;
    int subgraphSize = 5;
    
    public ESUVisualizer(){
        super();
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
        textField.setEditable(false);
        
        scrollPane.setTranslateX(7);
        scrollPane.setTranslateY(7);
        
        // zoom in canvas content 
        zoomInButton.setOnAction((event) -> {
            pane.setPrefSize(Math.max(pane.getBoundsInParent().
                    getMaxX()*3.3, scrollPane.getViewportBounds().getWidth()),
            Math.max(pane.getBoundsInParent().getMaxY()*3.3, scrollPane.
                    getViewportBounds().getHeight())
            );
            scrollPane.setContent(pane);
            
        });
        // zoomout canvas content 
        zoomOutButton.setOnAction((event) -> {
            pane.setPrefSize(Math.max(pane.getBoundsInParent().
                    getMaxX()/10, scrollPane.getViewportBounds().getWidth()),
            Math.max(pane.getBoundsInParent().getMaxY()/10, scrollPane.
                    getViewportBounds().getHeight())
            );
            scrollPane.setContent(pane);
            
        });
        //show final ESU Tree
        showFinalTree.setOnAction((event) ->{
            if(textField.getText().isEmpty()){
                Alerts.displayFileNotFound();
                return;
            }
            /*
            currentIndex = 0;
            while(currentIndex < treeList.size() - 1 ){
                showTree();
                currentIndex++;
            }
            */
            currentIndex = treeList.size() - 1;
            showTree();
        });
        
        nextButton.setOnAction((event) ->{
            if (treeList == null)
                return;
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
        prevButton.setOnAction((event) ->{
            if (treeList == null)
                return;
            if(currentIndex < 1){
                currentIndex = 0;
            }
            else{
                currentIndex--;
            }
            // here instead of show tree, we can call nextstep to show step 
            // by step execution of the tree. 
            showTree();
        });
        
        openFileButton.setOnAction(((event) -> {
            configureFileChooser(fileChooser);
            File file = fileChooser.showOpenDialog(new Stage());
            if(file !=null){
                //openGraphFile(file);
                loadGraph(file);
            }else 
                Alerts.displayFileNotFound();
            
        }));
        graphButton.setOnAction((event) ->{
            reset();
            loadWindow("/esu/algorithm/UI/GraphApp.fxml","Undirected Graph");
        });
        resetButton.setOnAction((event) ->{
            reset();
        });
        
        saveButton.setOnAction((event) ->{
            String output = "Biohazard    \n "
                    + "Subgraphs found: " + leaves + "\n"
                    + "Subgraphs: "+ "\n";
           
            Text tString = TextBuilder.create().text(output).build();
            
            //get subgraphs as text for file printing
            int filler = 0;
            for(ESUNode eNode : finalNodes[finalNodes.length -1]){
                output += eNode.getSubgraphAsString() + "\n";
            }
            
            FileChooser fileChooser = new FileChooser();
            //Set extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);
              
            //Show save file dialog
            File file = fileChooser.showSaveDialog(new Stage());
            if(file != null){
                saveFile(output, file);
                Alerts.displaySavedToFile();
            }
        });
        backButton.setOnAction(((event) -> {
            loadWindow("/esu/algorithm/UI/loadScreen.fxml", "Undirected Subgraph Enumeration Software");
            closeStage();
        }));
        scaleButton.setOnAction(((event) -> {
            if(scaleButton.isSelected()){
                node.setScaleX(0.3);
                node.setScaleY(0.3);
            }else{
                node.setScaleX(1);
                node.setScaleY(1);
            }
            Platform.runLater(new Runnable(){
                @Override
                public void run() {
                    nodeContainer.setPrefSize(Math.max(nodeContainer.
                            getBoundsInParent().getMaxX(), scrollPane.
                                    getViewportBounds().getWidth()),
                            Math.max(nodeContainer.getBoundsInParent().
                                    getMaxY(), scrollPane.getViewportBounds().
                                            getHeight())
                    );
                }
                
            });
        }));
        ZoomingPane zoomingPane = new ZoomingPane(scrollPane);
       
        zoomingPane.zoomFactorProperty().bind(slider.valueProperty());
        scrollPane.setContent
        (nodeContainer);
        scrollPane.viewportBoundsProperty().addListener((ObservableValue<? extends Bounds> observableValue, Bounds oldBounds, Bounds newBounds) -> {
            nodeContainer.setPrefSize(
                    Math.max(node.getBoundsInParent().getMaxX(), newBounds.getWidth()),
                    Math.max(node.getBoundsInParent().getMaxY(), newBounds.getHeight())
            );
        });
        root.setCenter(zoomingPane);
        root.setTop(toolBar);
        toolBar.getItems().addAll(zoomInButton,zoomOutButton,
                                  new Separator(),textField,
                                  new Separator(),openFileButton,
                                  resetButton,nextButton,prevButton, new Separator(),slider,new Separator()
                                  );
        
        toolBar.setPadding(new Insets(5, 25, 5, 150));
        toolBar2.setOrientation(Orientation.VERTICAL);
        toolBar2.getItems().addAll(new Separator(),scaleButton,new Separator(),
                                   graphButton);
        root.setLeft(toolBar2);
        scrollPane.setPadding(new Insets(5, 5, 5, 5));
        //root.setCenter(scrollPane);
        toolBar3.setOrientation(Orientation.VERTICAL);
        toolBar3.getItems().addAll(new Separator(),saveButton,showFinalTree,new Separator());
        root.setRight(toolBar3);
        showProgress.setPrefSize(610, 100);
        ObservableList<String> items =FXCollections.observableArrayList();
        showProgress.setItems(items);
        toolBar4.getItems().addAll(showProgressLabel,new Separator(),showProgress,new Separator(),backButton);
        root.setBottom(toolBar4);
        root.setPadding(new Insets(5, 5, 5, 5));
    }
    /**
     * 
     * @param primaryStage 
     */
    @Override
    public void start(Stage primaryStage) {
        setNodes();
        Stage stage = new Stage();
        Scene scene = new Scene(root,910,650);
        
        stage.setScene(scene);
        stage.setTitle("ESU Visualization Software");
        stage.sizeToScene();
        stage.show();
    }
    /**
     * 
     * @param newNode 
     */
    private void setNode(Node newNode) {
        Pane parent = this.node != null ? (Pane) this.node.getParent() : null;
        if (parent != null) {
            parent.getChildren().remove(this.node);
            parent.getChildren().add(newNode);
        }
        this.node = newNode;
        reset();
        node.boundsInParentProperty().addListener(new ChangeListener<Bounds>() {
            @Override public void changed(ObservableValue<? extends Bounds> observableValue, Bounds oldBounds, Bounds newBounds) {
                nodeContainer.setPrefSize(
                        Math.max(newBounds.getMaxX(), scrollPane.getViewportBounds().getWidth()),
                        Math.max(newBounds.getMaxY(), scrollPane.getViewportBounds().getHeight())
        );
      }
    });
    }
    /**
     * reset the canvas 
     */
    void reset(){
        pane.getChildren().clear();
        nodeContainer.getChildren().clear();
        currentIndex = -1;
    }
    /**
     * showTree 
     */
    void showTree(){
        if(treeList == null)
            return;
        pane.getChildren().clear();
        pane.getChildren().addAll(0,AuxilaryClass.getPrintables(rectangles, 
                treeList.get(currentIndex).getNodesByLevel(), finalNodes));
        scrollPane.setContent(pane);
        showProgress.getItems().clear();
        ArrayList<StepInfo> stepLog = treeList.get(currentIndex).getLog();
        
        //moved to "loadGraph" to set on initialization
        //count = stepLog.get(stepLog.size() - 1).count;
        
        for (int entry = 0; entry < stepLog.size(); entry++) {
            String caller = stepLog.get(entry).caller.getSubgraphAsString();
            String target = "{}";
            if(stepLog.get(entry).target != null)
                target = stepLog.get(entry).target.getSubgraphAsString();
            String description = stepLog.get(entry).description;
            description = description.replace("%t", target);
            description = description.replace("%c", caller);
            showProgress.getItems().add(description);        
        }
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
    
    private Node createGraph() {// create a graph node to be acted on by the controls.// setup graph
        return node;
    }
    public class ZoomingPane extends Pane{
    Node content;
    private DoubleProperty zoomFactor = new SimpleDoubleProperty(1);
    
    private ZoomingPane(Node content){
        this.content = content;
        getChildren().add(content);
        Scale scale = new Scale(1,1);
        content.getTransforms().add(scale);
        zoomFactor.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            scale.setX(newValue.doubleValue());
            scale.setY(newValue.doubleValue());
            requestLayout();
        });
    }
    @Override
    protected void layoutChildren() {
        Pos pos = Pos.TOP_LEFT;
        double width = getWidth();
        double height = getHeight();
        double top = getInsets().getTop();
        double right = getInsets().getRight();
        double left = getInsets().getLeft();
        double bottom = getInsets().getBottom();
        double contentWidth = (width - left - right)/zoomFactor.get();
        double contentHeight = (height - top - bottom)/zoomFactor.get();
        layoutInArea(content, left, top,
                    contentWidth, contentHeight,
                    0, null,
                    pos.getHpos(),
                    pos.getVpos());
        }

        public final Double getZoomFactor() {
            return zoomFactor.get();
        }
        public final void setZoomFactor(Double zoomFactor) {
            this.zoomFactor.set(zoomFactor);
        }
        public final DoubleProperty zoomFactorProperty() {
            return zoomFactor;
        }
    }
    
    public void loadGraph(File file){
        int size = getLargestVertex(file);
        if(size < 0){
            return;
        }
        textField.setText(file.getName());
        graph = new UndirectedGraph(size);
        graph.fillGraph(file.getPath());
        
        ESUTree tree = new ESUTree(graph, subgraphSize);
        treeList= new ArrayList<>();
        ESUTree tempTree = new ESUTree(tree);
        tree.clearStepLog();
        treeList.add(tempTree);
        tree.clearStepLog();
        
        //step until done
        while(tree.step()){
            tempTree = new ESUTree(tree);
            tree.clearStepLog();
            treeList.add(tempTree);
        }
        finalNodes = treeList.get(treeList.size()-1).getNodesByLevel(); 
        
        AuxilaryClass.setNodeDims(treeList.get(treeList.size()-1));
        treeSpace = AuxilaryClass.getTreeSpace(treeList.get(treeList.size()-1));
        rectangles = AuxilaryClass.getRectangles(treeList.get(treeList.size()-1));
        
        leaves = finalNodes[finalNodes.length-1].size();
        currentIndex = -1;
    }
    
    int getLargestVertex(File file){
        int result = -1;
        try{
            Scanner scan = new Scanner(file);
            while(scan.hasNext()){
                int next = scan.nextInt();
                if(next > result){
                    result = next;
                }
            }
            scan.close();
        }
        catch(Exception e){
            Alerts.displayInputMismatch();
            System.out.println("Error opening file: " + file.getAbsolutePath());
        }
        return result;
    }
    /**
     * 
     * @param loc   location of the fxml class
     * @param title to the scene 
     */
    void loadWindow(String loc, String title){
        try {
            Parent parent = FXMLLoader.load(getClass().getResource(loc));
            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle(title);
            stage.setScene(new Scene(parent));
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(ESUVisualizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    /**
     * 
     * @param content
     * @param file 
     */
    private void saveFile(String content, File file){
        try {
            FileWriter fileWriter = null;
            fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException e) {
            Logger.getLogger(ESUVisualizer.class.getName()).log(Level.SEVERE,null,e);
        }
    }
    /**
     * close current stage 
     */
    public void closeStage() {
        ((Stage)root.getScene().getWindow()).close();
    }
    
}

