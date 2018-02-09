/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esu.algorithm;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author dawitabera
 */
public class Vertex implements Iterable<Integer>{
    private boolean visited;
    private ArrayList<Integer> w;
    
    //constructor
    public Vertex(){
        this.visited  = false;
        this.w = new ArrayList<>();
    }
    
    // deep copy
    public Vertex(Vertex rightObj){
        this.visited = rightObj.visited;
        this.w = new ArrayList<>();
    }
    public boolean isVisited(){
        return visited;
    }
    
    public void setVisited(boolean v){
        this.visited = v;
    }

    @Override
    public Iterator<Integer> iterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
