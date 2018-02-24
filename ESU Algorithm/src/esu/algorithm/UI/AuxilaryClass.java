/* **************************************************************************
 *
 * Auxilary Class:
 * Just some file to work on geometric calculations and concepts.
 *
 ************************************************************************** */

package esu.algorithm.UI;

import esu.algorithm.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Just some class as a temporary development environment for writing functions
 * that will be useful for printing rectangles and shit to the screen.
 * 
 * Comments are sparse, ask Nate for any questions. Class-level variables are
 * declared to prevent syntax errors, but are not initialized and are only 
 * place-holders for variables that will be implemented outside of the scope
 * of these functions.
 * 
 * THIS CLASS SHOULD BE ACTUALLY BE USED, IT IS JUST A DEVELOPMENT ENVIRONMENT
 * FOR FUNCTIONS THAT MAY BE USEFUL ELSEWHERE IN OUR CODE.
 * 
 * @author Biohazard
 */
public class AuxilaryClass {
    
    static public double nodeWidth;
    static public double nodeHeight;
    static public double treeWidth;
    static public double treeHeight;
    
    static public double innerPaddingX = 5;
    static public double innerPaddingY = 5;
    static public double outerPaddingX = 15;
    static public double outerPaddingY = 100;
    
    static public final double FONT_HEIGHT = 20;
    static public final double FONT_WIDTH = 10;
    
    //empty consrtuctor, does nothing
    public AuxilaryClass(){/* Nothing to see here... Move along. */}
    
    /*       Helper functions       */
    public static double max(double d0, double d1){
        if(d0 > d1){
            return d0;
        }
        return d1;
    }
    public static int max(int d0, int d1){
        if(d0 > d1){
            return d0;
        }
        return d1;
    }
    /* ******************************* */
    
    //calculates the tree space
    public static Rectangle getTreeSpace(ESUTree finalState){
        
        //bounding Rectengle
        Rectangle out = new Rectangle();
        
        //get tree as lists of Nodes
        List<ESUNode>[] tree = finalState.getNodesByLevel();
        
        //current longest level
        int currentMax = 0;
        
        //find longest level
        for(int level = 0; level < tree.length; level++){
            currentMax = max(currentMax, tree[level].size());
        }
        
        //set width relative to the longest level
        out.setWidth(currentMax * nodeWidth + (currentMax + 1) * outerPaddingX);
        
        //set height relative to the tree height
        out.setHeight( tree.length * nodeHeight + (tree.length + 1) * outerPaddingY);
        
        //set tree dimensions for static variables
        treeWidth = out.getWidth();
        treeHeight = out.getHeight();
        
        return out;
    }
    
    //set node dimensions:
    //height based on FONT_HEIGHT and innerPaddingY
    //width based on whichever Node has the longest list to display,
    //  FONT_WIDTH and innerPaddingX
    public static void setNodeDims(ESUTree finalState){
        int maxChars = 6; //six caharcters in "[root]"
        ArrayList<ESUNode>[] nodes = finalState.getNodesByLevel();
        for(int level = 1; level < finalState.getMaxHeight() + 1; level++){
            for(int node = 0; node < nodes[level].size(); node++){
                maxChars = max(maxChars, nodes[level].get(node).getSubgraphAsString().length());
                maxChars = max(maxChars, nodes[level].get(node).getPossibleStepsAsString().length());
                maxChars = max(maxChars, nodes[level].get(node).getSubgraphNeighborsAsString().length());
            }
        }
        nodeWidth = maxChars*FONT_WIDTH + 2*innerPaddingX;
        nodeHeight = FONT_HEIGHT * 3 + innerPaddingX * 4;
    }
    
