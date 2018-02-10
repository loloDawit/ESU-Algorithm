/* ****************************************************************************
 * File:    ESUNode.java
 * Purpose: To model an individual node in an ESU tree
 * Description:
 * Each node represnts a possible subgraph,  keeps track of a queue of possible 
 * next steps that represent a list of neighbor vertices in a graph, such that 
 * the next possible steps are connected to the currently linked "subgraph."
 * The nodes are designed in a way to exclude duplicate subgraphs.
 * The nodes also modify their tree, so that the tree itself can access its
 * leaf nodes directly.
 * These nodes also include a feature for taking individual steps into the tree
 * building process, so that the process can be paused and explored visually.
 *************************************************************************** */

//package
package esu.algorithm;

//imports
import java.util.LinkedList;
import java.util.PriorityQueue;

/** **************************************************************************
 * Class: ESUNode
 * 
 * @author BioHazard
 * @version 0.1
 * History - 
 *      2/9/18 - 
 *          File Created
 *          Main constructor written
 *          General logic outlined
 *      2/10/18 -
 *          Step() implemented
 *          getSubGrpah() implemented
 *          Main constructor tweeked
 *          Added support of "special root node" constructor
 *          Added dependency on ESUTree to update tree's leaf node list.
 ************************************************************************** */
public class ESUNode {

    //lists
    private PriorityQueue<Integer> possibleSteps; //next possible steps
    private LinkedList<ESUNode> children;//children nodes

    //Tree Reference
    private ESUTree tree;

    //parent "pointer"
    private ESUNode parent;

    //garph reference
    private UndirectedGraph graph;

    //current level in tree
    private int level;

    //My "step"
    private Integer myStep; //null if root
    
    //the first step in this tree's branch
    private Integer firstStep; //null if root
    
    
    
    /* ******************* Method Declarations ************************** */
    
    /** **********************************************************************
     * Root Constructor:
     * SHOULD ONLY BE USED TO CREATE THE ROOT OF A NEW ESU TREE.
     * For a general purpose/internal constructor:
     * @see ESUNode(ESUNode, Integer)
     * 
     * This constructor assigns a Graph to the Tree, and an ESU Tree to the
     * nodes.
     * 
     * This root node created will have minimal data, so DO NOT try to access
     * fields: myStep, or firstStep. Null values flag un-used
     * data for logical tests in recursive functions.
     * 
     * @param ug - An undirected graph to build this tree based on.
     * @param esu - The ESU Tree that owns this set of ESU Nodes.
     *********************************************************************** */
    public ESUNode(UndirectedGraph ug, ESUTree esu){
        myStep = null;
        firstStep = null;
        level = 0;
        possibleSteps = new PriorityQueue<>();
        for(Integer vertex : ug.getVertices()){
            possibleSteps.add(vertex);
        }
        children = new LinkedList<>();
        graph = ug;
        tree = esu;
    }
    
    /** ***********************************************************************
     * Special Copy Constructor:
     * Accepts the new Node's parent and a Node to perform a deep copy of.
     * 
     * This constructor SHOULD NOT BE USED TO CREATE THE ROOT OF A NEW ESU TREE.
     * This constructor assumes the parent Node is initialized and is valid
     * (not null).
     * 
     * @param parent    - This ESUNode's parent node
     * @param copy      - An ESUNode to deep copy
     *********************************************************************** */
    public ESUNode(ESUNode parent, ESUNode copy){
        level = copy.level;
        myStep = copy.myStep;
        firstStep = copy.firstStep;
        tree = parent.tree;
        possibleSteps = new PriorityQueue<>();
        children = new LinkedList<>();
        graph = copy.graph;
        this.parent = parent;
        for(Integer i : copy.possibleSteps){
            possibleSteps.add(i);
        }
        for(ESUNode child : copy.children){
            children.add(new ESUNode(this, child));
        }
    }
    
