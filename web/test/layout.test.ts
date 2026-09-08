import { describe, expect, it } from 'vitest';
import { UndirectedGraph } from '../src/graph.js';
import { ESUTree } from '../src/esu.js';
import { TreeLayout, centreX } from '../src/layout.js';
import { SAMPLES } from './samples.js';

function finished(graph: UndirectedGraph, k: number): ESUTree {
  const tree = new ESUTree(graph, k);
  while (tree.step()) tree.clearLog();
  return tree;
}

function layoutOf(sample: string, k: number): TreeLayout {
  return new TreeLayout(finished(UndirectedGraph.parse(SAMPLES[sample]!), k));
}

describe('TreeLayout', () => {
  it('centres every parent over its children', () => {
    const layout = layoutOf('cluster.txt', 4);

    for (const box of layout.boxes()) {
      const children = layout.childrenOf(box);
      if (children.length === 0) continue;
      const first = centreX(children[0]!);
      const last = centreX(children[children.length - 1]!);
      expect(centreX(box)).toBeCloseTo((first + last) / 2, 6);
    }
  });

  it('never overlaps two boxes on the same row', () => {
    const layout = layoutOf('sample-small.txt', 4);

    for (let level = 0; level <= layout.depth; level++) {
      const row = layout.row(level).sort((a, b) => a.x - b.x);
      for (let i = 1; i < row.length; i++) {
        expect(row[i - 1]!.x + row[i - 1]!.width).toBeLessThanOrEqual(row[i]!.x);
      }
    }
  });

  it('puts deeper nodes further down', () => {
    const layout = layoutOf('cluster.txt', 4);

    for (const box of layout.boxes()) {
      for (const child of layout.childrenOf(box)) {
        expect(child.y).toBeGreaterThan(box.y);
      }
    }
  });

  it('keeps every box inside the reported extent', () => {
    const layout = layoutOf('sample-small.txt', 5);

    for (const box of layout.boxes()) {
      expect(box.x).toBeGreaterThanOrEqual(0);
      expect(box.x + box.width).toBeLessThanOrEqual(layout.width + 0.001);
      expect(box.y + box.height).toBeLessThanOrEqual(layout.height + 0.001);
    }
  });

  it('survives a graph with no edges at all', () => {
    const layout = new TreeLayout(finished(new UndirectedGraph(3), 3));

    expect(layout.boxes()).toHaveLength(1);
    expect(layout.depth).toBe(0);
  });

  it('traces a node back to the root', () => {
    const layout = layoutOf('bowtie.txt', 3);

    expect(layout.pathToRoot('{0, 1, 2}')).toEqual(['[root]', '{0}', '{0, 1}', '{0, 1, 2}']);
  });
});
