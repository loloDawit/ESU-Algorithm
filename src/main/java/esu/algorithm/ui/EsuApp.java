/*
 * The application: one window showing the graph, the search tree, and where
 * the algorithm currently is.
 */
package esu.algorithm.ui;

import esu.algorithm.EsuNode;
import esu.algorithm.EsuSession;
import esu.algorithm.Shape;
import esu.algorithm.StepInfo;
import esu.algorithm.UndirectedGraph;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javafx.application.Application;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Class EsuApp
 *
 * One window: the input graph and the step log down the left, the search tree
 * filling the rest, controls along the top and bottom.
 *
 * All state lives in the EsuSession. Every view reads from it, so they cannot
 * disagree about which step is on screen.
 */
public class EsuApp extends Application {

    public static final int MIN_SUBGRAPH_SIZE = 2;
    /** Above this the tree has more boxes than anyone can read. */
    public static final int MAX_SUBGRAPH_SIZE = 6;

    private EsuSession session;
    private File currentFile;
    private int subgraphSize = 4;

    private final Random random = new Random();

    /** Which node the picture is explaining: the active one, or a clicked one. */
    private String focusId;

    private final GraphPanel graphPanel = new GraphPanel();
    private final HistoryPanel historyPanel = new HistoryPanel(this::goTo);
    private final CheckMenuItem showHistory =
            new CheckMenuItem("Show step history");
    private final CheckMenuItem showShapes =
            new CheckMenuItem("Show shapes found");
    private final ShapesPanel shapesPanel =
            new ShapesPanel(this::pickShape, this::showInstanceInGraph);
    private final CheckMenuItem followStep =
            new CheckMenuItem("Follow the current step");
    private final CheckMenuItem darkMode = new CheckMenuItem("Dark appearance");
    private Scene scene;
    private final TreeView treeView = new TreeView();
    private final ListView<String> stepLog = new ListView<>();
    private final Label statusLabel = new Label();
    private final Label subgraphLabel = new Label();
    private final Label extensionLabel = new Label();
    private final ProgressIndicator busy = new ProgressIndicator();

    private final Button openButton = new Button("Open graph");
    private final Button randomButton = new Button("Random");
    private final Button saveButton = new Button("Save results");
    private final Button startButton = new Button("«");
    private final Button prevButton = new Button("‹");
    private final Button nextButton = new Button("›");
    private final Button endButton = new Button("»");
    private final ToggleButton playButton = new ToggleButton("Play");
    private final Button fitButton = new Button("Fit");
    private final Button zoomInButton = new Button("+");
    private final Button zoomOutButton = new Button("−");
    private final Slider zoomSlider =
            new Slider(TreeView.MIN_ZOOM, TreeView.MAX_ZOOM, 1);
    private final Slider speedSlider = new Slider(1, 20, 6);
    private final HBox sizePills = new HBox(4);
    private final ToggleGroup sizeGroup = new ToggleGroup();

    private Playback playback;
    private BorderPane root;

    @Override
    public void start(Stage stage) {
        scene = new Scene(buildLayout(), 1180, 780);
        scene.getStylesheets().add(
                getClass().getResource("esu.css").toExternalForm());
        followSystemAppearance();

        wireControls();
        showEmptyState();

        stage.setScene(scene);
        stage.setTitle("ESU Visualizer");
        stage.show();
    }

    // ------------------------------------------------------------------
    // layout
    // ------------------------------------------------------------------

    private Parent buildLayout() {
        root = new BorderPane();
        root.setTop(new VBox(buildMenuBar(), buildTopBar()));
        root.setLeft(buildSidePanel());
        root.setCenter(buildTreeArea());
        root.setBottom(buildBottomBar());
        return root;
    }

