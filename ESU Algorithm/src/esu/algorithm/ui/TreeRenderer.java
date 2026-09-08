/*
 * Turning a laid-out tree into shapes, and colouring them by what the
 * algorithm did with each node.
 */
package esu.algorithm.ui;

import esu.algorithm.ESUNode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Class TreeRenderer
 *
 * Builds the shapes for a tree and paints each node according to its state:
 * being worked on, finished as a result, or dead.
 */
public class TreeRenderer {

    /** A node that has not been reached yet, or is still expanding. */
    public static final Color PENDING_FILL = Color.web("#ffffff");
    public static final Color PENDING_STROKE = Color.web("#7a8699");
    /** The node the current step is working on. */
    public static final Color ACTIVE_FILL = Color.web("#fff3cd");
    public static final Color ACTIVE_STROKE = Color.web("#b06f00");
    /** A subgraph of the requested size: an actual result. */
    public static final Color COMPLETE_FILL = Color.web("#d7f0dc");
    public static final Color COMPLETE_STROKE = Color.web("#2e7d4f");
    /** A branch that ran out of valid vertices before reaching size k. */
    public static final Color DEADEND_FILL = Color.web("#f1f3f5");
    public static final Color DEADEND_STROKE = Color.web("#adb5bd");

    private TreeRenderer() {
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
        Font font = new Font(TreeLayout.FONT_FAMILY, TreeLayout.FONT_HEIGHT);
        
        //if NOT root, get Text for the three lists
        if(node.getLevel() != 0){
            
            //Store 3 Text fields for each ESUNode
            out  = new Text[3];
            
            //create Text object for Subgraph
            String text = node.getSubgraphAsString();
            out[0] = new Text(rect.getX() + rect.getWidth()/2 - 
                        text.length() * TreeLayout.FONT_WIDTH / 2, 
                    rect.getY() + TreeLayout.innerPaddingY + TreeLayout.FONT_HEIGHT, 
                    text);
            //set font
            out[0].setFont(font);

            //create Text object for Possible Steps
            text = node.getPossibleStepsAsString();
            out[1] = new Text(rect.getX() + rect.getWidth()/2 - 
                        text.length() * TreeLayout.FONT_WIDTH / 2, 
                    rect.getY() + (TreeLayout.innerPaddingY + TreeLayout.FONT_HEIGHT) * 2, 
                    text);
            //set font
            out[1].setFont(font);

            //create Text object for SubgraphNeighbors
            text = node.getSubgraphNeighborsAsString();
            out[2] = new Text(rect.getX() + rect.getWidth()/2 - 
                        text.length() * TreeLayout.FONT_WIDTH / 2, 
                    rect.getY() + (TreeLayout.innerPaddingY + TreeLayout.FONT_HEIGHT) * 3, 
                    text);
            //set font
            out[2].setFont(font);
            
            //test for leaves, they have no subgraph neighbors or possible steps
            if(out[1].getText().length() == 2 && out[2].getText().length() == 2){
                Text temp = out[0];
                out = new Text[1];
                out[0] = temp;
                out[0].setY(rect.getY() + (TreeLayout.innerPaddingY + TreeLayout.FONT_HEIGHT) * 2);
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
                                text.length() * TreeLayout.FONT_WIDTH / 2, 
                    rect.getY() + (TreeLayout.innerPaddingY + TreeLayout.FONT_HEIGHT) * 2, 
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
                Line line = TreeLayout.getLineToParent(rects, finalNodes, 
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
     * Style Nodes:
     *
     * Paints every drawn Rectangle according to what the algorithm did with
     * that node: still working, finished as a result, or died without
     * reaching size k.
     *
     * Every rectangle is repainted on every call. The Rectangle objects are
     * shared across steps, so a node styled at one step would otherwise keep
     * that styling at the next one.
     *
     * @param rects         - Rectangles for every node, by level
     * @param currentNodes  - the tree as it stands at this step
     * @param finalNodes    - the finished tree, which is what says whether a
     *                        node ever gained children
     * @param activeSubgraph - subgraph string of the node this step is
     *                        working on, or null
     *********************************************************************** */
    public static void styleNodes(ArrayList<Rectangle>[] rects,
            ArrayList<ESUNode>[] currentNodes, ArrayList<ESUNode>[] finalNodes,
            String activeSubgraph){

        int lastLevel = finalNodes.length - 1;

        for(int level = 0; level < currentNodes.length; level++){
            for(int index = 0; index < currentNodes[level].size(); index++){

                Rectangle rect = rects[level].get(index);
                String subgraph = currentNodes[level].get(index)
                        .getSubgraphAsString();

                rect.setStrokeWidth(1.0);
                rect.getStrokeDashArray().clear();

                if(subgraph.equals(activeSubgraph)){
                    rect.setFill(ACTIVE_FILL);
                    rect.setStroke(ACTIVE_STROKE);
                    rect.setStrokeWidth(2.5);
                }
                else if(level == lastLevel){
                    rect.setFill(COMPLETE_FILL);
                    rect.setStroke(COMPLETE_STROKE);
                    rect.setStrokeWidth(1.8);
                }
                else if(isDeadEnd(finalNodes, subgraph, level, lastLevel)){
                    rect.setFill(DEADEND_FILL);
                    rect.setStroke(DEADEND_STROKE);
                    rect.getStrokeDashArray().addAll(4.0, 3.0);
                }
                else {
                    rect.setFill(PENDING_FILL);
                    rect.setStroke(PENDING_STROKE);
                }
            }
        }
    }

    /** **********************************************************************
     * Is Dead End:
     *
     * A node is a dead end when the finished tree shows it never gained a
     * child and it never reached the requested subgraph size. Asking the
     * current tree instead would be wrong: at an intermediate step a node
     * has no children simply because it has not expanded yet.
     *
     * Nodes are matched by subgraph string, the same way getLineToParent
     * does it, because the tree copy taken for each step creates new ESUNode
     * objects.
     *
     * @param finalNodes - the finished tree as ESUNode lists
     * @param subgraph   - subgraph string of the node in question
     * @param level      - the node's level
     * @param lastLevel  - the level holding complete subgraphs
     *
     * @return - true if this branch died before reaching size k
     *********************************************************************** */
    public static boolean isDeadEnd(ArrayList<ESUNode>[] finalNodes,
            String subgraph, int level, int lastLevel){

        if(level >= lastLevel){
            return false;
        }
        for(ESUNode candidate : finalNodes[level]){
            if(candidate.getSubgraphAsString().equals(subgraph)){
                return candidate.getChildren().isEmpty();
            }
        }
        return false;
    }

}
