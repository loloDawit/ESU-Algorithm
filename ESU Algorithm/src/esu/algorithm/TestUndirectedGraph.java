/*
 * THIS IS JUST A PLACEHOLDER TEST CLASS, MADE TO TEST THE INTEGRITY OF THE 
 * ESU TREE AND ESU NODE LOGIC
 */
package esu.algorithm;

import java.util.LinkedList;

public class TestUndirectedGraph {

    public TestUndirectedGraph(){ /* Do Nothing */}
    
    //get vertices of this graph
    public LinkedList<Integer> getVertices(){
        LinkedList<Integer> out = new LinkedList<>();
        out.add(1);
        out.add(2);
        out.add(3);
        out.add(4);
        out.add(5);
        out.add(6);
        out.add(7);
        out.add(8);
        out.add(9);
        return out;
    }
    
    //get neighbors of the parameter vertex
    public LinkedList<Integer> getNeighbors(int v){
        LinkedList<Integer> out = new LinkedList<>();
        switch (v){
            case 1:
                out.add(2);
                out.add(3);
                out.add(4);
                out.add(5);
                break;
            case 2:
                out.add(1);
                out.add(3);
                out.add(6);
                out.add(7);
                break;
            case 3:
                out.add(1);
                out.add(2);
                out.add(8);
                out.add(9);
                break;
            case 4:
                out.add(1);
                break;
            case 5:
                out.add(1);
                break;
            case 6:
                out.add(2);
                break;
            case 7:
                out.add(2);
                break;
            case 8:
                out.add(3);
                break;
            case 9:
                out.add(3);
                break;
            
        }
        return out;
    }
    
}
