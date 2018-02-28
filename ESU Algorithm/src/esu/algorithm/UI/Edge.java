/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esu.algorithm.UI;

/**
 * This class is used to create a EdgeObject for the Graph  
 * 
 * @author BioHazard
 */
public class Edge {
    
    private final int   weight;
    private final DrawNode  nodeFrom;
    private final DrawNode  nodeTo;
    /**
     * Default constructor
     * 
     * @param nodeFrom    startNode of the edge (or node one as edges are undirected)
     * @param nodeTo      endNode of the edge (or node two as edges are undirected)
     * @param newWeight     random integer weight of the edge
     */
    public Edge(DrawNode nodeFrom, DrawNode nodeTo, int newWeight) {    
        this.nodeFrom = nodeFrom;
        this.nodeTo = nodeTo;
        weight  = newWeight;
    }
    /**
     * Getter for the weight (distance) of the edge
     * @return integer weight
     */              
    public int getWeight() {
        return  weight;
    }
    /**
     * Getter for the startNode of the edge (or node one as edges are undirected)
     * @return Node startNode of the edge
     */              
    public DrawNode getNodeFrom() {
        return  nodeFrom;
    }
    /**
     * Getter for the endNode of the edge
     * @return Node endNode of the edge (or node two as edges are undirected)
     */                  
    public DrawNode getNodeTo() {
        return  nodeTo;
    }
    
}
