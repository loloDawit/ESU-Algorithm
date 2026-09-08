/*
 * Class: ESUVisualizer.java
 * Purpose: A graphical user interface to dispaly the ESU graph. 
 * This class is reposible to read the graph text input and display a step by 
 * execution of the ESU algorithm.
 */
package esu.algorithm.UI;

import javafx.animation.Animation;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
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
import java.util.Random;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.StageStyle;
/**
 *
 * @author BioHazard
 */
public class ESUVisualizer extends Application {
    private final Desktop desktop = Desktop.getDesktop();
    final FileChooser fileChooser = new FileChooser();
    UndirectedGraph graph = null;
    // controls 
    Button zoomInButton = new Button("+");
    Button zoomOutButton = new Button("-");
    Button resetButton = new Button("Reset");
    Button nextButton = new Button("Next");
    Button prevButton = new Button("Prev");
    Button fitButton = new Button("Fit");
    Button graphButton = new Button("Random");
    final Random random = new Random();
    Button openFileButton = new Button("open File");
    TextField textField = new TextField();
    TextField sampleField = new TextField();
    Label showProgressLabel = new Label("Progress: ");
    Button saveButton = new Button("Save");
    ListView<String> showProgress = new ListView<>();
    Button backButton = new Button("Back");
    ToggleButton playButton = new ToggleButton("Play");
    Label statusLabel = new Label();
    Slider speedSlider = new Slider(1, 20, 5);
    Timeline player = null;
    Button showFinalTree = new Button("FinalTree");
    
    // Containers 
    BorderPane root = new BorderPane();
    VBox vBox = new VBox();
    StackPane nodeContainer = new StackPane();
    // Floor is low on purpose: myGraph.txt at k=7 needs 0.05 to fit at all.
    Slider slider = new Slider(0.02,2,1);
    
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
    static final int MIN_SUBGRAPH_SIZE = 2;
    static final int MAX_SUBGRAPH_SIZE = 9;
    int subgraphSize = 5;
    File currentFile = null;
    ArrayList<StepInfo> currentLog = null;
    
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
        fileChooser.setTitle("Open a graph file");

        // Start where the graphs actually are. Falls back to home when the
        // app is run from somewhere else.
        File samples = new File("samples");
        fileChooser.setInitialDirectory(samples.isDirectory()
                ? samples
                : new File(System.getProperty("user.home")));

