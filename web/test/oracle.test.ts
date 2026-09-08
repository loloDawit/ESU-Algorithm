import { describe, expect, it } from 'vitest';
import { UndirectedGraph } from '../src/graph.js';
import { ESUTree } from '../src/esu.js';
import { SAMPLES } from './samples.js';

/**
 * The same brute-force oracle the Java suite uses: enumerate every subset of
 * the right size, keep the connected ones, and require ESU to return exactly
 * that set, with no duplicates.
 */
function bruteForce(graph: UndirectedGraph, k: number): Set<string> {
  const found = new Set<string>();
  const n = graph.size;

  for (let mask = 0; mask < 1 << n; mask++) {
    const vertices: number[] = [];
    for (let v = 0; v < n; v++) if (mask & (1 << v)) vertices.push(v);
    if (vertices.length !== k) continue;
    if (isConnected(graph, vertices)) found.add(key(vertices));
  }
  return found;
}

function isConnected(graph: UndirectedGraph, vertices: number[]): boolean {
  const inside = new Set(vertices);
  const seen = new Set<number>([vertices[0]!]);
  const queue = [vertices[0]!];

  while (queue.length > 0) {
    for (const neighbour of graph.neighbours(queue.pop()!)) {
      if (inside.has(neighbour) && !seen.has(neighbour)) {
        seen.add(neighbour);
        queue.push(neighbour);
      }
    }
  }
  return seen.size === vertices.length;
}

function key(vertices: number[]): string {
  return [...vertices].sort((a, b) => a - b).join(',');
}

function runToCompletion(graph: UndirectedGraph, k: number): number[][] {
  const tree = new ESUTree(graph, k);
  while (tree.step()) tree.clearLog();
  return tree.subgraphs();
}

describe('ESU', () => {
  for (const [name, text] of Object.entries(SAMPLES)) {
    for (const k of [2, 3, 4, 5]) {
      it(`finds exactly the connected subgraphs of size ${k} in ${name}`, () => {
        const graph = UndirectedGraph.parse(text);

        const found = runToCompletion(graph, k);

        expect(new Set(found.map(key))).toEqual(bruteForce(graph, k));
      });

      it(`finds each subgraph of size ${k} in ${name} exactly once`, () => {
        const graph = UndirectedGraph.parse(text);

        const found = runToCompletion(graph, k).map(key);

        expect(found.length).toBe(new Set(found).size);
      });
    }
  }

  it('finds nothing when no subgraph is big enough', () => {
    const graph = UndirectedGraph.parse(SAMPLES['bowtie.txt']!);

    expect(runToCompletion(graph, 6)).toEqual([]);
  });
});

describe('parsing', () => {
  it('keeps the highest-numbered vertex', () => {
    // The 2018 reader sized the matrix to the largest id, putting that
    // vertex out of bounds and silently dropping every edge touching it.
    const graph = UndirectedGraph.parse(SAMPLES['bowtie.txt']!);

    expect(graph.size).toBe(5);
    expect(graph.neighbours(4)).toEqual([2, 3]);
  });

  it('ignores lines that are not a pair of numbers', () => {
    const graph = UndirectedGraph.parse('0 1\n\n# a comment\n1 2\nnonsense\n');

    expect(graph.size).toBe(3);
    expect(graph.neighbours(1)).toEqual([0, 2]);
  });
});