    /**
     * The menu bar, which is where a desktop app is expected to keep its
     * commands and the only sensible home for their keyboard shortcuts.
     */
    private MenuBar buildMenuBar() {
        MenuBar bar = new MenuBar();
        // On macOS this puts the menu where every other application has it.
        bar.setUseSystemMenuBar(true);

        Menu file = new Menu("File");
        file.getItems().addAll(
                item("Open graph\u2026", KeyCode.O, this::chooseFile),
                item("Random graph", KeyCode.R, this::generateRandomGraph),
                new SeparatorMenuItem(),
                item("Save results\u2026", KeyCode.S, this::saveResults));

        Menu view = new Menu("View");
        showHistory.setAccelerator(new KeyCodeCombination(
                KeyCode.H, KeyCombination.SHORTCUT_DOWN));
        showShapes.setAccelerator(new KeyCodeCombination(
                KeyCode.G, KeyCombination.SHORTCUT_DOWN));
        // Both live in the same slot, so choosing one puts the other away.
        showHistory.selectedProperty().addListener((obs, was, now) -> {
            if (now) {
                showShapes.setSelected(false);
            }
            updateSidePanel();
        });
        showShapes.selectedProperty().addListener((obs, was, now) -> {
            if (now) {
                showHistory.setSelected(false);
            } else {
                pickShape(null);
            }
            updateSidePanel();
        });
        darkMode.selectedProperty().addListener(
                (obs, was, now) -> setDark(now));

        followStep.setSelected(true);
        followStep.selectedProperty().addListener(
                (obs, was, now) -> treeView.setFollow(now));

        view.getItems().addAll(
                showHistory,
                showShapes,
                followStep,
                darkMode,
                new SeparatorMenuItem(),
                item("Fit tree to window", KeyCode.DIGIT0, this::fitToWindow),
                item("Zoom in", KeyCode.EQUALS, () -> nudgeZoom(0.15)),
                item("Zoom out", KeyCode.MINUS, () -> nudgeZoom(-0.15)));

        Menu search = new Menu("Search");
        search.getItems().addAll(
                item("Play or pause", KeyCode.SPACE, this::togglePlayback),
                new SeparatorMenuItem(),
                item("Next step", KeyCode.RIGHT, () -> stepBy(1)),
                item("Previous step", KeyCode.LEFT, () -> stepBy(-1)),
                item("Back to start", KeyCode.HOME, () -> goTo(0)),
                item("Jump to end", KeyCode.END,
                        () -> goTo(session.getTotalSteps())));

        bar.getMenus().addAll(file, view, search);
        return bar;
    }

    /**
     * A menu item with a shortcut, disabled while there is nothing to act on.
     *
     * @param text     what the item says
     * @param key      the key, combined with the platform's shortcut modifier
     * @param action   what it does
     * @return the item
     */
    private MenuItem item(String text, KeyCode key, Runnable action) {
        MenuItem menuItem = new MenuItem(text);
        menuItem.setAccelerator(
                new KeyCodeCombination(key, KeyCombination.SHORTCUT_DOWN));
        menuItem.setOnAction(event -> action.run());
        return menuItem;
    }

    /** Play if paused, pause if playing. */
    private void togglePlayback() {
        playButton.setSelected(!playButton.isSelected());
        if (playButton.isSelected()) {
            startPlayback();
        } else {
            stopPlayback();
        }
    }

    /**
     * Step, stopping playback first so the two do not fight.
     *
     * @param delta 1 to advance, -1 to go back
     */
    private void stepBy(int delta) {
        if (session == null) {
            return;
        }
        stopPlayback();
        if (delta > 0) {
            session.stepForward();
        } else {
            session.stepBack();
        }
        refresh();
    }

    /**
     * Take the system's light or dark appearance, and keep taking it if the
     * user changes it while the app is open.
     *
     * The web version does this through a media query. This is the same idea:
     * the shapes carry style classes rather than colours set in code, so a
     * second stylesheet is all a theme takes.
     */
    private void followSystemAppearance() {
        Platform.getPreferences().colorSchemeProperty().addListener(
                (obs, was, now) -> darkMode.setSelected(now == ColorScheme.DARK));
        darkMode.setSelected(
                Platform.getPreferences().getColorScheme() == ColorScheme.DARK);
    }