        // set, not add: this runs on every open and filters used to stack up.
        fileChooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Graph files (*.txt)", "*.txt"));
    }
    /**
     * SetNodes
     * A simple method that sets and executes all action listeners.  
     */
    public void setNodes(){
        textField.setPromptText("open file");
        textField.setEditable(false);

        // Subgraph size. Above MAX_SUBGRAPH_SIZE the step-by-step history
        // (one full tree copy per step) exhausts the heap.
        sampleField.setPrefWidth(50);
        sampleField.setText(Integer.toString(subgraphSize));
        sampleField.setTooltip(new Tooltip(
                "Subgraph size to search for (" + MIN_SUBGRAPH_SIZE
                + "-" + MAX_SUBGRAPH_SIZE + ")"));
        sampleField.setOnAction((event) -> applySubgraphSize());

        // Autoplay. The speed slider is its own control: `slider` is already
        // bound to the canvas zoom factor.
        speedSlider.setPrefWidth(90);
        speedSlider.setTooltip(new Tooltip("Playback speed (steps per second)"));
        playButton.setOnAction((event) -> {
            if(playButton.isSelected()){
                startPlayback();
            } else {
                stopPlayback();
            }
        });
        speedSlider.valueProperty().addListener((obs, was, now) -> {
            if(player != null){
                startPlayback();   // rebuild at the new rate
            }
        });

        // Selecting a log line highlights the node that line is about.
        showProgress.getSelectionModel().selectedIndexProperty()
                .addListener((obs, was, now) -> highlightFromLog(now.intValue()));
        sampleField.focusedProperty().addListener((obs, hadFocus, hasFocus) -> {
            if(!hasFocus){
                applySubgraphSize();
            }
        });
        
        scrollPane.setTranslateX(7);
        scrollPane.setTranslateY(7);
        
        // +/- step the same zoom the slider drives, so the three controls
        // that used to disagree now all mean one thing.
        zoomInButton.setTooltip(new Tooltip("Zoom in"));
        zoomOutButton.setTooltip(new Tooltip("Zoom out"));
        zoomInButton.setOnAction((event) -> nudgeZoom(0.15));
        zoomOutButton.setOnAction((event) -> nudgeZoom(-0.15));
        fitButton.setTooltip(new Tooltip("Zoom so the whole tree fits"));
        fitButton.setOnAction((event) -> fitToWindow());
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
        graphButton.setTooltip(new Tooltip(
                "Generate a random connected graph and run ESU on it"));
        graphButton.setOnAction((event) -> generateRandomGraph());
        resetButton.setOnAction((event) ->{
            reset();
        });
        
        saveButton.setOnAction((event) ->{
            String output = "Biohazard    \n "
                    + "Subgraphs found: " + leaves + "\n"
                    + "Subgraphs: "+ "\n";
           
            
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
            stopPlayback();
            loadWindow("/esu/algorithm/UI/loadScreen.fxml", "Undirected Subgraph Enumeration Software");
            closeStage();
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
        statusLabel.getStyleClass().add("status-label");
        HBox statusBar = new HBox(statusLabel);
        statusBar.setPadding(new Insets(4, 10, 4, 10));
        statusBar.getStyleClass().add("status-bar");
        updateStatus();
        root.setTop(new VBox(toolBar, buildLegend(), statusBar));
        toolBar.getItems().addAll(zoomInButton,zoomOutButton,
                                  new Separator(),textField,
                                  new Separator(),openFileButton,
                                  resetButton,prevButton,nextButton,
                                  playButton,speedSlider,
                                  new Separator(),graphButton,
                                  new Separator(),new Label("k ="),sampleField,
                                  new Separator(),slider,new Separator()
                                  );
        
        toolBar.setPadding(new Insets(5, 25, 5, 150));
        toolBar2.setOrientation(Orientation.VERTICAL);
        toolBar2.getItems().addAll(new Separator(),fitButton);
        root.setLeft(toolBar2);
        scrollPane.setPadding(new Insets(5, 5, 5, 5));
        //root.setCenter(scrollPane);
        toolBar3.setOrientation(Orientation.VERTICAL);
        toolBar3.getItems().addAll(new Separator(),saveButton,new Separator(),showFinalTree,new Separator());
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
        Scene scene = new Scene(root,1040,720);
        scene.getStylesheets().add(
                getClass().getResource("esu.css").toExternalForm());
        
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
     * Generate a random connected graph, save it beside the other samples,
     * and load it. It is written out as an ordinary graph file so it can be
     * reopened, edited or kept, rather than existing only on screen.
     */
    private void generateRandomGraph(){
        try {
            File file = new File("samples/random-graph.txt");
            file.getParentFile().mkdirs();
            RandomGraph.writeToFile(RandomGraph.generate(random), file);
            reset();
            loadGraph(file);
            showTree();
            fitToWindow();
        } catch (IOException e) {
            Logger.getLogger(ESUVisualizer.class.getName())
                    .log(Level.SEVERE, null, e);
            Alerts.displayFileNotFound();
        }
    }

    /**
     * Move the zoom by a step, staying inside the slider's range.
     *
     * @param delta amount to add to the current zoom factor
     */
    private void nudgeZoom(double delta){
        double next = slider.getValue() + delta;
        slider.setValue(Math.max(slider.getMin(),
                Math.min(slider.getMax(), next)));
    }

    /**
     * Zoom so the whole tree is visible, and scroll back to the top left.
     * The tree is laid out wider than the window for anything but a tiny
     * graph, so without this it opens part way into empty canvas.
     */
    private void fitToWindow(){
        if(treeList == null){
            return;
        }
        // Measure the tree, not the pane: the pane is stretched to the
        // viewport, so measuring it would never zoom out.
        if(treeSpace == null){
            return;
        }
        Bounds view = scrollPane.getViewportBounds();
        double treeWidth = treeSpace.getWidth();
        double treeHeight = treeSpace.getHeight();
        if(treeWidth <= 0 || treeHeight <= 0
                || view.getWidth() <= 0 || view.getHeight() <= 0){
            return;
        }
        double factor = Math.min(view.getWidth() / treeWidth,
                view.getHeight() / treeHeight) * 0.95;   // a little margin
        slider.setValue(Math.max(slider.getMin(),
                Math.min(slider.getMax(), factor)));
        scrollPane.setHvalue(0);
        scrollPane.setVvalue(0);
    }

    /**
     * Enable or disable everything that needs a loaded graph.
     *
     * @param loaded true once a graph is on screen
     */
    private void setControlsEnabled(boolean loaded){
        for(Node control : new Node[]{ zoomInButton, zoomOutButton, resetButton,
                nextButton, prevButton, playButton, speedSlider, fitButton,
                graphButton, saveButton, showFinalTree, sampleField, slider }){
            control.setDisable(!loaded);
        }
    }

    /**
     * Say where we are: which step, and how many subgraphs have turned up.
     * With no graph loaded this is the only instruction on screen.
     */
    private void updateStatus(){
        if(treeList == null){
            statusLabel.setText("Open a graph file to begin  \u2192  "
                    + "samples/sample-small.txt is a good start");
            return;
        }
        int shown = currentIndex < 0 ? 0 : currentIndex + 1;
        statusLabel.setText("Step " + shown + " of " + treeList.size()
                + "   \u00b7   " + leaves + " subgraph"
                + (leaves == 1 ? "" : "s") + " of size " + subgraphSize
                + " found   \u00b7   Play or Next to step through");
    }

    /**
     * One legend entry: a swatch in a node's colour and its meaning.
     *
     * @param fill   swatch fill
     * @param stroke swatch outline
     * @param label  what that colour means
     * @param dashed true to draw the outline dashed, as dead ends are
     * @return the swatch and its caption
     */
    private HBox legendEntry(Color fill, Color stroke, String label,
            boolean dashed){
        Rectangle swatch = new Rectangle(16, 11, fill);
        swatch.setStroke(stroke);
        if(dashed){
            swatch.getStrokeDashArray().addAll(3.0, 2.0);
        }
        Label caption = new Label(label);
        caption.getStyleClass().add("legend-label");
        HBox entry = new HBox(5, swatch, caption);
        entry.setAlignment(Pos.CENTER_LEFT);
        return entry;
    }

    /**
     * The legend bar: what each node colour means, and how to read the three
     * lines inside a node.
     *
     * @return the assembled legend
     */
    private HBox buildLegend(){
        Label reading = new Label("node:  {subgraph}  (extension)  [neighbors]");
        reading.getStyleClass().add("legend-key");
        HBox legend = new HBox(14,
                legendEntry(AuxilaryClass.ACTIVE_FILL,
                        AuxilaryClass.ACTIVE_STROKE, "working on", false),
                legendEntry(AuxilaryClass.COMPLETE_FILL,
                        AuxilaryClass.COMPLETE_STROKE, "subgraph found", false),
                legendEntry(AuxilaryClass.DEADEND_FILL,
                        AuxilaryClass.DEADEND_STROKE, "dead end", true),
                legendEntry(AuxilaryClass.PENDING_FILL,
                        AuxilaryClass.PENDING_STROKE, "still expanding", false),
                new Separator(Orientation.VERTICAL),
                reading);
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.setPadding(new Insets(4, 10, 4, 10));
        legend.getStyleClass().add("legend-bar");
        return legend;
    }

    /**
     * Start (or restart) stepping the tree forward on a timer.
     * Stops on its own at the last step.
     */
    private void startPlayback(){
        stopPlayback();
        if(treeList == null){
            playButton.setSelected(false);
            return;
        }
        Duration tick = Duration.seconds(1.0 / speedSlider.getValue());
        player = new Timeline(new KeyFrame(tick, (event) -> {
            if(currentIndex >= treeList.size() - 1){
                stopPlayback();
                playButton.setSelected(false);
                return;
            }
            currentIndex = currentIndex < 0 ? 0 : currentIndex + 1;
            showTree();
        }));
        player.setCycleCount(Animation.INDEFINITE);
        player.play();
        playButton.setText("Pause");
    }

    /**
     * Stop the playback timer. Safe to call when nothing is playing, and
     * called before anything that invalidates the tree or the window.
     */
    private void stopPlayback(){
        if(player != null){
            player.stop();
            player = null;
        }
        playButton.setText("Play");
    }

    /**
     * Read k from its field and rebuild the tree if it changed.
     * Out-of-range or non-numeric input snaps back to the current value.
     */
    private void applySubgraphSize(){
        int requested;
        try {
            requested = Integer.parseInt(sampleField.getText().trim());
        } catch (NumberFormatException e) {
            sampleField.setText(Integer.toString(subgraphSize));
            return;
        }
        if(requested < MIN_SUBGRAPH_SIZE || requested > MAX_SUBGRAPH_SIZE){
            Alerts.displaySubgraphSizeRange(MIN_SUBGRAPH_SIZE, MAX_SUBGRAPH_SIZE);
            sampleField.setText(Integer.toString(subgraphSize));
            return;
        }
        if(requested == subgraphSize){
            return;
        }
        subgraphSize = requested;
        if(currentFile != null){
            reset();
            loadGraph(currentFile);
            showTree();
        }
    }

    /**
     * reset the canvas 
     */
    void reset(){
        stopPlayback();
        playButton.setSelected(false);
        pane.getChildren().clear();
        nodeContainer.getChildren().clear();
        currentIndex = -1;
        updateStatus();
    }
    /**
     * showTree 
     */
    void showTree(){
        if(treeList == null)
            return;
        ArrayList<ESUNode>[] currentNodes =
                treeList.get(currentIndex).getNodesByLevel();
        currentLog = treeList.get(currentIndex).getLog();

        AuxilaryClass.styleNodes(rectangles, currentNodes, finalNodes,
                activeSubgraph(currentLog));

        pane.getChildren().clear();
        pane.getChildren().addAll(0,AuxilaryClass.getPrintables(rectangles, 
                currentNodes, finalNodes));
        scrollPane.setContent(pane);
        updateStatus();
        showProgress.getItems().clear();
        ArrayList<StepInfo> stepLog = currentLog;
        
        //moved to "loadGraph" to set on initialization
        //count = stepLog.get(stepLog.size() - 1).count;
        
        for (int entry = 0; entry < stepLog.size(); entry++) {
            showProgress.getItems().add(stepLog.get(entry).render());
        }
        // ************ @DEPRICATED ****************
        //AuxilaryClass.drawTo(screen.getGraphicsContext2D(), AuxilaryClass.getPrintables(rectangles, treeList.get(currentIndex).getNodesByLevel()));
        // *****************************************
    }

    /**
     * The node this step is working on: the caller of the step's last log
     * entry.
     *
     * @param stepLog the log for the step being displayed
     * @return its subgraph string, or null if there is nothing to highlight
     */
    private String activeSubgraph(ArrayList<StepInfo> stepLog){
        if(stepLog == null || stepLog.isEmpty()){
            return null;
        }
        return stepLog.get(stepLog.size() - 1).getCallerSubgraph();
    }

    /**
     * Highlight the node a log line is talking about. Log entries are added
     * in stepLog order, so the selected row indexes straight into it.
     *
     * @param logIndex row selected in the progress list
     */
    private void highlightFromLog(int logIndex){
        if(currentLog == null || logIndex < 0 || logIndex >= currentLog.size()){
            return;
        }
        AuxilaryClass.styleNodes(rectangles,
                treeList.get(currentIndex).getNodesByLevel(), finalNodes,
                currentLog.get(logIndex).getCallerSubgraph());
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
        graph = UndirectedGraph.fromFile(file);
        if(graph == null){
            Alerts.displayInputMismatch();
            return;
        }
        currentFile = file;
        textField.setText(file.getName());
        
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
        setControlsEnabled(true);
        updateStatus();
        // Fit once the pane has been laid out, so the tree is on screen and
        // whole rather than opening zoomed into a corner of it.
        Platform.runLater(this::fitToWindow);
        if(leaves == 0){
            Alerts.displayNoSubgraphs(subgraphSize);
        }
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

