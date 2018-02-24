/* ***************************************************************************
 * File: StepInformation.java
 * Purpose: To store the necessary information to describe each step the tree
 * takes while expanding.
 * Description: A simple micro class wrapper for some useful information for
 * printing each step of the algorithm.
 ************************************************************************** */

//package
package esu.algorithm;

//no imports

import java.util.ArrayList;


/** **************************************************************************
 * Class: Step Information
 * 
 * @author Biohazard
 * @version 0.1
 * History - 
 *      2/14/18 - Happy Valentine's Day
 *          File created.
 *          Debating whether a stepCode will be useful. Included in case
 *              a use arises.
 *      2/17/18 - 
 *          Revised Code enum.
 *          Filled in documentation
 *      2/20/18 - 
 *          Added a copy of the ESUTree to the StepInfo data
 ************************************************************************** */
public class StepInfo {
    
    // ***** public because i'm lazy. dont change from outside!!! *****
    public ESUNode caller;      //the ESUNode calling the log entry
    public ESUNode target;      //an optional target ESUNode for this log entry
    public String description;  //a text description of this log entry
    public Integer check;       //An optional Integer for this log entry
    public Code stepCode;       //Log Code: see enum at bottom of file
    public ESUTree tree;        //A copy of the tree's state for the log
    //*****************************************************************
    
    /** ***********************************************************************
     * Constructor:
     * Simply initializes this StepInformation's internal variables to all those
     * passed in as parameters.
     * 
     * target parameter can be set to null if this step does not involve a 
     * target node.
     * 
     * @param caller    - The ESU Node being built during this step.
     * @param desc      - A text description of this step. Possible formatting
     *                      substrings maybe implemented.
     * @param code      - an enum representing what type of step this 
     *                      information is attempting to describe.
     * @param target    - Either a target ESU Node for comparisons OR null
     *                      if no target is required by this step.
     * @param check     - An integer storing an Integer being checked in this
     *                      step. Can be null if no Integer is being checked
     *                      during this step.
     *********************************************************************** */
    public StepInfo(ESUNode caller, String desc, Code code, ESUNode target, Integer check){
        
        this.caller = null;//default to null
        description = desc;
        stepCode = code;
        this.target = target;
        this.check = check;
        
        tree = caller.getTree();
        ArrayList<ESUNode> levels[] = null;
        try{
            levels = tree.getNodesByLevel();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        //find relavent caller node in tree copy
        for(int node = 0; node < levels[caller.getLevel()].size(); node++){
            if (caller.getSubgraphAsString().equals(levels[caller.getLevel()].get(node).getSubgraphAsString())){
                this.caller = levels[caller.getLevel()].get(node);
                break;
            }
        }
        
        //find relavent target node in tree copy if not null
        if(target != null){
            for(int node = 0; node < levels[target.getLevel()].size(); node++){
                if (target.getSubgraphAsString().equals(levels[target.getLevel()].get(node).getSubgraphAsString())){
                    this.target = levels[target.getLevel()].get(node);
                    break;
                }
            }
        }
        
        
        
    }
    
    
    // ********************  Step Code Enum  *************************** //
    /** **********************************************************************
     * Step Code:
     * An enumerated type that represents a "type" of StepInfo log. The 
     * Code type can be used to make assumptions about the data stored inside
     * a StepInfo object. See the description of each Code type for 
     * understanding of each Code.
     *********************************************************************** */
    public enum Code{
        CreateRoot,                 //Create the root of the tree
        Start,                      //start the creating a node
        InheritLists,               //inherit lists from parent
        GetNeighbors,               //get the neighbors of the current vertex
        SelectNeighbor,             //select a neighbor 
        CheckSelectedNeighbor,      //check potential neighbor
        RegisterCheck,              //validation decision (approve or deny)
        UpdateLists,                //update the current node's lists
        SubgraphCreation,           //created a unique subgraph
        Finish                      //finish creation of new node
    }
}
