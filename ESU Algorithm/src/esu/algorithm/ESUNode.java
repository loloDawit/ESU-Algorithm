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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

/** **************************************************************************
 * Class: ESUNode
 * 
 * @author BioHazard
 * @version 0.2
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
 *      2/13/18 - 
 *          Changed the checkParents() method to support the
 *              correct "Node filtering" logic.
 *          Implemented getLevels()
 *          Updated documentation/comments
 *      2/14/18 - 
 *          Started implementing step log support.
 *          Fixed a bug where copied leaf Nodes were not updating their
 *              parent tree's leaf list.
 *              - Tested: Passed.
 *      2/17/18 - 
 *          Finished implementing step log support.
 *          Changed printing sequence for subgraphs (prints in order now).
 *              - Tested: Failed - minor fixes.
 *              - Tested: Passed
 *          updated to version 0.2
 ************************************************************************** */
public class ESUNode {

    //lists
    private final PriorityQueue<Integer> possibleSteps; //next possible steps
    private final LinkedList<ESUNode> children;         //children nodes
    private final LinkedList<Integer> subgraphNeighbors;//Vald subgraph neighbors

    //Tree Reference
    private final ESUTree tree;

    //parent "pointer"
    private final ESUNode parent;

    //garph reference
    private final UndirectedGraph graph;

    //current level in tree
    private final int level;

    //My "step" into a possible subgraph
    private final Integer myStep; //<--- null if root
    
    //the first step in this tree's branch
    private final Integer firstStep; //<--- null if root
    
    
    
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
        subgraphNeighbors = null;
        for(Integer vertex : ug.getVertices()){
            possibleSteps.add(vertex);
        }
        children = new LinkedList<>();
        graph = ug;
        tree = esu;
        parent = null;
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
        
        //shallow copy integers
        level = copy.level;
        myStep = copy.myStep;
        firstStep = copy.firstStep;
        
        //inherit parent's traits
        tree = parent.tree;
        graph = parent.graph;
        
        this.parent = parent;
        
        //copy lists
        subgraphNeighbors = (LinkedList<Integer>)copy.subgraphNeighbors.clone();
        possibleSteps = new PriorityQueue<>();
        for(Integer i : copy.possibleSteps){
            possibleSteps.add(i);
        }
        
        //copy over copy's children
        children = new LinkedList<>();
        for(ESUNode child : copy.children){
            children.add(new ESUNode(this, child));
        }
        