    /** ***********************************************************************
     * General Constructor:
     * This constructor is to be used inside this class only. 
     * For creating a new set of Nodes:
     * @see ESUNode(UndirectedGraph, ESUTree)
     * 
     * Creates a new non-root node for this ESU Tree. This constructor requires 
     * a reference to this new Node's parent, and the step in the Graph this
     * Node is to represent.
     * 
     * @param parent    - This Node's parent Node
     * @param nextStep  - This Node's "step" vertex from the Graph
     *********************************************************************** */
    private ESUNode(ESUNode parent, Integer nextStep) {
        //initialize lists
        possibleSteps = new PriorityQueue<>();
        children = new LinkedList<>();

        //set my step
        myStep = nextStep;

        //step tree
        tree = parent.tree;
        
        //set parent
        this.parent = parent;

        //set level
        level = parent.level + 1;
        if(level == 1){
            firstStep = myStep;
        }
        else{
            firstStep = parent.firstStep;
        }
        
        //If i'm not a leaf
        if(level < tree.maxHeight){
            
            //copy parent's possible steps if parent not root
            if(parent.myStep != null){
                for (Integer ps : parent.possibleSteps) {
                    possibleSteps.add(ps);
                }
            }
            //add my step's neighbors (if not included by parents)
            for (Integer neighbor : graph.getNeighbors(nextStep)) {
                /*
                if (!checkParents(neighbor)) {
                    possibleSteps.add(neighbor);
                }
                */
                if(neighbor > firstStep){
                    possibleSteps.add(neighbor);
                }
            }
        }else{
            //im a leaf
            //let my tree know.
            tree.leaves.add(this); //<--------------------------
            //leaking "this" in constructor is not considered safe,
            //but in this case can be responsible as the last call
            //in the constructor. "this" is a reference to an uninstantiated 
            //object, but the process of adding it to the tree's leaf list
            //will not modify or access the data inside this object, thus,
            //it is safe to place "this's" reference into its tree's leaf list.
        }
    }
    
    /** **********************************************************************
     * Check Parents:
     * A depricated function for checking all parents' possibleStep Queue's
     * for validating a next step to take. This function should not be used as
     * it's logic does not agree with the ESU Algorithm. Was implemented with a
     * misconception of how the algorithm worked.
     * 
     * @param i     - An integer to validate
     * @return      - True if the integer is invalid, false otherwise
     * @depricated
     ********************************************************************** */
    private boolean checkParents(Integer i) {
        if (possibleSteps.contains(i)) {
            return true;
        }
        if (parent == null) {
            return false;
        }
        return parent.checkParents(i);
    }
    
    /** **********************************************************************
     * Get Sub Graph:
     * Accepts an initialized LinkedList of Integers, and recursively adds
     * Integers into the list to represent a subgraph.
     * The parameter list is updated, and void is returned.
     * 
     * For accurate results:
     * THIS METHOD SHOULD ONLY BE CALLED ON LEAF NODES.
     * 
     * @param list  - A list of integers that will represent the vertices in a 
     *              subgraph for this ESU Tree's graph object.
     *********************************************************************** */
    public void getSubGraph(LinkedList<Integer> list) {
        if (myStep != null) {
            list.add(myStep);
            parent.getSubGraph(list);
        }
    }
    
    /** ***********************************************************************
     * Step: attempts to take a single step into the ESU Tree. If this Node
     * takes the step, true is returned up the recursive chain. If not, false is
     * returned to this Node's parent for further processing.
     * This step algorithm follows a depth-first traversal.
     * 
     * @return  True if this Node took the step or if one of this Nodes children
     *          took the step. False otherwise.
     ************************************************************************ */
    public boolean step() {
        //if I'm a leaf, return false;
        if (tree.maxHeight == level) {
            return false;
        }
        
        //step into children
        for (ESUNode child : children) {
            if (child.step()) {
                return true;
            }
        }

        Integer pop = possibleSteps.poll();
        if (pop == null) {
            return false; //<--nothing to expand
        }
        
        //make one child, return true
        ESUNode newChild = new ESUNode(this, pop);
        children.add(newChild);
        return true;
        
    }
    
    /** ***********************************************************************
     * Get Children:
     * A simple accessor for this ESUNode's children nodes.
     * This accessor is designed to be used by the ESUTree's deep copy
     * constructor to access the copyTree's root's children.
     * 
     * @return  - A LinkedList of this ESUNode's children Nodes.
     ************************************************************************ */
    LinkedList<ESUNode> getChildren(){
        return children;
    }
}
