import { describe, expect, it } from 'vitest';
import { UndirectedGraph } from '../src/graph.js';
import { ESUTree } from '../src/esu.js';
import { EsuSession } from '../src/session.js';
import { SAMPLES } from './samples.js';

/** Everything about the tree that a viewer could notice. */
function describeTree(tree: ESUTree): string {
  return tree
    .nodesByLevel()
    .slice(1)
    .map((level) =>
      level
        .map((n) => `${n.subgraphLabel()}${n.possibleStepsLabel()}${n.neighboursLabel()}`)
        .join(' '),
    )
    .join('|');
}

function byStepping(graph: UndirectedGraph, k: number, n: number): string {
  const tree = new ESUTree(graph, k);
  for (let i = 0; i < n; i++) {
    tree.step();
    tree.clearLog();
  }
  return describeTree(tree);
}

describe('EsuSession', () => {
  // Replay is the whole memory model, and it is the one thing that can go
  // wrong silently: a replayed step must be indistinguishable from a
  // stepped-to one.
  for (const [name, text] of Object.entries(SAMPLES)) {
    for (const k of [3, 4]) {
      it(`reaches every step of ${name} at k=${k} exactly as stepping would`, () => {
        const graph = UndirectedGraph.parse(text);
        const session = new EsuSession(graph, k);

        for (let step = 0; step <= session.totalSteps; step++) {
          session.goToStep(step);
          expect(describeTree(session.tree)).toBe(byStepping(graph, k, step));
        }
      });
    }
  }

  it('agrees going backwards and forwards', () => {
    const graph = UndirectedGraph.parse(SAMPLES['cluster.txt']!);
    const forward = new EsuSession(graph, 4);
    const backward = new EsuSession(graph, 4);

    backward.goToStep(backward.totalSteps);
    for (let i = 0; i < 5; i++) backward.stepBack();
    forward.goToStep(forward.totalSteps - 5);

    expect(describeTree(backward.tree)).toBe(describeTree(forward.tree));
  });

  it('will not step past either end', () => {
    const session = new EsuSession(UndirectedGraph.parse(SAMPLES['bowtie.txt']!), 3);

    expect(session.stepBack()).toBe(false);
    expect(session.currentStep).toBe(0);

    session.goToStep(session.totalSteps);
    expect(session.stepForward()).toBe(false);
    expect(session.currentStep).toBe(session.totalSteps);
  });

  it('has nothing highlighted before the search starts', () => {
    const session = new EsuSession(UndirectedGraph.parse(SAMPLES['bowtie.txt']!), 3);

    expect(session.activeSubgraph()).toEqual([]);
    expect(session.activeExtension()).toEqual([]);
  });

  it('names the subgraph being built at a step', () => {
    const session = new EsuSession(UndirectedGraph.parse(SAMPLES['bowtie.txt']!), 3);

    session.goToStep(7);

    expect(session.activeSubgraph()).toEqual([1]);
  });
});