    /**
     * @param dark true to layer the dark stylesheet over the base one
     */
    private void setDark(boolean dark) {
        String sheet = getClass().getResource("dark.css").toExternalForm();
        scene.getStylesheets().remove(sheet);
        if (dark) {
            scene.getStylesheets().add(sheet);
        }
    }

    /** Put whichever of the two side panels is asked for beside the tree. */
    private void updateSidePanel() {
        if (showHistory.isSelected()) {
            root.setRight(historyPanel);
        } else if (showShapes.isSelected()) {
            root.setRight(shapesPanel);
        } else {
            root.setRight(null);
        }
    }

    /**
     * Pick out the subgraphs of one shape in the tree.
     *
     * @param shape the shape chosen, or null to pick out nothing
     */
    private void pickShape(Shape shape) {
        if (session == null) {
            return;
        }
        treeView.setPicked(shape == null
                ? Set.of() : session.subgraphsWithShape(shape));
        shapesPanel.showInstances(shape == null
                ? List.of() : session.subgraphsOfShape(shape));
        refresh();
    }

    /**
     * Show one found subgraph in the input graph, so it is clear where in the
     * network that occurrence actually sits.
     *
     * @param vertices the subgraph's vertices
     */
    private void showInstanceInGraph(List<Integer> vertices) {
        graphPanel.highlight(vertices, List.of());
    }