        if(level == tree.maxHeight){
            tree.leaves.add(this);
        }
    }
    
    /** ***********************************************************************
     * General Purpose Private Constructor:
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
        subgraphNeighbors = new LinkedList<>();

        //set my step
        myStep = nextStep;

        //step tree
        tree = parent.tree;
        
        //set parent
        this.parent = parent;
        
        this.graph = parent.graph;

        //set level
        level = parent.level + 1;
        
        //********** set first step... @depricated ? ***************
        if(level == 1){
            firstStep = myStep;
        }
        else{
            firstStep = parent.firstStep;
        }
        //**********************************************************
        
        
    }
    
    public void setLists(){
        String desc;
        //If i'm not a leaf
        if(level < tree.maxHeight){
            
            //update log with node creation
            String parentPS = "(";
            for(Integer i : parent.possibleSteps){
                parentPS += " " + i + ",";
            }
            parentPS = parentPS.substring(0, parentPS.length()-1);
            parentPS += " )";
            
            
            //copy parent's possible steps if parent not root
            if(parent.myStep != null){
                desc = "Create node %c, copy %t's possible steps " + parentPS + " into %c's possible steps () and subgraph neighbors [].";
                tree.log.add(new StepInfo(this, desc, StepInfo.Code.InheritLists, parent, null));
                for (Integer ps : parent.possibleSteps) {
                    possibleSteps.add(ps);
                    subgraphNeighbors.add(ps);
                }
            }
            //add my step's neighbors (if not included by parents)
            desc = "Getting " + myStep + "'s neighbors: {";
            ArrayList<Integer> neighbors = graph.getNeighbors(myStep);
            for(Integer vertex : neighbors){
                desc += " " + vertex + ",";
            }
            desc = desc.substring(0, desc.length() -1) + " }.";
            tree.log.add(new StepInfo(this, desc, StepInfo.Code.GetNeighbors, null, null));
            for (Integer neighbor : neighbors) {
                
                if(neighbor <= firstStep){
                    desc = "" + neighbor + " is less than or equal to this branch's first step (" + firstStep + "). Validation denied.";
                    tree.log.add(new StepInfo(this, desc, StepInfo.Code.RegisterCheck, null, neighbor));
                }
                else if (checkParents(neighbor, this)) {
                    desc = "" + neighbor + " added to %c's lists.";
                    tree.log.add(new StepInfo(this, desc, StepInfo.Code.UpdateLists, null, neighbor));
                    possibleSteps.add(neighbor);
                    subgraphNeighbors.add(neighbor);
                }
                /*
                if(neighbor > firstStep){
                    possibleSteps.add(neighbor);
                }
                */
            }
        }else{
            //im a leaf
            //let my tree know.
            desc = "Created a uniquie subgraph %c.";
            tree.log.add(new StepInfo(this, desc, StepInfo.Code.SubgraphCreation, null, null));
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
     * A function for checking all parents' subgraph neighbors
     * for validating a next step to take. This function is responsible for
     * validating the Integer i as a valid insertion into the calling ESUNode's
     * subgraphNeighbors list and possibleSteps Queue.
     * 
     * @param i     - An integer to validate
     * @return      - True if the integer is invalid, false otherwise
     ********************************************************************** */
    private boolean checkParents(Integer i, ESUNode caller) {
        
        //base case, tree root
        if (myStep == null) {
            String desc = "Validation approved for " + i + ".";
            tree.log.add(new StepInfo(caller, desc, StepInfo.Code.RegisterCheck, this, i));
            return true;
        }
        
        //check my subgraphNeighbors and myStep
        if ( i.equals(myStep) || subgraphNeighbors.contains(i) ) {
            String desc = "" + i + " was found in %t's data. Validation denied.";
            tree.log.add(new StepInfo(caller, desc, StepInfo.Code.RegisterCheck, this, i));
            return false;
        }
        
        //check next node up the tree
        String desc = "" + i + " was not found in %t's data. Validation continues.";
        tree.log.add(new StepInfo(caller, desc, StepInfo.Code.RegisterCheck, this, i));
        return parent.checkParents(i, caller);
    }
    
    /** **********************************************************************
     * Get Sub Graph:
     * Accepts an initialized LinkedList of Integers, and recursively adds
     * Integers into the list to represent a subgraph.
     * The parameter list is updated, and void is returned.
     * 
     * @param list  - A list of integers that will represent the vertices in a 
     *              subgraph for this ESU Tree's graph object.
     *********************************************************************** */
    public final void getSubGraph(LinkedList<Integer> list) {
        if (myStep != null && list != null) {
            list.addFirst(myStep);
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
        String desc = "Creating new node.";
        tree.log.add(new StepInfo(this, desc, StepInfo.Code.Start, null, null));
        children.add(newChild);
        newChild.setLists();
        desc = "Finished making node %c.";
        tree.log.add(new StepInfo(newChild, desc, StepInfo.Code.Start, null, null));
        return true;
        
    }
    
    /** **********************************************************************
     * Get Level:
     * Accessor for this ESUNode's height (or level) in the ESUTree.
     * 
     * @return - An int representing this ESUNode's height (or level) in the 
     *              owning ESUTree.
     *********************************************************************** */
    public int getLevel(){
        return level;
    }
    
    /** ***********************************************************************
     * Get Tree:
     * Returns a deep copy of this ESUNode's tree reference.
     * 
     * @return - A deep copy of this ESUNode's ESUTree.
     *********************************************************************** */
    public ESUTree getTree(){
        return new ESUTree(tree);
    }
    /** ***********************************************************************
     * Get Children:
     * A simple accessor for this ESUNode's children nodes.
     * This accessor is designed to be used by the ESUTree's deep copy
     * constructor to access the copyTree's root's children.
     * 
     * @return  - A LinkedList of this ESUNode's children Nodes.
     ************************************************************************ */
    public LinkedList<ESUNode> getChildren(){
        return children;
    }
    
    /** ***********************************************************************
     * Get Possible Steps:
     * Accessor for this ESU Node's possible steps. 
     * This returns a shallow copy, proceed with caution! Do not mutate the
     * returned object at all as it's changes will be reflected in the building
     * of the ESU Tree.
     * 
     * @return - This ESUNode's possible steps Priority Queue. [shallow copy]
     *********************************************************************** */
    public PriorityQueue getPossibleSteps(){
        return possibleSteps;
    }
    
    /** **********************************************************************
     * Get Subgraph Neighbors:
     * Accessor for this ESUNode's subgraph neighbors. The list returned stores
     * the vertices of all possible neighbors of the subgraph that this node
     * is to represent.
     * 
     * @return - a deep copy of this ESUNode's subgraphNeighbors as a 
     *              linked list.
     ********************************************************************** */
    public LinkedList<Integer> getSubgraphNeighbors(){
        return (LinkedList<Integer>)subgraphNeighbors.clone();
    }
    
    /** **********************************************************************
     * Get Subgraph As Sting:
     * Returns a string representation of this ESUNode's subgraph.
     * 
     * @return - A string representation of this ESUNode's subgraph.
     *********************************************************************** */
    public String getSubgraphAsString(){
        String out = "{";
        LinkedList<Integer> list = new LinkedList<>();
        getSubGraph(list);
        for(Integer i : list){
            out += "" + i + ", ";
        }
        
        if(out.length() > 1){
            out = out.substring(0, out.length()-2);
        }
        out += "}";
        return out;
    }
    
    /** **********************************************************************
     * Get Subgraph Neighbors As Sting:
     * Returns a string representation of this ESUNode's subGraph neighbors.
     * 
     * @return - A string representation of this ESUNode's subgraph neighbors.
     *********************************************************************** */
    public String getSubgraphNeighborsAsString(){
        String out = "[";
        
        for(Integer i : subgraphNeighbors){
            out += "" + i + ", ";
        }
        
        out = out.substring(0, out.length()-2) + "]";
        return out;
    }
    
    /** **********************************************************************
     * Get Possible Steps As Sting:
     * Returns a string representation of this ESUNode's possibleSteps Queue.
     * 
     * @return - A string representation of this ESUNode's possibleSteps Queue.
     *********************************************************************** */
    public String getPossibleStepsAsString(){
        String out = "(";
        
        //populate temporary queue
        PriorityQueue<Integer> temp = new PriorityQueue<>();
        for(Integer i : possibleSteps){
            temp.add(i);
        }
        
        //pop in order
        while(!temp.isEmpty()){
            out += "" + temp.poll() + ", ";
        }
        
        //delete trailing ", " from string and add closing parenthesis
        out = out.substring(0, out.length()-2) + ")";
        return out;//return out
    }
    
    /** ***********************************************************************
     * Get Levels:
     * This function populates an array of lists where the index of the array
     * represents a level in the tree.
     * 
     * given an ESU Tree:
     *                      [root]
     *         
     *          1(2,3,4)    2(3,5)              3(4)    4(5)    5(0)
     *      ________|__       _|__________        |____   |____
     *  1,2(3,4,5)  1,3(4)  2,3(4,5)    2,3,5(0)    3,4(5)  4,5(0)
     * 
     *  (...)
     * 
     * the array will be populated as:
     * 
     * [0]: [root]
     * [1]: [1(2,3,4)], [2(3,4), [3(4)], [4(5)], [5(0)]
     * [2]: [1,2(3,4,5)], [1,3(4)], [2,3(4,5)], [2,3,5(0)], [3,4(5)], [4,5(0)]
     * [3]: (...)
     * 
     * @param lists - An array of linked lists where each index of the array 
     *                  has a list of nodes for that level.
     */
    public void getLevels(LinkedList<ESUNode> lists[]) {
        if (lists != null && lists[level] != null) {
            lists[level].add(this);
            for (ESUNode child : children) {
                child.getLevels(lists);
            }
        }
    }
    
}
