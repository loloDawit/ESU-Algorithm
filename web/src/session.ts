/**
 * One run of the algorithm over one graph, positioned at a step.
 *
 * Ported from `EsuSession.java`, including its central decision: rather than
 * keeping a copy of the tree after every step so it can go backwards, it
 * reaches a step by replaying the search. Enumerating from scratch is cheap
 * enough that re-running beats remembering.
 */
import { ESUNode, ESUTree, type StepEntry } from './esu.js';
import { UndirectedGraph } from './graph.js';

export class EsuSession {
  private readonly finalTreeValue: ESUTree;
  readonly totalSteps: number;
  readonly subgraphCount: number;

  private currentTree!: ESUTree;
  private currentStepValue = 0;

  constructor(
    readonly graph: UndirectedGraph,
    readonly subgraphSize: number,
  ) {
    const tree = new ESUTree(graph, subgraphSize);
    let steps = 0;
    while (tree.step()) {
      tree.clearLog();
      steps++;
    }
    this.finalTreeValue = tree;
    this.totalSteps = steps;
    this.subgraphCount = tree.subgraphs().length;

    this.goToStep(0);
  }

  get finalTree(): ESUTree {
    return this.finalTreeValue;
  }

  get tree(): ESUTree {
    return this.currentTree;
  }

  get currentStep(): number {
    return this.currentStepValue;
  }

  get log(): StepEntry[] {
    return this.currentTree.log;
  }

  /** Move to a step, replaying from the start to get there. */
  goToStep(step: number): void {
    const target = Math.max(0, Math.min(this.totalSteps, Math.trunc(step)));
    const tree = new ESUTree(this.graph, this.subgraphSize);
    for (let taken = 0; taken < target; taken++) {
      tree.clearLog();
      tree.step();
    }
    this.currentTree = tree;
    this.currentStepValue = target;
  }

  /** Advance one step on the live tree, without replaying. */
  stepForward(): boolean {
    if (this.currentStepValue >= this.totalSteps) return false;
    this.currentTree.clearLog();
    this.currentTree.step();
    this.currentStepValue++;
    return true;
  }

  stepBack(): boolean {
    if (this.currentStepValue <= 0) return false;
    this.goToStep(this.currentStepValue - 1);
    return true;
  }

  /** The node this step is building, found by the label its log entries carry. */
  activeNode(): ESUNode | null {
    const last = this.currentTree.log[this.currentTree.log.length - 1];
    if (!last) return null;

    const levels = this.currentTree.nodesByLevel();
    for (let depth = 1; depth < levels.length; depth++) {
      for (const node of levels[depth]!) {
        if (node.subgraphLabel() === last.caller) return node;
      }
    }
    return null;
  }

  /** Vertices of the subgraph being built, for highlighting in the graph. */
  activeSubgraph(): number[] {
    return this.activeNode()?.subgraph() ?? [];
  }

  /** Vertices it could add next. */
  activeExtension(): number[] {
    const node = this.activeNode();
    return node ? [...node.possibleSteps.values()].sort((a, b) => a - b) : [];
  }
}