    private Node buildTopBar() {
        buildSizePills();
        zoomSlider.setPrefWidth(120);

        Label sizeCaption = new Label("Subgraph size");
        sizeCaption.getStyleClass().add("caption");
        Label zoomCaption = new Label("Zoom");
        zoomCaption.getStyleClass().add("caption");

        HBox bar = new HBox(8, openButton, randomButton, saveButton,
                separator(), sizeCaption, sizePills,
                separator(), zoomCaption, zoomOutButton, zoomSlider,
                zoomInButton, fitButton);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(9, 12, 9, 12));
        bar.getStyleClass().add("top-bar");
        return bar;
    }

    /** One toggle per allowed subgraph size, as in the reference design. */
    private void buildSizePills() {
        for (int size = MIN_SUBGRAPH_SIZE; size <= MAX_SUBGRAPH_SIZE; size++) {
            ToggleButton pill = new ToggleButton(Integer.toString(size));
            pill.setUserData(size);
            pill.setToggleGroup(sizeGroup);
            pill.getStyleClass().add("pill");
            pill.setSelected(size == subgraphSize);
            sizePills.getChildren().add(pill);
        }
    }

    private Node buildSidePanel() {
        Label graphTitle = new Label("Input graph");
        graphTitle.getStyleClass().add("panel-title");
        graphPanel.setPrefHeight(280);
        graphPanel.setMinHeight(220);
        graphPanel.getStyleClass().add("graph-panel");

        subgraphLabel.getStyleClass().add("set-chosen");
        extensionLabel.getStyleClass().add("set-candidate");

        VBox sets = new VBox(4,
                labelled("Subgraph", subgraphLabel),
                labelled("Extension", extensionLabel));
        sets.setPadding(new Insets(10, 0, 6, 0));

        Label logTitle = new Label("This step");
        logTitle.getStyleClass().add("panel-title");
        VBox.setVgrow(stepLog, Priority.ALWAYS);

        VBox side = new VBox(6, graphTitle, graphPanel, sets, logTitle, stepLog);
        side.setPadding(new Insets(12));
        side.setPrefWidth(340);
        side.setMinWidth(340);
        side.getStyleClass().add("side-panel");
        return side;
    }

    private Node labelled(String name, Label value) {
        Label caption = new Label(name);
        caption.getStyleClass().add("caption");
        HBox row = new HBox(8, caption, value);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Node buildTreeArea() {
        busy.setMaxSize(48, 48);
        busy.setVisible(false);
        return new StackPane(treeView, busy);
    }

    private Node buildBottomBar() {
        statusLabel.getStyleClass().add("status-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label speedCaption = new Label("Speed");
        speedCaption.getStyleClass().add("caption");
        speedSlider.setPrefWidth(110);

        HBox bar = new HBox(8, statusLabel, spacer, startButton, prevButton,
                playButton, nextButton, endButton,
                separator(), speedCaption, speedSlider);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(9, 12, 9, 12));
        bar.getStyleClass().add("bottom-bar");

        VBox bottom = new VBox(buildLegend(), bar);
        return bottom;
    }

    private Node buildLegend() {
        HBox legend = new HBox(16,
                swatch("t-active", "working on"),
                swatch("t-complete", "subgraph found"),
                swatch("t-dead", "dead end"),
                swatch("t-pending", "still expanding"),
                separator(),
                caption("each box is a subgraph being built  \u00b7  "
                        + "the line down to it is how it got there"));
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.setPadding(new Insets(6, 12, 6, 12));
        legend.getStyleClass().add("legend-bar");
        return legend;
    }

    /**
     * A legend swatch, carrying the same style class as the boxes it stands
     * for, so the two cannot drift apart.
     *
     * @param styleClass the node state this describes
     * @param text       what that state means
     * @return the swatch and its caption
     */
    private Node swatch(String styleClass, String text) {
        Rectangle box = new Rectangle(15, 11);
        box.getStyleClass().addAll("t-box", styleClass);
        HBox entry = new HBox(6, box, caption(text));
        entry.setAlignment(Pos.CENTER_LEFT);
        return entry;
    }

    private Label caption(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("caption");
        return label;
    }

    private Node separator() {
        Separator separator = new Separator(Orientation.VERTICAL);
        separator.setPrefHeight(18);
        return separator;
    }

    // ------------------------------------------------------------------
    // wiring
    // ------------------------------------------------------------------

    private void wireControls() {
        // Advancing is the tick. Whenever playback stops, for any reason, the
        // button goes back to saying Play.
        playback = new Playback(() -> {
            if (session == null || !session.stepForward()) {
                return false;
            }
            refresh();
            return true;
        }, () -> {
            playButton.setSelected(false);
            playButton.setText("Play");
        });

        openButton.setOnAction(event -> chooseFile());
        randomButton.setOnAction(event -> generateRandomGraph());
        randomButton.setTooltip(new Tooltip(
                "Generate a random connected graph and run ESU on it"));
        saveButton.setOnAction(event -> saveResults());
        saveButton.setTooltip(new Tooltip(
                "Write the subgraphs found to a text file"));

        startButton.setTooltip(new Tooltip("Back to the first step"));
        endButton.setTooltip(new Tooltip("Jump to the finished tree"));
        fitButton.setTooltip(new Tooltip("Zoom so the whole tree fits"));

        startButton.setOnAction(event -> goTo(0));
        endButton.setOnAction(event -> goTo(session.getTotalSteps()));
        prevButton.setOnAction(event -> {
            session.stepBack();
            refresh();
        });
        nextButton.setOnAction(event -> {
            session.stepForward();
            refresh();
        });

        playButton.setOnAction(event -> {
            if (playButton.isSelected()) {
                startPlayback();
            } else {
                stopPlayback();
            }
        });
        speedSlider.valueProperty().addListener(
                (obs, was, now) -> playback.setRate(now.doubleValue()));

        zoomSlider.valueProperty().addListener(
                (obs, was, now) -> treeView.setZoom(now.doubleValue()));
        zoomInButton.setOnAction(event -> nudgeZoom(0.15));
        zoomOutButton.setOnAction(event -> nudgeZoom(-0.15));
        fitButton.setOnAction(event -> fitToWindow());

        sizeGroup.selectedToggleProperty().addListener((obs, was, now) -> {
            if (now == null) {
                was.setSelected(true);   // never leave the group empty
                return;
            }
            int chosen = (Integer) now.getUserData();
            if (chosen != subgraphSize) {
                subgraphSize = chosen;
                if (currentFile != null) {
                    loadGraph(currentFile);
                }
            }
        });

        stepLog.getSelectionModel().selectedIndexProperty()
                .addListener((obs, was, now) -> highlightFromLog(now.intValue()));

        // Entries are sentences, so wrap them rather than clipping to one
        // line behind a horizontal scrollbar.
        stepLog.setCellFactory(view -> new ListCell<>() {
            private final Label text = new Label();
            {
                text.setWrapText(true);
                text.getStyleClass().add("log-line");
                text.maxWidthProperty().bind(
                        stepLog.widthProperty().subtract(34));
                setGraphic(text);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                text.setText(empty ? "" : item);
                setGraphic(empty ? null : text);
            }
        });
    }

    // ------------------------------------------------------------------
    // loading
    // ------------------------------------------------------------------

    private void chooseFile() {
        File file = GraphFiles.chooseGraph(openButton.getScene().getWindow());
        if (file != null) {
            loadGraph(file);
        }
    }

    private void generateRandomGraph() {
        try {
            loadGraph(GraphFiles.writeRandomGraph(random));
        } catch (IOException e) {
            Alerts.displayCouldNotWrite(e.getMessage());
        }
    }

    private void saveResults() {
        try {
            GraphFiles.saveResults(saveButton.getScene().getWindow(),
                    session.getFinalTree(), subgraphSize,
                    currentFile == null ? null : currentFile.getName());
        } catch (IOException e) {
            Alerts.displayCouldNotWrite(e.getMessage());
        }
    }

    /**
     * Build a session for a file, off the UI thread.
     *
     * Enumerating is fast on the graphs this draws, but it is unbounded work
     * on someone else's file, and it used to run on the JavaFX thread and
     * freeze the window with no way out.
     *
     * @param file the graph file to load
     */
    private void loadGraph(File file) {
        UndirectedGraph graph = UndirectedGraph.fromFile(file);
        if (graph == null) {
            Alerts.displayUnreadableFile(file.getName());
            return;
        }

        stopPlayback();
        setBusy(true);
        currentFile = file;

        Task<EsuSession> work = new Task<>() {
            @Override
            protected EsuSession call() {
                return new EsuSession(graph, subgraphSize);
            }
        };
        work.setOnSucceeded(event -> {
            setBusy(false);
            adopt(work.getValue(), file);
        });
        work.setOnFailed(event -> {
            setBusy(false);
            Alerts.displayCouldNotRun(work.getException());
        });

        Thread thread = new Thread(work, "esu-enumerate");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Take on a freshly built session and draw it.
     *
     * @param built the session that finished enumerating
     * @param file  the file it came from
     */
    private void adopt(EsuSession built, File file) {
        session = built;
        historyPanel.setHistory(session.getHistory());
        shapesPanel.setGraph(session.getGraph());
        shapesPanel.setShapes(session.getShapes());
        treeView.setPicked(Set.of());
        treeView.setTree(session.getFinalTree(), subgraphSize);
        graphPanel.setGraph(session.getGraph());
        setControlsEnabled(true);
        goTo(0);
        zoomSlider.setValue(treeView.getZoom());

        if (session.getSubgraphCount() == 0) {
            Alerts.displayNoSubgraphs(subgraphSize);
        }
    }

    // ------------------------------------------------------------------
    // display
    // ------------------------------------------------------------------

    private void goTo(int step) {
        session.goToStep(step);
        refresh();
    }

    /** Redraw everything from the session. The single place that does. */
    private void refresh() {
        if (session == null) {
            return;
        }
        List<StepInfo> log = session.getCurrentLog();
        String active = log.isEmpty()
                ? null : log.get(log.size() - 1).getCallerSubgraph();
        focusId = active;
        drawTree(active, active);

        graphPanel.highlight(session.getActiveSubgraph(),
                session.getActiveExtension());
        subgraphLabel.setText(setText(session.getActiveSubgraph()));
        extensionLabel.setText(setText(session.getActiveExtension()));

        stepLog.getItems().clear();
        for (StepInfo entry : log) {
            stepLog.getItems().add(entry.render());
        }

        historyPanel.showStep(session.getCurrentStep());
        updateStatus();
        updateButtons();
    }

    private String setText(List<Integer> vertices) {
        if (vertices.isEmpty()) {
            return "—";
        }
        StringBuilder out = new StringBuilder();
        for (Integer vertex : vertices) {
            out.append(out.length() == 0 ? "" : ", ").append(vertex);
        }
        return out.toString();
    }

    private void updateStatus() {
        if (session == null) {
            statusLabel.setText("Open a graph, or press Random to generate one");
            return;
        }
        int found = session.getSubgraphCount();
        statusLabel.setText("Step " + session.getCurrentStep()
                + " of " + session.getTotalSteps()
                + "     ·     " + found + " subgraph"
                + (found == 1 ? "" : "s") + " of size " + subgraphSize);
    }

    private void updateButtons() {
        boolean atStart = session.getCurrentStep() <= 0;
        boolean atEnd = session.getCurrentStep() >= session.getTotalSteps();
        startButton.setDisable(atStart);
        prevButton.setDisable(atStart);
        nextButton.setDisable(atEnd);
        endButton.setDisable(atEnd);
    }

    /**
     * Draw the tree as it stands, tracing the path down to one node.
     *
     * @param activeId the node being worked on, highlighted amber
     * @param traceId  the node to trace back to the root, or null
     */
    private void drawTree(String activeId, String traceId) {
        treeView.show(session.getCurrentTree(), activeId, traceId);
    }



    private void highlightFromLog(int index) {
        List<StepInfo> log = session == null
                ? List.of() : session.getCurrentLog();
        if (session == null || index < 0 || index >= log.size()) {
            return;
        }
        drawTree(focusId, log.get(index).getCallerSubgraph());
    }

    private void showEmptyState() {
        setControlsEnabled(false);
        updateStatus();
    }

    private void setControlsEnabled(boolean loaded) {
        for (Node control : new Node[]{startButton, prevButton, nextButton,
                endButton, playButton, speedSlider, fitButton, zoomInButton,
                zoomOutButton, zoomSlider, saveButton}) {
            control.setDisable(!loaded);
        }
    }

    private void setBusy(boolean running) {
        busy.setVisible(running);
        openButton.setDisable(running);
        randomButton.setDisable(running);
        sizePills.setDisable(running);
        if (running) {
            setControlsEnabled(false);
        }
    }

    // ------------------------------------------------------------------
    // zoom and playback
    // ------------------------------------------------------------------

    /**
     * Move the zoom by a step, staying inside what the view supports.
     *
     * @param delta amount to add to the current zoom factor
     */
    private void nudgeZoom(double delta) {
        zoomSlider.setValue(Math.max(TreeView.MIN_ZOOM,
                Math.min(TreeView.MAX_ZOOM, zoomSlider.getValue() + delta)));
    }

    /** Fit the tree, then bring the slider back in step with it. */
    private void fitToWindow() {
        treeView.fit();
        zoomSlider.setValue(treeView.getZoom());
    }


    private void startPlayback() {
        if (session == null) {
            playButton.setSelected(false);
            return;
        }
        playback.setRate(speedSlider.getValue());
        playback.start();
        playButton.setText("Pause");
    }

    private void stopPlayback() {
        if (playback != null) {
            playback.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
