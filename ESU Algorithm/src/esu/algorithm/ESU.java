/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package esu;

import java.util.ArrayList;

/**
 *
 * @author polina
 */
public class ESU {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        UndirectedGraph g = new UndirectedGraph(5);
        g.fillGraph("/Users/polina/Desktop/ESU/src/esu/myGraph.txt");
        g.printGraph();
        System.out.println(g.getSize());
        g.deleteNode(8);
        g.printGraph();
        ArrayList<Integer> n = g.getNeighbors(1);
        for (int i = 0; i < n.size(); i++) {
            System.out.println(n.get(i));
        }
    }

}
