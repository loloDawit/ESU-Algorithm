/* ***************************************************************************
 * Class:   ESUTree.java
 * Purpose: The ESU Tree is a model for the structure of the Fanmod ESU 
 * Algorithm. For more information on the ESU Algorithm, please refer to their
 * research paper.
 * Description:
 * This ESU Tree class is a "step-wise" implementation of the ESU algorithm.
 * The main feature includes the ability to take individual steps into the 
 * algorithm for understanding and visualization purposes.
 * This class utilizes the ESUNode class, and relies on the Nodes to do most 
 * of the work, but offers a clean interface for user interaction.
 *************************************************************************** */

//package
package esu.algorithm;

//imports
import java.util.ArrayList;
import java.util.LinkedList;

/** **************************************************************************
 * Class: ESUTree
 * 
 * @author BioHazard
 * @version 0.2
 * 
 * History - 
 *          2/9/18 -
 *              File Created.
 *          2/10/18 - 
 *              Started constructor implementation.
 *              getSubGraphs() implemented.
 *          2/13/18 - 
 *              Implemented getNodesByLevel()
 *              Tested class on a local TestUndirectedGraph
 *                  - Test passed (Hooray!)
 *          2/14/18 - Happy Valentine's Day!
 *              Started implementing a step log.
 *          2/17/18 - 
 *              Step log support added in full.
 *              Driver updated to reflect new log functionality an tests.
 *                  Tested: Failed - minor bug fixes in ESUNode.
 *                  Tested: Passed!
 *              Updated to version 0.2
 *************************************************************************** */
public class ESUTree {
    
    //leaf list
    LinkedList<ESUNode> leaves;
    
    //root
    ESUNode root;
    
    //my graph to search for subgraphs
    UndirectedGraph graph;
    
    //max height (sub graph size)
    protected int maxHeight;
    
    //step descriptions
    ArrayList<StepInfo> log;
    
    /** ***********************************************************************
     * Constructor:
     * accepts a reference to an UndirectedGraph and a size for subgraphs to
     * find. 
     * 
     * Initializes the root ESU Node, and prepares this ESU Tree to start 
     * stepping through the algorithm.
     * 
     * @param ug            - a base undirected graph to search for subgraphs
     * @param subGraphSize  - The size of the subgraphs to search for.
     *********************************************************************** */
    public ESUTree(UndirectedGraph ug, int subGraphSize){
        leaves = new LinkedList<>();
        log = new ArrayList<>();
        maxHeight = subGraphSize;
        graph = ug;
        root = new ESUNode(graph, this);
        //root.setLists();
        //add Root creation to step log
        String desc = "Create root Node";
        log.add(new StepInfo(root, desc, 
                StepInfo.Code.CreateRoot, null, null));
    }
    
    /** *********************************************************************
     * Copy Constructor:
     * Performs a deep copy of the ESU Tree. The Graph referenced by the ESUTree
     * is not deep copied, but all ESUNodes and data are.
     * This function assumes that the ESUTree being copied is initialized and 
     * has a valid root Node.
     * 
     * @param copy      - An ESUTree to perform a deep copy on.
     ************************************************************************ */
    public ESUTree(ESUTree copy){
        graph = copy.graph;
        maxHeight = copy.maxHeight;
        leaves = new LinkedList<>();
        log = new ArrayList<>();
        root = new ESUNode(graph, this);
        for(ESUNode child : copy.root.getChildren()){
            root.getChildren().add(new ESUNode(root, child)); //copy ESUNodes
        }
        
        //copy step log
        for(StepInfo info : copy.log){
            log.add(info);
        }
        
        //clear copy's step log
        //copy.log.clear();
        //gets "copy"'s step log ready to record all events from this point on.
    }
    
    public void clearStepLog(){
        log.clear();
    }
    /** **********************************************************************
     * Step:
     * Attempts to take the next step of the algorithm. If a step was taken,
     * true is returned. If there are no more valid steps for the algorithm,
     * false is returned.
     * 
     * @return  - True if a step was taken, false otherwise.
     ********************************************************************** */
    public boolean step(){
        return root.step();
    }
    
