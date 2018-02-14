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
import java.util.LinkedList;

/** **************************************************************************
 * Class: ESUTree
 * 
 * @author BioHazard
 * @version 0.1
 * 
 * History - 
 *          2/9/18
 *          File Created.
 *          2/10/18
 *          Started constructor implementation.
 *          getSubGraphs() implemented.
 */
public class ESUTree {
    
    //leaf list
    LinkedList<ESUNode> leaves;
    
    //root
    ESUNode root;
    
    //my graph to search for subgraphs
    TestUndirectedGraph graph;
    
    //max height (sub graph size)
    protected int maxHeight;
    
    /** ***********************************************************************
     * Constructor:
     * accepts a reference to an UndirectedGraph and a size for subgraphs to
     * find. 
     * 
     * Initializes the root ESNode, and prepares this ESU Tree to start stepping
     * through the algorithm.
     * 
     * @param ug            - a base undirected graph to search for subgraphs
     * @param subGraphSize  - The size of the subgraphs to search for.
     *********************************************************************** */
    public ESUTree(TestUndirectedGraph ug, int subGraphSize){
        leaves = new LinkedList<>();
        maxHeight = subGraphSize;
        graph = ug;
        root = new ESUNode(graph, this);
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
        root = new ESUNode(graph, this);
        for(ESUNode child : copy.root.getChildren()){
            root.getChildren().add(new ESUNode(root, child)); //copy ESUNodes
        }
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
    LinkedList<ESUNode>[] getNodesByLevel(){
        LinkedList<ESUNode> lists[];
        lists = new LinkedList[maxHeight];
        for(int i = 0; i < lists.length; i++){
            lists[i] = new LinkedList<>();
        }
        root.getLevels(lists);
        return lists;
    }
    
    /**
     * TESTING ENVIRONMENT
     * 
     * @param args 
     */
    public static void main(String args[]){
        
        //set-up
        TestUndirectedGraph graph = new TestUndirectedGraph();
        ESUTree tree = new ESUTree(graph, 3);
        
        //step until done
        while(tree.step()){/* Do Nothing */};
        
        //print each detected subgraph
        for(ESUNode subgraph : tree.leaves){
            LinkedList<Integer> vertices = new LinkedList<>();
            subgraph.getSubGraph(vertices);
            String output = "{";
            for(Integer vertex : vertices){
                output += " " + vertex + ",";
            }
            System.out.println(output.substring(0, output.length()-1) + " }");
        }
        
    }
}
