/**
 * Drawing the graph and the search tree as SVG.
 *
 * Colours are left to CSS classes rather than set here, so the demo follows
 * the light or dark theme of the page it sits in.
 */
import type { UndirectedGraph } from './graph.js';
import { circlePositions, centreX, type Box, type TreeLayout } from './layout.js';

const SVG_NS = 'http://www.w3.org/2000/svg';

function el<K extends keyof SVGElementTagNameMap>(
  name: K,
  attrs: Record<string, string | number>,
): SVGElementTagNameMap[K] {
  const node = document.createElementNS(SVG_NS, name);
  for (const [key, value] of Object.entries(attrs)) {
    node.setAttribute(key, String(value));
  }
  return node;
}

/** What the algorithm is doing with a vertex right now. */
export interface Highlight {
  readonly subgraph: ReadonlySet<number>;
  readonly extension: ReadonlySet<number>;
}

/**
 * Draw the input graph with the current subgraph picked out inside it.
 *
 * @param svg       the element to draw into; its contents are replaced
 * @param graph     the graph being searched
 * @param highlight which vertices are chosen and which are candidates
 * @param size      the viewBox to lay out within
 */
export function renderGraph(
  svg: SVGSVGElement,
  graph: UndirectedGraph,
  highlight: Highlight,
  size: { width: number; height: number },
): void {
  svg.setAttribute('viewBox', `0 0 ${size.width} ${size.height}`);
  svg.replaceChildren();

  const vertices = graph.connectedVertices();
  if (vertices.length === 0) return;

  const points = circlePositions(vertices, size.width, size.height);
  const at = new Map(points.map((p) => [p.vertex, p]));

  // Edges first, so vertices sit on top of them.
  for (let i = 0; i < vertices.length; i++) {
    for (let j = i + 1; j < vertices.length; j++) {
      const from = vertices[i]!;
      const to = vertices[j]!;
      if (!graph.areAdjacent(from, to)) continue;

      const inside = highlight.subgraph.has(from) && highlight.subgraph.has(to);
      svg.append(
        el('line', {
          x1: at.get(from)!.x,
          y1: at.get(from)!.y,
          x2: at.get(to)!.x,
          y2: at.get(to)!.y,
          class: inside ? 'g-edge g-edge-in' : 'g-edge',
        }),
      );
    }
  }

  for (const point of points) {
    const chosen = highlight.subgraph.has(point.vertex);
    const candidate = !chosen && highlight.extension.has(point.vertex);
    const state = chosen ? 'chosen' : candidate ? 'candidate' : 'plain';

    svg.append(
      el('circle', { cx: point.x, cy: point.y, r: 17, class: `g-vertex g-${state}` }),
    );
    const label = el('text', {
      x: point.x,
      y: point.y,
      class: `g-label g-label-${state}`,
      'text-anchor': 'middle',
      'dominant-baseline': 'central',
    });
    label.textContent = String(point.vertex);
    svg.append(label);
  }
}

/** Everything the tree drawing needs to know about the current step. */
export interface TreeState {
  /** Ids of the nodes that exist at this step. */
  readonly present: ReadonlySet<string>;
  /** Ids that never gained a child in the finished tree. */
  readonly deadEnds: ReadonlySet<string>;
  /** The level whose nodes are complete subgraphs. */
  readonly foundAt: number;
  /** The node being worked on, if any. */
  readonly activeId: string | null;
  /** Ids from the root to the node of interest. */
  readonly path: ReadonlySet<string>;
}

/**
 * Draw the search tree as it stands.
 *
 * @param svg    the element to draw into; its contents are replaced
 * @param layout positions for the whole finished tree
 * @param state  what the current step makes of each node
 */
export function renderTree(
  svg: SVGSVGElement,
  layout: TreeLayout,
  state: TreeState,
): void {
  svg.setAttribute('viewBox', `-8 0 ${layout.width + 16} ${layout.height + 8}`);
  svg.replaceChildren();

  const visible = (box: Box): boolean =>
    box.parentId === null || state.present.has(box.id);

  // Connectors first so the boxes sit on top of them.
  for (const box of layout.boxes()) {
    if (box.parentId === null || !visible(box)) continue;
    const parent = layout.get(box.parentId);
    if (!parent || !visible(parent)) continue;

    // A connector is on the traced path only when both ends are.
    const onPath = state.path.has(box.id) && state.path.has(parent.id);
    const midY = (parent.y + parent.height + box.y) / 2;
    svg.append(
      el('polyline', {
        points: `${centreX(parent)},${parent.y + parent.height} `
          + `${centreX(parent)},${midY} ${centreX(box)},${midY} ${centreX(box)},${box.y}`,
        class: onPath ? 't-edge t-edge-path' : 't-edge',
      }),
    );
  }

  for (const box of layout.boxes()) {
    if (!visible(box)) continue;
    svg.append(...boxShapes(box, state));
  }
}

function boxShapes(box: Box, state: TreeState): SVGElement[] {
  const classes = ['t-box'];
  if (box.parentId === null) classes.push('t-root');
  else if (box.id === state.activeId) classes.push('t-active');
  else if (box.level === state.foundAt) classes.push('t-complete');
  else if (state.deadEnds.has(box.id)) classes.push('t-dead');
  else classes.push('t-pending');
  if (state.path.has(box.id) && box.id !== state.activeId) classes.push('t-on-path');

  const shapes: SVGElement[] = [
    el('rect', {
      x: box.x,
      y: box.y,
      width: box.width,
      height: box.height,
      rx: 8,
      class: classes.join(' '),
    }),
  ];

  const label = el('text', {
    x: centreX(box),
    y: box.y + box.height / 2,
    class: classes.includes('t-dead') ? 't-text t-text-dead' : 't-text',
    'text-anchor': 'middle',
    'dominant-baseline': 'central',
  });
  label.textContent = box.label;
  shapes.push(label);

  if (box.parentId === null) {
    // The root is not a subgraph, and nothing else on screen says so.
    const caption = el('text', {
      x: centreX(box),
      y: box.y - 6,
      class: 't-caption',
      'text-anchor': 'middle',
    });
    caption.textContent = 'no vertices chosen';
    shapes.push(caption);
  }
  return shapes;
}