    //calculates a Rectangle for each node in the tree, relative to the tree space
    public static ArrayList<Rectangle>[] getRectangles(ESUTree finalState){
        
        //set up variables
        ArrayList<Rectangle>[] out = new ArrayList[finalState.getMaxHeight() + 1];
        ArrayList<ESUNode>[] nodes = finalState.getNodesByLevel();
        
        //for each level
        for(int level = 0; level < out.length; level++){
            
            ////total node width on this level
            double totalNodeWidth = nodeWidth*nodes[level].size();
            
            //padding to be evenly distributed on current level
            double levelPadding = (treeWidth - totalNodeWidth) / (nodes[level].size() + 1);
            
            //create arraylist @ level
            out[level] = new ArrayList<>();
            
            //make each Rectangle for each node in this level
            for(int node = 0; node < nodes[level].size(); node++){
                Rectangle cell = new Rectangle();
                cell.setWidth(nodeWidth);
                cell.setHeight(nodeHeight);
                cell.setX(nodeWidth * node + (levelPadding* (node + 1)));
                cell.setY(nodeHeight * level + outerPaddingY * (level + 1));
                cell.setFill(null);
                cell.setStroke(Color.BLACK);
                out[level].add(cell);
            }
        }
        
        //return out
        return out;
    }
    
    /**
    * deep copy for Rectangle lists
    * @deprecated 
    * */
    public static ArrayList<Rectangle>[] copyRectangles(ArrayList<Rectangle>[] in){
        
        //output variable
        ArrayList<Rectangle>[] out = new ArrayList[in.length];
        
        //deep copy
        for(int level = 0; level < in.length; level++){
            out[level] = new ArrayList<>();
            for(int rect = 0; rect < in[level].size(); rect++){
                Rectangle copy = new Rectangle();
                copy.setX(in[level].get(rect).getX());
                copy.setY(in[level].get(rect).getY());
                copy.setWidth(in[level].get(rect).getWidth());
                copy.setHeight(in[level].get(rect).getHeight());
                out[level].add(copy);
            }
        }    
        return out;
    }
    
    //creates a text object for each of the three text fields to show for the
    //ESUNode, relative to the Rectangle's coordinates.
    public static Text[] getText(Rectangle rect, ESUNode node){
        
        //Text array for returning
        Text out[];
        
        //create font
        Font font = new Font("Times New Roman", FONT_HEIGHT);
        
        //if NOT root, get Text for the three lists
        if(node.getLevel() != 0){
            
            //Store 3 Text fields for each ESUNode
            out  = new Text[3];
            
            //create Text object for Subgraph
            String text = node.getSubgraphAsString();
            out[0] = new Text(rect.getX() + rect.getWidth()/2 - text.length() * FONT_WIDTH / 2, 
                    rect.getY() + innerPaddingY + FONT_HEIGHT, 
                    text);
            //set font
            out[0].setFont(font);

            //create Text object for Possible Steps
            text = node.getPossibleStepsAsString();
            out[1] = new Text(rect.getX() + rect.getWidth()/2 - text.length() * FONT_WIDTH / 2, 
                    rect.getY() + (innerPaddingY + FONT_HEIGHT) * 2, 
                    text);
            //set font
            out[1].setFont(font);

            //create Text object for SubgraphNeighbors
            text = node.getSubgraphNeighborsAsString();
            out[2] = new Text(rect.getX() + rect.getWidth()/2 - text.length() * FONT_WIDTH / 2, 
                    rect.getY() + (innerPaddingY + FONT_HEIGHT) * 3, 
                    text);
            //set font
            out[2].setFont(font);
            
            //test for leaves, they have no subgraph neighbors or possible steps
            if(out[1].getText().length() == 2 && out[2].getText().length() == 2){
                Text temp = out[0];
                out = new Text[1];
                out[0] = temp;
                out[0].setY(rect.getY() + (innerPaddingY + FONT_HEIGHT) * 2);
            }
        
        }
        //if Root, only get Text for 
        else{
            
            //only one Text fields for the Root
            out = new Text[1];
            
            //root's diplay text
            String text = "[root]";
            out[0] = new Text(rect.getX() + rect.getWidth()/2 - text.length() * FONT_WIDTH / 2, 
                    rect.getY() + (innerPaddingY + FONT_HEIGHT) * 2, 
                    text);
            //set font
            out[0].setFont(font);
            
        }
        //return result
        return out;
    }
    
