/**
 * An undirected graph over vertices `0 .. size - 1`.
 *
 * Ported from `UndirectedGraph.java`. Keeps the adjacency-matrix
 * representation: the graphs this draws legibly are small, and the matrix
 * makes `areAdjacent` a constant-time lookup during rendering.
 */
export class UndirectedGraph {
  private readonly matrix: boolean[][];

  constructor(readonly size: number) {
    this.matrix = Array.from({ length: size }, () =>
      new Array<boolean>(size).fill(false),
    );
  }

  /**
   * Parse the same edge-per-line format the desktop app reads: two
   * whitespace-separated whole numbers per line.
   *
   * The matrix is sized to the largest vertex id **plus one**. Getting that
   * wrong is what made the 2018 app silently drop every edge touching its
   * highest-numbered vertex.
   */
  static parse(text: string): UndirectedGraph {
    const edges: Array<[number, number]> = [];
    let largest = -1;

    for (const line of text.split('\n')) {
      const parts = line.trim().split(/\s+/).filter((p) => p.length > 0);
      if (parts.length < 2) continue;

      const from = Number(parts[0]);
      const to = Number(parts[1]);
      if (!Number.isInteger(from) || !Number.isInteger(to)) continue;
      if (from < 0 || to < 0) continue;

      edges.push([from, to]);
      largest = Math.max(largest, from, to);
    }

    const graph = new UndirectedGraph(largest + 1);
    for (const [from, to] of edges) graph.addEdge(from, to);
    return graph;
  }

  addEdge(from: number, to: number): void {
    if (!this.inRange(from) || !this.inRange(to)) return;
    this.matrix[from]![to] = true;
    this.matrix[to]![from] = true;
  }

  areAdjacent(from: number, to: number): boolean {
    return this.inRange(from) && this.inRange(to) && this.matrix[from]![to]!;
  }

  /** Neighbours of a vertex, ascending. Empty for a vertex out of range. */
  neighbours(vertex: number): number[] {
    if (!this.inRange(vertex)) return [];
    const row = this.matrix[vertex]!;
    const out: number[] = [];
    for (let i = 0; i < this.size; i++) if (row[i]) out.push(i);
    return out;
  }

  /** Vertices taking part in at least one edge, ascending. */
  connectedVertices(): number[] {
    const out: number[] = [];
    for (let v = 0; v < this.size; v++) {
      if (this.matrix[v]!.some(Boolean)) out.push(v);
    }
    return out;
  }

  private inRange(vertex: number): boolean {
    return Number.isInteger(vertex) && vertex >= 0 && vertex < this.size;
  }
}
