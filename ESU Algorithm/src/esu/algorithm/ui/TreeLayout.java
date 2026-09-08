/*
 * Where every box and connecting line goes.
 *
 * Split out of the old AuxilaryClass, which mixed geometry, drawing and two
 * unused helpers in one 617-line pile of statics.
 */
package esu.algorithm.ui;

import esu.algorithm.ESUNode;
import esu.algorithm.ESUTree;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * Class TreeLayout
 *
 * Positions the search tree: how big a node box has to be to hold its text,
 * where each box sits, and how a box connects to its parent. Knows nothing
 * about colour or state.
 */
public class TreeLayout {

    /** Size of a node box, set by setNodeDims from the widest text. */
    static public double nodeWidth;
    static public double nodeHeight;

    /** Extent of the whole tree, set by getTreeSpace. */
    static public double treeWidth;
    static public double treeHeight;

    /** Space inside a box, around its text. */
    static final public double innerPaddingX = 5;
    static final public double innerPaddingY = 5;

    /** Space between boxes, and between levels. */
    static final public double outerPaddingX = 15;
    static final public double outerPaddingY = 45;

    static public final double FONT_HEIGHT = 12;
    /** Advance width of the monospaced face, used to centre text. */
    static public final double FONT_WIDTH = 7.2;
    /** Monospaced, so FONT_WIDTH stays a true measure of text width. */
    static public final String FONT_FAMILY = "Menlo";

    private TreeLayout() {
    }

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
            currentMax = Math.max(currentMax, tree[level].size());
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
                
                maxChars = Math.max(maxChars, 
                        nodes[level].get(node).getSubgraphAsString().length());
                maxChars = Math.max(maxChars, 
                        nodes[level].get(node).getPossibleStepsAsString().length());
                maxChars = Math.max(maxChars, 
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
    
}
