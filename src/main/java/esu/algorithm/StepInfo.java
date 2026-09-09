/* ***************************************************************************
 * File: StepInformation.java
 * Purpose: To store the necessary information to describe each step the tree
 * takes while expanding.
 * Description: A simple micro class wrapper for some useful information for
 * printing each step of the algorithm.
 ************************************************************************** */

//package
package esu.algorithm;

//imports

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
 *          Added a copy of the EsuTree to the StepInfo data
 *      3/2/18 - 
 *          added a count public variable for leaf count.
 ************************************************************************** */
public class StepInfo {
    
    /** Stands in for a node that is absent. */
    private static final String EMPTY = "{}";

    private final String callerSubgraph; //subgraph of the node being built
    private final String targetSubgraph; //subgraph of an optional other node
    public final String description;     //text of this log entry
    public final Integer check;          //an optional Integer for this entry
    public final Code stepCode;          //Log Code: see enum at bottom of file
    
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
    public StepInfo(EsuNode caller, String desc,
            Code code, EsuNode target, Integer check){

        // Only the subgraph strings are ever read back, and a node's subgraph
        // is fixed once the node exists. Keeping them means this constructor
        // no longer has to snapshot the tree, which it used to do on every
        // single log entry.
        this.callerSubgraph = caller == null ? EMPTY : caller.getSubgraphAsString();
        this.targetSubgraph = target == null ? EMPTY : target.getSubgraphAsString();
        this.description = desc;
        this.stepCode = code;
        this.check = check;
    }

    /**
     * The subgraph this entry is about.
     *
     * @return the caller's subgraph, e.g. "{0, 2}"
     */
    public String getCallerSubgraph(){
        return callerSubgraph;
    }

    /**
     * The other subgraph this entry refers to, when it compares two nodes.
     *
     * @return the target's subgraph, or "{}" if this entry has no target
     */
    public String getTargetSubgraph(){
        return targetSubgraph;
    }

    /**
     * This entry as a sentence, with the node placeholders filled in.
     *
     * @return the description with %c and %t resolved
     */
    public String render(){
        return description.replace("%t", targetSubgraph)
                          .replace("%c", callerSubgraph);
    }

    // ********************  Step Code Enum  *************************** //
    /** **********************************************************************
     * Step Code:
     * An enumerated type that represents a "type" of StepInfo log entry. The 
     * Code type can be used to make assumptions about the data stored inside
     * a StepInfo object. See the description of each Code type for 
     * understanding of each Code.
     *********************************************************************** */
    public enum Code{
        CreateRoot,                 //Create the root of the tree
        Start,                      //start the creating a node
        InheritLists,               //inherit lists from parent
        GetNeighbors,               //get the neighbors of the current vertex
        RegisterCheck,              //validation decision (approve or deny)
        UpdateLists,                //update the current node's lists
        SubgraphCreation,           //created a unique subgraph
    }
}
