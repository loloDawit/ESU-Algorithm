/**
 * One run of the algorithm over one graph, positioned at a step.
 *
 * Ported from `EsuSession.java`, including its central decision: rather than
 * keeping a copy of the tree after every step so it can go backwards, it
 * reaches a step by replaying the search. Enumerating from scratch is cheap
 * enough that re-running beats remembering.
 */
import { EsuNode, EsuTree, type StepEntry } from './esu.js';
import { UndirectedGraph } from './graph.js';
import { Shape, type ShapeCount } from './shape.js';

export class EsuSession {
  private readonly finalTreeValue: EsuTree;
  readonly totalSteps: number;
  readonly subgraphCount: number;

  private shapesFound: ShapeCount[] | null = null;
  private currentTree!: EsuTree;
  private currentStepValue = 0;

  constructor(
    readonly graph: UndirectedGraph,
    readonly subgraphSize: number,
  ) {
    const tree = new EsuTree(graph, subgraphSize);
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

  get finalTree(): EsuTree {
    return this.finalTreeValue;
  }

  get tree(): EsuTree {
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
    const tree = new EsuTree(this.graph, this.subgraphSize);
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

  /**
   * The shapes among the subgraphs found, most frequent first. Worked out on
   * first asking; the search does not change.
   */
  shapes(): ShapeCount[] {
    if (!this.shapesFound) {
      this.shapesFound = Shape.classify(this.graph, this.finalTreeValue.subgraphs());
    }
    return this.shapesFound;
  }

  /**
   * The subgraphs having a given shape, as their vertices, so they can be
   * drawn.
   *
   * Always every one of them, whatever step is being shown: the shapes are a
   * property of the finished search, and the counts beside them are final.
   */
  subgraphsOfShape(shape: Shape): number[][] {
    return this.finalTreeValue
      .subgraphs()
      .filter((subgraph) => Shape.of(this.graph, subgraph).equals(shape));
  }

  /**
   * The tree nodes whose subgraph has a given shape, named the way the tree
   * names them, so the view can pick them out.
   */
  subgraphsWithShape(shape: Shape): Set<string> {
    const found = new Set<string>();
    for (const subgraph of this.finalTreeValue.subgraphs()) {
      if (Shape.of(this.graph, subgraph).equals(shape)) {
        found.add(`{${subgraph.join(', ')}}`);
      }
    }
    return found;
  }

  /** The node this step is building, found by the label its log entries carry. */
  activeNode(): EsuNode | null {
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
