/* **************************************************************************
 * File: AuxilaryClass.java
 * 
 * A static class for running calculations needed for graphically representing
 * an ESUTree as a series of Rectangles, Lines and Text fields.
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

/** ***********************************************************************
 * Class:       Auxilary Class
 * Purpose: To house the logic associated with the geometric representation
 * of an ESUTree as Rectangles, Lines and Text fields.
 * 
 * @author Biohazard
 ************************************************************************* */
public class AuxilaryClass {
    
    //"session variables"
    static public double nodeWidth;
    static public double nodeHeight;
    static public double treeWidth;
    static public double treeHeight;
    
    //Visual padding
    static final public double innerPaddingX = 5;
    static final public double innerPaddingY = 5;
    static final public double outerPaddingX = 15;
    static final public double outerPaddingY = 100;
    
    //font dimensions
    static public final double FONT_HEIGHT = 20;
    static public final double FONT_WIDTH = 10;
    
    /** ************************************************
     * Constructor
     * Does nothing.
     ************************************************** */
    //empty consrtuctor, does nothing
    public AuxilaryClass(){/* Nothing to see here... Move along. */}
    
    
    /*******************************************************
                    Helper functions       
    *******************************************************/
    
    /** *********************************************************************
     * Max:
     * 
     * A simple method for finding the max of two numbers.
     * 
     * @param d0 - first candidate.
     * @param d1 - second candidate
     * 
     * @return The larger of the two candidate values.
     *********************************************************************** */
    public static double max(double d0, double d1){
        if(d0 > d1){
            return d0;
        }
        return d1;
    }
    
    /** *********************************************************************
     * Max:
     * 
     * A simple method for finding the max of two numbers.
     * 
     * @param d0 - first candidate.
     * @param d1 - second candidate
     * 
     * @return The larger of the two candidate values.
     *********************************************************************** */
    public static int max(int d0, int d1){
        if(d0 > d1){
            return d0;
        }
        return d1;
    }
    /* *****************************************************************
    *********************************************************************/
    
    
    /** ********************************************************************
     * Get Tree Space:
     * 
     * This function calculates and allocates screen space based on the final
     * state of the ESUTree.
     * 
     * This method initializes the tree space that encapsulates the drawing
     * area. This function requires that the node dimensions have been
     * determined ahead of time. Rightfully so:
     * THIS FUNCTION SHOULD BE CALLED AFTER "SET NODE DIMS"
     * 
     * @see setNodeDims( ... )
     * 
     * @param finalState - The Final state of the ESUTree
     * 
     * @return A Rectangle to encase the total space the ESUTree will take
     *              up.
     ********************************************************************* */
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
    
    
    /** ********************************************************************
     * Set Node Dimensions:
     * 
     * Calculates the dimensions of the nodes based on the final state of
     * the ESUTree. 
     * 
     * THIS FUNCTION IS REQUIRED TO BE CALLED BEFORE THE OTHER STATIC 
     * FUNCTIONS OF THIS CLASS.
     * 
     * The height is static relative to the font height and inner padding.
     * The width is based on the widest displayed text for the ESUTree.
     * 
     * @param finalState - The final state of the ESUTree to display
     * 
     ********************************************************************* */
    public static void setNodeDims(ESUTree finalState){
        int maxChars = 6; //six caharcters in "[root]"
        ArrayList<ESUNode>[] nodes = finalState.getNodesByLevel();
        for(int level = 1; level < finalState.getMaxHeight() + 1; level++){
            for(int node = 0; node < nodes[level].size(); node++){
                
                maxChars = max(maxChars, 
                        nodes[level].get(node).getSubgraphAsString().length());
                maxChars = max(maxChars, 
                        nodes[level].get(node).getPossibleStepsAsString().length());
                maxChars = max(maxChars, 
                        nodes[level].get(node).getSubgraphNeighborsAsString().length());
            }
        }
        nodeWidth = maxChars*FONT_WIDTH + 2*innerPaddingX;
        nodeHeight = FONT_HEIGHT * 3 + innerPaddingX * 4;
    }
    