    //returns drawable Nodes (Rectangles and Text fields)
    public static List<Node> getPrintables(ArrayList<Rectangle>[] rects, ArrayList<ESUNode>[] currentNodes, ArrayList<ESUNode>[] finalNodes){
        
        //List to hold the printable Nodes
        LinkedList<Node> out = new LinkedList<>();
        
        //for each level of ESUNodes
        for(int level = 0; level < currentNodes.length; level++){
            
            //for each ESUNode in the current level
            for(int node = 0; node < currentNodes[level].size(); node++){
                
                //shallow copy... Might need a deep copy...? not sure
                Rectangle rect = rects[level].get(node);
                
                //get the line to the parent
                Line line = getLineToParent(rects, finalNodes, currentNodes[level].get(node));
                
                //get text fields for current Rectangle
                Text[] text = getText(rect, currentNodes[level].get(node));
                
                //add Rectangles and Lines to the front of the list
                out.addFirst(rect);
                if(line != null)
                    out.addFirst(line);
                
                //add text fields into the end of the list
                //hopefully this ensures the text draws on top
                //(if objects are printed in List ordering)
                for(Text t: text){
                    out.addLast(t);
                }
            }
            
        }
        
        //return result
        return out;
    }
    
    //calculates the line between the parameter ESUNode and it's parent, using
    //the Rectangles and ESUNodes to locate screen position.
    public static Line getLineToParent(ArrayList<Rectangle>[] rects, ArrayList<ESUNode>[] finalNodes, ESUNode node){
        
        //if root, return null
        if(node.getLevel() < 1){
            return null;
        }
        
        //line start/end coordinates
        double startX = 0.;
        double startY = 0.;
        double endX = 0.;
        double endY = 0.;
        
        //my level index
        int levelIndex = 0;
        
        //count my level index in the tree (via nodes)
        for(ESUNode curr : finalNodes[node.getLevel()]){
            if(curr.getSubgraphAsString().equals(node.getSubgraphAsString())){
                break;
            }
            levelIndex++;
        }
        
        //my parent's index
        int parentLevelIndex = 0;
        
        //count my parent's level index in tree (via nodes)
        for(ESUNode curr : finalNodes[node.getLevel() - 1]){
            if(curr.getSubgraphAsString().equals(node.getParent().getSubgraphAsString())){
                break;
            }
            parentLevelIndex++;
        }
        
        //number of siblings from parent
        //int numSiblings = node.getParent().getChildren().size();
        int numSiblings = finalNodes[node.getLevel()-1].get(parentLevelIndex).getChildren().size();
        
        //my number in my parents children
        int siblingNum = 0;
        
        //find my index in my parent's children
        for(ESUNode sibling : node.getParent().getChildren()){
            if (sibling == node){
                break;
            }
            siblingNum++;
        }
        
        //helper variable for splitting the width of the Rectangles by number
        //of siblings
        double levelDivision = nodeWidth / (numSiblings + 1);
        
        //start Y, top of a rectangle on my level
        startY = rects[node.getLevel()].get(0).getY();
        
        //start X, X value of rectangle, offset by my position in my
        //parents children
        startX = rects[node.getLevel()].get(levelIndex).getX() + nodeWidth - (levelDivision * (siblingNum + 1) );
        
        //end Y, bottom of a rectangle of the parent's level
        endY = rects[node.getLevel()-1].get(0).getY() + nodeHeight;
        
        //end X, X value of parent rectangle, opposite offset from my X offest
        endX = rects[node.getLevel()-1].get(parentLevelIndex).getX() + (levelDivision * (siblingNum + 1) );
        
        //return Line
        return new Line(startX, startY, endX, endY);
    }
    
    /**
     *      NOT USED CURRENTLY
     *          @deprecated
     */
    public static void drawTo(GraphicsContext gc, List<Node> drawables){
        gc.clearRect(0, 0, treeWidth, treeHeight);
        for(Node drawable : drawables){
            if(drawable instanceof Line){
                Line line = (Line)drawable;
                gc.strokeLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
            }
            if(drawable instanceof Rectangle){
                Rectangle rect = (Rectangle)drawable;
                gc.strokeRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            }
            if(drawable instanceof Text){
                Text text = (Text) drawable;
                gc.strokeText(text.getText(), text.getX(), text.getY(), text.getText().length()*FONT_WIDTH);
            }
        }
    }
}
