/*
 * UndirectedGraph class for ESU algorithm. Assumes zero based nodes.
 */
package esu.algorithm;

import esu.algorithm.UI.Alerts;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Class UndirectedGraph
 * 
 * 
 * @author biohazard
 * @author polina eremenko
 */
public class UndirectedGraph {

    private int[][] graph;
    private int size;

    public UndirectedGraph(int size) {
        this.size = size;
        graph = new int[size][size];
    }

    /**
     * fillGraph
     *
     * the file should be formatted as such:
     *
     * each line should have 2 numbers representing the nodes separated by any
     * amount of whitespace. If it doesn't, the function simply stops reading
     * when it hits invalid data
     *
     * @param filename name of the file to read graph from
     */
    public void fillGraph(String fileName) {
        int from = 0;
        int to = 0;
        try {
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                if (scanner.hasNextInt()) {
                    from = scanner.nextInt();
                } else {
                    return;
                }
                if (scanner.hasNextInt()) {
                    to = scanner.nextInt();
                } else {
                    return;
                }
                insertNode(from, to);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
    }

    /**
     * insertNode
     *
     * inserts a node into the matrix
     *
     * @param mainNode node to be inserted
     * @param adjacentNode its adjacent node to be set
     */
    public void insertNode(int mainNode, int adjacentNode) {
        if (mainNode > size - 1 || mainNode < 0 || 
                adjacentNode > size - 1 || adjacentNode < 0) return;
        graph[mainNode][adjacentNode] = 1;
        graph[adjacentNode][mainNode] = 1;
    }

    /**
     * deleteNode
     *
     * removes a node from the matrix
     *
     * @param num node to be deleted
     */
    public void deleteNode(int num) {
        if (num > size - 1 || num < 0) return;
        for (int i = 0; i < size; i++) {
            graph[num][i] = 0;
            graph[i][num] = 0;
        }
    }
    
    /**
     * getSize
     * 
     * @return size of graph
     */
    public int getSize() {
        return size;
    }

    /**
     * getNeighbors
     * 
     * returns a list of neighbors of a node
     * @param x node to be looked t
     * @return list of adjacent nodes
     */
    public ArrayList getNeighbors(int x) {
        if (x > size - 1 || x < 0) return null;
        ArrayList<Integer> neighbors = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if(graph[x][i] == 1) {
                neighbors.add(i);
            }
        }
        return neighbors;
    }

    /**
     * Return a list of vertices.
     * 
     * @return - An array list of integers to represent all vertices of this graph.
     */
    public ArrayList<Integer> getVertices(){
        ArrayList<Integer> vertices = new ArrayList<>();
        for(int x = 0; x < size; x++){
            for(int y = 0; y < size; y++){
                if(graph[x][y] != 0){
                    vertices.add(x);
                    break;
                }
            }
        }
        return vertices;
    }
    /**
     * printGraph
     *
     * used for testing purposes
     */
    public void printGraph() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print(graph[i][j]);
            }
            System.out.println("");
        }
    }

}
