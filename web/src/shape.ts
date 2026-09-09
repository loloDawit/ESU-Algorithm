/**
 * The shape of a subgraph, independent of which vertices happen to form it.
 *
 * Ported from `Shape.java`. Enumerating connected subgraphs is the first step
 * of motif discovery; this is the second. Until subgraphs are grouped by
 * shape, {0, 1, 2} and {2, 3, 4} are two answers rather than two triangles.
 */
import type { UndirectedGraph } from './graph.js';

/** Beyond this the number of relabellings stops being trivial. */
export const MAX_SHAPE_SIZE = 8;

export class Shape {
  private constructor(
    readonly size: number,
    /** Smallest adjacency bitstring over every relabelling. */
    private readonly canonical: number,
    readonly edges: number,
  ) {}

  /**
   * The shape of the subgraph a set of vertices induces.
   *
   * The canonical form is found by brute force: every relabelling is tried
   * and the smallest result kept. Industrial tools use nauty, which earns its
   * keep at the sizes they work at; here a subgraph has at most a few
   * vertices, so a few hundred relabellings cost microseconds.
   */
  static of(graph: UndirectedGraph, vertices: readonly number[]): Shape {
    const size = vertices.length;
    if (size > MAX_SHAPE_SIZE) {
      throw new Error(
        `subgraphs of more than ${MAX_SHAPE_SIZE} vertices are not classified: `
        + 'there are too many relabellings to try',
      );
    }

    const adjacent: boolean[][] = Array.from({ length: size }, () =>
      new Array<boolean>(size).fill(false));
    let edges = 0;
    for (let row = 0; row < size; row++) {
      for (let column = row + 1; column < size; column++) {
        if (graph.areAdjacent(vertices[row]!, vertices[column]!)) {
          adjacent[row]![column] = true;
          adjacent[column]![row] = true;
          edges++;
        }
      }
    }

    let smallest = Number.MAX_SAFE_INTEGER;
    for (const relabelling of permutations(size)) {
      smallest = Math.min(smallest, bits(adjacent, relabelling));
    }
    return new Shape(size, smallest, edges);
  }

  /**
   * Group subgraphs by shape, most frequent first.
   *
   * Ties are broken by the shape itself rather than by which subgraph arrived
   * first, so this listing matches the Java one exactly.
   */
  static classify(
    graph: UndirectedGraph,
    subgraphs: readonly (readonly number[])[],
  ): ShapeCount[] {
    const tally = new Map<string, { shape: Shape; count: number }>();
    for (const subgraph of subgraphs) {
      const shape = Shape.of(graph, subgraph);
      const key = shape.key();
      const seen = tally.get(key);
      if (seen) {
        seen.count++;
      } else {
        tally.set(key, { shape, count: 1 });
      }
    }
    return [...tally.values()]
      .map(({ shape, count }) => ({ shape, count }))
      .sort((a, b) => b.count - a.count || a.shape.compare(b.shape));
  }

  /** Identity, for use as a map key and for equality. */
  key(): string {
    return `${this.size}:${this.canonical}`;
  }

  equals(other: Shape): boolean {
    return this.key() === other.key();
  }

  /** Orders by size then canonical form: arbitrary, but the same everywhere. */
  compare(other: Shape): number {
    return this.size !== other.size
      ? this.size - other.size
      : this.canonical - other.canonical;
  }

  /**
   * Whether two of the shape's vertices are joined, in its canonical form.
   * Enough to draw it.
   */
  joins(from: number, to: number): boolean {
    if (from === to) return false;
    const low = Math.min(from, to);
    const high = Math.max(from, to);

    // bits() shifts left as it goes, so the first pair it writes ends up in
    // the highest bit: a pair's position is counted from the end.
    const pairs = (this.size * (this.size - 1)) / 2;
    let index = 0;
    for (let row = 0; row < this.size; row++) {
      for (let column = row + 1; column < this.size; column++) {
        if (row === low && column === high) {
          return ((this.canonical >> (pairs - 1 - index)) & 1) === 1;
        }
        index++;
      }
    }
    return false;
  }

  /**
   * What people call this shape, where they call it anything. Only the small
   * ones have names in common use; above four the drawing says more.
   */
  name(): string {
    if (this.size === 2) return 'edge';
    if (this.size === 3) return this.edges === 3 ? 'triangle' : 'path';
    if (this.size === 4) {
      const highest = this.degrees()[3];
      switch (this.edges) {
        case 3: return highest === 3 ? 'star' : 'path';
        case 4: return highest === 3 ? 'triangle and tail' : 'cycle';
        case 5: return 'diamond';
        case 6: return 'clique';
        default: return '';
      }
    }
    return '';
  }

  /** How the shape reads when it has no common name. */
  describe(): string {
    const name = this.name();
    return name || `${this.size} vertices, ${this.edges} edges`;
  }

  /** Vertex degrees, ascending. */
  private degrees(): number[] {
    const degrees = new Array<number>(this.size).fill(0);
    for (let row = 0; row < this.size; row++) {
      for (let column = row + 1; column < this.size; column++) {
        if (this.joins(row, column)) {
          degrees[row]!++;
          degrees[column]!++;
        }
      }
    }
    return degrees.sort((a, b) => a - b);
  }
}

export interface ShapeCount {
  readonly shape: Shape;
  readonly count: number;
}

/** The adjacency of a subgraph as a bitstring under one relabelling. */
function bits(adjacent: boolean[][], relabelling: number[]): number {
  let out = 0;
  for (let row = 0; row < relabelling.length; row++) {
    for (let column = row + 1; column < relabelling.length; column++) {
      out = out * 2 + (adjacent[relabelling[row]!]![relabelling[column]!] ? 1 : 0);
    }
  }
  return out;
}

/** Every ordering of 0..size-1, by Heap's algorithm. */
function permutations(size: number): number[][] {
  const values = Array.from({ length: size }, (_, at) => at);
  const out: number[][] = [];

  const permute = (upTo: number): void => {
    if (upTo === 1) {
      out.push([...values]);
      return;
    }
    for (let at = 0; at < upTo; at++) {
      permute(upTo - 1);
      const swapWith = upTo % 2 === 0 ? at : 0;
      [values[swapWith], values[upTo - 1]] = [values[upTo - 1]!, values[swapWith]!];
    }
  };
  permute(size);
  return out;
}