    //calculates a Rectangle for each node in the tree, relative to the tree space
    /** ***********************************************************************
     * Get Rectangles:
     * 
     * Pre-determines the location of each Rectangle for each node in the
     * final state of the ESUTree. Stores the Rectangles as an array of
     * ArrayLists where each index of the array hold all the Rectangles for
     * that level of the tree. The returned array should be used in conjunction
     * with the other static functions of this file.
     * 
     * @param finalState - The final state of the ESUTree to display.
     * 
     * @return - An array of ArrayLists of Rectangles (lol)
     *              representing all Node in the Tree.
     ************************************************************************ */
    public static ArrayList<Rectangle>[] getRectangles(ESUTree finalState){
        
        //set up variables
        ArrayList<Rectangle>[] out = 
                new ArrayList[finalState.getMaxHeight() + 1];
        
        ArrayList<ESUNode>[] nodes = finalState.getNodesByLevel();
        
        //for each level
        for(int level = 0; level < out.length; level++){
            
            ////total node width on this level
            double totalNodeWidth = nodeWidth*nodes[level].size();
            
            //padding to be evenly distributed on current level
            double levelPadding = (treeWidth - totalNodeWidth) / 
                    (nodes[level].size() + 1);
            
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
    
    /** *********************** NOT USED ***********************************
    * Copy Rectangles:
    * Deep copy for Rectangle lists. 
    * *************
    * @deprecated *
    ********************************************************************* */
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
    
    
    /** *********************************************************************
     * Get Text:
     * 
     * Creates the Text objects for each of the three text fields to show
     * for the ESUNode. The text coordinates are calculated relative to the
     * Rectangle accepted.
     * 
     * @param rect - The Rectangle space for the ESUNode whose test we are
     *                  calculating.
     * @param node - The ESUnode to supply the text fields.
     * 
     * @return - A Text array of the text fields calculated.
     *********************************************************************** */
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
            out[0] = new Text(rect.getX() + rect.getWidth()/2 - 
                        text.length() * FONT_WIDTH / 2, 
                    rect.getY() + innerPaddingY + FONT_HEIGHT, 
                    text);
            //set font
            out[0].setFont(font);

            //create Text object for Possible Steps
            text = node.getPossibleStepsAsString();
            out[1] = new Text(rect.getX() + rect.getWidth()/2 - 
                        text.length() * FONT_WIDTH / 2, 
                    rect.getY() + (innerPaddingY + FONT_HEIGHT) * 2, 
                    text);
            //set font
            out[1].setFont(font);

            //create Text object for SubgraphNeighbors
            text = node.getSubgraphNeighborsAsString();
            out[2] = new Text(rect.getX() + rect.getWidth()/2 - 
                        text.length() * FONT_WIDTH / 2, 
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
            out[0] = new Text(
                    rect.getX() + rect.getWidth()/2 - 
                                text.length() * FONT_WIDTH / 2, 
                    rect.getY() + (innerPaddingY + FONT_HEIGHT) * 2, 
                    text);
            //set font
            out[0].setFont(font);
            
        }
        //return result
        return out;
    }
    
    
    /** ***********************************************************************
     * Get Printables:
     * 
     * Gets the printable Nodes (javafx) for displaying the ESUTree described
     * by finalNodes.
     * 
     * @param rects - The lits of Rectangles of all ESUNodes
     * @param currentNodes - The current nodes of the Tree's state to print.
     * @param finalNodes - The final state of the ESUTree as ESUNode lists.
     * 
     * @return A list of drawable "Node" objects for use in javafx.
     ************************************************************************* */
    public static List<Node> getPrintables(ArrayList<Rectangle>[] rects, 
            ArrayList<ESUNode>[] currentNodes, ArrayList<ESUNode>[] finalNodes){
        
        //List to hold the printable Nodes
        LinkedList<Node> out = new LinkedList<>();
        
        //for each level of ESUNodes
        for(int level = 0; level < currentNodes.length; level++){
            
            //for each ESUNode in the current level
            for(int node = 0; node < currentNodes[level].size(); node++){
                
                //shallow copy... Might need a deep copy...? not sure
                Rectangle rect = rects[level].get(node);
                
                //get the line to the parent
                Line line = getLineToParent(rects, finalNodes, 
                        currentNodes[level].get(node));
                
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
    
    
    /** **********************************************************************
     * Get Line To Parent:
     * 
     * Gets the line from the node to it's parent. The lines are calculated in
     * such a way that the space on the bottom of the parent's rectangle
     * is divided between it's children's lines.
     * 
     * @param rects - The location of all Rectangles for Nodes
     * @param finalNodes - The final state of the ESUTree as ESUNode lists
     * @param node - The current Node to draw the line to its parent.
     * 
     * @return - The Line object that will link the node to its parent.
     *********************************************************************** */
    public static Line getLineToParent(ArrayList<Rectangle>[] rects, 
            ArrayList<ESUNode>[] finalNodes, ESUNode node){
        
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
            if(curr.getSubgraphAsString().equals(
                    node.getSubgraphAsString())){
                break;
            }
            levelIndex++;
        }
        
        //my parent's index
        int parentLevelIndex = 0;
        
        //count my parent's level index in tree (via nodes)
        for(ESUNode curr : finalNodes[node.getLevel() - 1]){
            if(curr.getSubgraphAsString().equals(
                    node.getParent().getSubgraphAsString())){
                break;
            }
            parentLevelIndex++;
        }
        
        //number of siblings from parent
        //int numSiblings = node.getParent().getChildren().size();
        int numSiblings = finalNodes[node.getLevel()-1].get(
                parentLevelIndex).getChildren().size();
        
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
        startX = rects[node.getLevel()].get(levelIndex).getX() 
                + nodeWidth - (levelDivision * (siblingNum + 1) );
        
        //end Y, bottom of a rectangle of the parent's level
        endY = rects[node.getLevel()-1].get(0).getY() + nodeHeight;
        
        //end X, X value of parent rectangle, opposite offset from my X offest
        endX = rects[node.getLevel()-1].get(parentLevelIndex).getX() 
                + (levelDivision * (siblingNum + 1) );
        
        //return Line
        return new Line(startX, startY, endX, endY);
    }
    
    /** ****************************************************************
     *                      NOT USED CURRENTLY
     *                          @deprecated
     ****************************************************************** */
    public static void drawTo(GraphicsContext gc, List<Node> drawables){
        gc.clearRect(0, 0, treeWidth, treeHeight);
        for(Node drawable : drawables){
            if(drawable instanceof Line){
                Line line = (Line)drawable;
                gc.strokeLine(line.getStartX(), line.getStartY(), 
                        line.getEndX(), line.getEndY());
            }
            if(drawable instanceof Rectangle){
                Rectangle rect = (Rectangle)drawable;
                gc.strokeRect(rect.getX(), rect.getY(), 
                        rect.getWidth(), rect.getHeight());
            }
            if(drawable instanceof Text){
                Text text = (Text) drawable;
                gc.strokeText(text.getText(), text.getX(), 
                        text.getY(), text.getText().length()*FONT_WIDTH);
            }
        }
    }
}