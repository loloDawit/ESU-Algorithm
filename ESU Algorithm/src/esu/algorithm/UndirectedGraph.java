/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esu.algorithm;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 *
 * @author dawitabera
 */
public class UndirectedGraph implements Graph{
    Scanner scanner;
    private int V;
    private int E;
    private Map<Integer,Integer> adjMap;
    
    // constructor
    public UndirectedGraph(int v){
        this.V = v;
        this.E =0;
        this.adjMap = new HashMap<>();
        for(int i=0; i<=v; i++){
            adjMap.add(i, new Vertex());
        }
    }
    // constructor 
    
    public UndirectedGraph(Scanner scanner){
        this.V = scanner.nextInt();
        this.adjMap = new HashMap<>();
        System.out.println(scanner.nextLine());
        
        while (scanner.hasNext()) {
            String infile = scanner.nextLine();
            // convert everything to a token
            StringTokenizer stringInput = new StringTokenizer(infile);
            
            
        }
    }

    @Override
    public int findEdges() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addEdges(int v, int w) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean containsEdges(int v, int w) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean containsVertices(int v) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeEdge(int v, int w) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void printGraph() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
