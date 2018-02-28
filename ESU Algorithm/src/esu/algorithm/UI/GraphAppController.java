/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esu.algorithm.UI;

import static java.lang.Math.max;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

/**
 * FXML Controller class
 *
 * @author dawitabera
 */
public class GraphAppController implements Initializable {
    @FXML
    private Button generate;
    @FXML
    private Pane canvas;
    private static final int MIN = 6;
    private static final int MAX = 10;
    int numberOfVertices;
    int numberOfEdges;
    private Edge edges[];
    private DrawNode nodes[];
    private final Random random = new Random();
    int randomNum = -2;
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert generate != null : "fx:id=\"generate\" was not injected: check FXML file 'Graph.fxml'.";
        generate.setOnAction(this::handleGenerateAction);
    }    
//
    @FXML
    private void handleGenerateAction(ActionEvent event) {
        canvas.getChildren().clear();
        //initialize pseudo random number of vertices
        numberOfVertices    = max(random.nextInt(MAX),MIN); 
        numberOfEdges       = 0;
        
        int randomCoordinateX;
        int randomCoordinateY;
        int connectedWeight;
        
        //maximum number of edges = v*(v-1)/2
        edges = new Edge[(numberOfVertices*(numberOfVertices-1)/2)];
        nodes = new DrawNode[numberOfVertices];
        
        Group groupNodes        = new Group();
        Group groupNodeNames    = new Group();
        Group groupEdges        = new Group();
        Group groupEdgeWeight   = new Group();
        
        Circle circle[] = new Circle[numberOfVertices];
        Line line[]     = new Line[(numberOfVertices*(numberOfVertices-1)/2)];
        Text text[]     = new Text[numberOfVertices];
        Text weight[]   = new Text[(numberOfVertices*(numberOfVertices-1)/2)];
        
        //initialize vertices with random locations within the canves bounds in Pixel
        for(int i = 0; i < numberOfVertices; i++) {
            randomCoordinateX = random.nextInt(580);
            randomCoordinateY = random.nextInt(600);
            //character as Node-Name starting at A (ASCII)
            char newCharacter = (char)(i+65);
            nodes[i]    = new DrawNode(randomCoordinateX, randomCoordinateY, newCharacter);
            circle[i]   = createCircle(randomCoordinateX,randomCoordinateY);
            text[i]     = charToText(randomCoordinateX,randomCoordinateY, newCharacter);
            //add vertices and their names to groups
            groupNodes.getChildren().add(circle[i]);
            groupNodeNames.getChildren().add(text[i]);
        }
        
        //initialize edges with two nodes each and random weight
        // number of edges in a simple graph <= v*(v-1)/2
        int skipEdge = 2;
        for(int i = 0; i < numberOfVertices; i=i+skipEdge) { //n/2 every 2nd edge matrix row
            for(int j = 1; j < numberOfVertices; j=j+skipEdge) {//(n-1)/2 every 2nd edge matrix column
                //connectedWeight (1)deduct random number or (2)add 1 to create (1)e <= v*(v-1)/2 or (2)e = v*(v-1)/2 edges 
                //however, deducting causes pendant or even isolated vertices
                //see initilization of integer randomNum at the top
                connectedWeight = random.nextInt(15)+(randomNum); 
                
                if(connectedWeight > 0) {                 
                    edges[numberOfEdges] = new Edge(nodes[i], nodes[j], connectedWeight);
                    numberOfEdges++;
                }
            }
        }
        
        //create lines as edges with line-start location of vertex one and line-end location of vertex two
        for(int i = 0; i < numberOfEdges; i++) {
            int weightCharacter    = edges[i].getWeight();
            
            line[i]     = createLine(edges[i].getNodeFrom().getLocationX(),
                                     edges[i].getNodeFrom().getLocationY(), 
                                     edges[i].getNodeTo().getLocationX(),
                                     edges[i].getNodeTo().getLocationY());
            //Edge weight text location in the middle of the line
            weight[i]   = intToText((edges[i].getNodeFrom().getLocationX()/2 + edges[i].getNodeTo().getLocationX()/2),
                                     (edges[i].getNodeFrom().getLocationY()/2 + edges[i].getNodeTo().getLocationY()/2), 
                                      weightCharacter);
            
            //add edges(lines) and their weight to groups
            groupEdges.getChildren().add(line[i]);
            groupEdgeWeight.getChildren().add(weight[i]);
        }
        //add in order, groups added later are added on top
        canvas.getChildren().addAll(groupEdges, groupNodes, groupNodeNames, groupEdgeWeight);
        //pathoutput.setText("Graph is generated!");
    }
    private Line createLine(double coordinateNodeOneX, double coordinateNodeOneY, double coordinateNodeTwoX, double coordinateNodeTwoY) {
        Line line = new Line(coordinateNodeOneX, coordinateNodeOneY, coordinateNodeTwoX, coordinateNodeTwoY);
        line.setStroke(Color.BLUE);
        line.setStrokeWidth(1);
        return line;
    }
     private Circle createCircle(double coordinateX, double coordinateY) {
        //Creates a new instance of Circle with a specified position, radius and fill
        final Circle circle = new Circle(coordinateX, coordinateY, 10, Color.WHITE);
        circle.setStroke(Color.BLUE);
        circle.setStrokeWidth(1.5);
        circle.setStrokeType(StrokeType.OUTSIDE);
        return circle;
    }
        private Text charToText(double coordinateX, double coordinateY, char newCharacter) {
        final Text text = new Text(coordinateX-6.5, coordinateY+6.5,"");
        char newValue = newCharacter;
        if (newValue == 0) {
            newValue = 'A';
        }
        text.setText("" + newValue);
        return formatText(text);
    }
    
     /**
     * convert integer to text and set layout and location 
     * for the text of the edge-weights
     * 
     * @param coordinateX X coordinate of the text location
     * @param coordinateY Y coordinate of the text location
     * @param newWeight integer value for the edge
     * @return 
     */
    private Text intToText(double coordinateX, double coordinateY, int newWeight) {
        final Text text = new Text(coordinateX-6.5, coordinateY+6.5,"");
        text.setText("" + newWeight);
        return formatText(text);
    }
    // helper-function to format the text
    private Text formatText(Text text)
    {
        text.setStroke(Color.BLACK);
        text.setStrokeWidth(1.5);
        text.setFont(new Font(20));
        text.setBoundsType(TextBoundsType.VISUAL);
        return text;
    }
    
}
