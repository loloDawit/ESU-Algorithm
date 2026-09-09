/*
 * The whole search, one row per step.
 */
package esu.algorithm.ui;

import esu.algorithm.EsuSession;
import java.util.List;
import java.util.function.IntConsumer;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Class HistoryPanel
 *
 * Every step of the search in order, with the one on screen picked out.
 * Choosing a row goes to that step.
 *
 * The step log beside it shows what the algorithm said during one step and
 * then throws it away. This is the other half: where you are in the whole run,
 * and a way back to any part of it.
 */
public class HistoryPanel extends VBox {

    private final ListView<EsuSession.Step> steps = new ListView<>();
    private final IntConsumer goToStep;
    /** Suppresses the listener while the panel is following the session. */
    private boolean following;

    /**
     * @param goToStep called with the step a chosen row represents
     */
    public HistoryPanel(IntConsumer goToStep) {
        this.goToStep = goToStep;

        Label title = new Label("Step history");
        title.getStyleClass().add("panel-title");

        steps.getStyleClass().add("history-list");
        steps.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(EsuSession.Step step, boolean empty) {
                super.updateItem(step, empty);
                setText(empty || step == null ? null : step.toString());
            }
        });
        steps.getSelectionModel().selectedItemProperty()
                .addListener((obs, was, now) -> {
                    if (!following && now != null) {
                        goToStep.accept(now.number());
                    }
                });

        VBox.setVgrow(steps, Priority.ALWAYS);
        getChildren().addAll(title, steps);
        setSpacing(6);
        getStyleClass().add("history-panel");
    }

    /**
     * Show a different search.
     *
     * @param history one entry per step
     */
    public void setHistory(List<EsuSession.Step> history) {
        steps.setItems(FXCollections.observableArrayList(history));
    }

    /**
     * Follow the session without treating it as the user choosing a row.
     *
     * @param step the step now on screen, 0 meaning before the search starts
     */
    public void showStep(int step) {
        following = true;
        if (step <= 0) {
            steps.getSelectionModel().clearSelection();
        } else {
            steps.getSelectionModel().select(step - 1);
            steps.scrollTo(Math.max(0, step - 3));
        }
        following = false;
    }

    public void clear() {
        steps.setItems(FXCollections.observableArrayList());
    }
}
