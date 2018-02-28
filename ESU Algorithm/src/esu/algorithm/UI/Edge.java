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
    private final DrawNode  nodeOne;
    private final DrawNode  nodeTwo;
    /**
     * Default constructor
     * 
     * @param newNodeOne    startNode of the edge (or node one as edges are undirected)
     * @param newNodeTwo    endNode of the edge (or node two as edges are undirected)
     * @param newWeight     random integer weight of the edge
     */
    public Edge(DrawNode newNodeOne, DrawNode newNodeTwo, int newWeight) {    
        nodeOne = newNodeOne;
        nodeTwo = newNodeTwo;
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
    public DrawNode getNodeOne() {
        return  nodeOne;
    }
    /**
     * Getter for the endNode of the edge
     * @return Node endNode of the edge (or node two as edges are undirected)
     */                  
    public DrawNode getNodeTwo() {
        return  nodeTwo;
    }
    
}