    /** **********************************************************************
     * Get Subgraphs:
     * Simply retrieves any discovered subgraphs as a list of lists of vertices.
     * Each element in the returned list is a list that contains the vertices for
     * a discovered subgraph.
     * 
     * @return  - A LinkedList of LinkedLists of integers that represent vertices
     *          of this ESU Tree's undirected Graph.
     ************************************************************************ */
    public LinkedList<LinkedList<Integer>> getSubGraphs(){
        
        //output list
        LinkedList<LinkedList<Integer>> out = new LinkedList<>();
        
        //get a list of vertices for each leaf node discovered
        for(ESUNode leaf : leaves){
            LinkedList<Integer> subGraph = new LinkedList<>();
            leaf.getSubGraph(subGraph);
            out.add(subGraph);
        }
        
        return out;
    }
    
    /** ***********************************************************************
     * Get Nodes By Level:
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
     * @return An array of linked lists where each index of the array 
     *             has a list of nodes for that level.
     *********************************************************************** */
    public LinkedList<ESUNode>[] getNodesByLevel(){
        LinkedList<ESUNode> lists[];
        lists = new LinkedList[maxHeight + 1];
        for(int i = 0; i < lists.length; i++){
            lists[i] = new LinkedList<>();
        }
        root.getLevels(lists);
        return lists;
    }
    
    /** ***********************************************************************
     * TESTING ENVIRONMENT.
     * Do not include in final release.
     * 
     * A simple test framework for running the ESUTree logic in a controlled
     * Alpha-testing environment.
     * 
     * @param args - [Not Used]
     *********************************************************************** */
    public static void main(String args[]){
        
        //set-up
        //TestUndirectedGraph graph = new TestUndirectedGraph();
        UndirectedGraph graph = new UndirectedGraph(101);
        graph.fillGraph("/home/nate/gits/ESU-Algorithm/ESU Algorithm/src/esu/algorithm/myGraph.txt");
        ESUTree tree = new ESUTree(graph, 4);
        ArrayList<ESUTree> treeList= new ArrayList<>();
        //step until done
        while(tree.step()){
            ESUTree tempTree = new ESUTree(tree);
            tree.clearStepLog();
            treeList.add(tempTree);
        }
        
        for(int i = 0; i < treeList.size(); i++){
            System.out.println("Tree[" + i + "] step log:");
            ArrayList<StepInfo> log = treeList.get(i).log;
            for(int entry = 0; entry < log.size(); entry++){
                String caller = nodeToString(log.get(entry).caller);
                String target = nodeToString(log.get(entry).target);
                String description = log.get(entry).description;
                description = description.replace("%t", target);
                description = description.replace("%c", caller);
                System.out.println(description);
            }
            /*
            //display leaves for current tree
            System.out.println("Leaves for tree[" + i + "]:");
            //print each detected subgraph
            for(ESUNode subgraph : treeList.get(i).leaves){
                LinkedList<Integer> vertices = new LinkedList<>();
                subgraph.getSubGraph(vertices);
                String output = "{";
                for(Integer vertex : vertices){
                    output += " " + vertex + ",";
                }
                System.out.print(output.substring(0, output.length()-1) + " } ");
            }
            */
            
            System.out.println();
        }
        System.out.println("" + treeList.get(treeList.size()-1).leaves.size() + " subgraphs detected: ");
        for(ESUNode leaf : treeList.get(treeList.size()-1).leaves){
            System.out.print(nodeToString(leaf) + " ");
        }
    }
    
    /** **********************************************************************
     * Node To String:
     * A simple function for converting a node into a String as its current
     * subgraph vertices.
     * 
     * @param node - an ESUNode to convert into a String.
     * @return - A String representation of the parameter ESUNode
     ********************************************************************** */
    public static String nodeToString(ESUNode node){
        if (node == null)
            return "";
        String out = "{";
        LinkedList<Integer> list = new LinkedList<>();
        node.getSubGraph(list);
        if(list.isEmpty()){
            //root
            return "{root}";
        }
        for(Integer i : list){
            out += " " + i + ",";
        }
        out = out.substring(0, out.length() - 1);
        out += " }";
        return out;
    }
}
