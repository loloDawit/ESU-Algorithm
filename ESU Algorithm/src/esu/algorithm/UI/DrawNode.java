/*
 * DrawNode
 */
package esu.algorithm.UI;

/**
 * This class is used to create a NodeObject for the Graph  
 * 
 * @author BioHazard
 */
public class DrawNode {
    
    private final char    nodeName;
    private final int     locationX;
    private final int     locationY;
    /**
     * Default constructor
     * 
     * @param newLocationX  location on the x axis
     * @param newLocationY  location on the y axis
     * @param gname          character identifier of the node
     */
    public DrawNode(int newLocationX, int newLocationY, char gname){
        this.nodeName    = gname;
        this.locationX   = newLocationX;
        this.locationY   = newLocationY;
    }
    
    /**
     * Getter for location on the x axis
     * @return integer locationX
     */
    public int getLocationX() {
        return  locationX;
    }
    /**
     * Getter for location on the y axis
     * @return integer locationY
     */    
    public int getLocationY() {
        return  locationY;
    }
    /**
     * Getter for the character name of the node
     * @return character nodeName
     */    
    public char getNodeName() {
        return  nodeName;
    }
    
}