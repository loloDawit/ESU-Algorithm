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
 ************************************************************************** */
public class StepInfo {
    
    // ***** public because i'm lazy. dont change from outside!!! *****
    public ESUNode caller;
    public ESUNode target;
    public String description;
    public Integer check;
    public Code stepCode;//...?
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
        this.caller = caller;
        description = desc;
        stepCode = code;
        this.target = target;
        this.check = check;
    }
    
    
    // ********************  Step Code Enum  *************************** //
    public enum Code{
        CreateRoot,                 //Create the root of the tree
        Start,                      //start the creating a node
        InheritLists,
        GetNeighbors,               //get the neighbors of the current vertex
        SelectNeighbor,             //select a neighbor 
        CheckSelectedNeighbor,      //check potential neighbor
        RegisterCheck,              //validation decision (approve or deny)
        UpdateLists,                //update the current node's lists
        SubgraphCreation,           //created a unique subgraph
        Finish                      //finish creation of new node
    }
}
