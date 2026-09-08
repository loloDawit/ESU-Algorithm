/**
 * Where every node of the search tree goes.
 *
 * Ported from `TreeLayout.java`. Walks the tree bottom-up: leaves take the
 * next free slot along the row, and every parent is centred over its
 * children. Subtrees occupy disjoint ranges of slots, so nothing overlaps.
 *
 * Free of any rendering concern, so the geometry is testable on its own.
 */
import type { ESUTree } from './esu.js';

const BOX_WIDTH = 74;
const BOX_HEIGHT = 30;
const COLUMN_GAP = 16;
const ROW_GAP = 46;
/** Space above the root for its caption. */
const CAPTION_ROOM = 16;

export const ROOT_ID = '[root]';
export const ROOT_LABEL = 'start';

export interface Box {
  readonly id: string;
  readonly label: string;
  readonly level: number;
  readonly parentId: string | null;
  x: number;
  readonly y: number;
  readonly width: number;
  readonly height: number;
}

export const centreX = (box: Box): number => box.x + box.width / 2;

export class TreeLayout {
  private readonly byId = new Map<string, Box>();
  private readonly kids = new Map<string, Box[]>();
  readonly depth: number;
  readonly width: number;
  readonly height: number;

  private nextLeafX = 0;

  constructor(tree: ESUTree) {
    const root: Box = {
      id: ROOT_ID,
      label: ROOT_LABEL,
      level: 0,
      parentId: null,
      x: 0,
      y: CAPTION_ROOM,
      width: BOX_WIDTH,
      height: BOX_HEIGHT,
    };
    this.byId.set(ROOT_ID, root);
    this.kids.set(ROOT_ID, []);

    const levels = tree.nodesByLevel();
    for (let level = 1; level < levels.length; level++) {
      for (const node of levels[level]!) {
        const id = node.subgraphLabel();
        const parentId = level === 1 ? ROOT_ID : node.parent!.subgraphLabel();
        const box: Box = {
          id,
          // No set notation: the boxes are small and the punctuation meant
          // nothing to a reader without a key.
          label: node.subgraph().join(' '),
          level,
          parentId,
          x: 0,
          y: CAPTION_ROOM + level * (BOX_HEIGHT + ROW_GAP),
          width: BOX_WIDTH,
          height: BOX_HEIGHT,
        };
        this.byId.set(id, box);
        if (!this.kids.has(parentId)) this.kids.set(parentId, []);
        this.kids.get(parentId)!.push(box);
        if (!this.kids.has(id)) this.kids.set(id, []);
      }
    }

    this.place(root);

    // Depth comes from the boxes that exist, not the tree's declared height:
    // a graph with no edges produces a root and nothing else.
    let deepest = 0;
    for (const box of this.byId.values()) deepest = Math.max(deepest, box.level);
    this.depth = deepest;
    this.width = Math.max(this.nextLeafX - COLUMN_GAP, BOX_WIDTH);
    this.height = CAPTION_ROOM + (deepest + 1) * BOX_HEIGHT + deepest * ROW_GAP;
  }

  private place(box: Box): void {
    const children = this.childrenOf(box);
    if (children.length === 0) {
      box.x = this.nextLeafX;
      this.nextLeafX += BOX_WIDTH + COLUMN_GAP;
      return;
    }
    for (const child of children) this.place(child);
    const first = centreX(children[0]!);
    const last = centreX(children[children.length - 1]!);
    box.x = (first + last) / 2 - BOX_WIDTH / 2;
  }

  get(id: string): Box | undefined {
    return this.byId.get(id);
  }

  boxes(): Box[] {
    return [...this.byId.values()];
  }

  childrenOf(box: Box): Box[] {
    return this.kids.get(box.id) ?? [];
  }

  row(level: number): Box[] {
    return this.boxes().filter((box) => box.level === level);
  }

  /** The chain of choices from the root down to a node. */
  pathToRoot(id: string): string[] {
    const path: string[] = [];
    let box = this.byId.get(id);
    while (box) {
      path.unshift(box.id);
      box = box.parentId === null ? undefined : this.byId.get(box.parentId);
    }
    return path;
  }
}

/** Node ids that never gained a child, so their branch died. */
export function deadEnds(finalTree: ESUTree): Set<string> {
  const dead = new Set<string>();
  const levels = finalTree.nodesByLevel();
  for (let level = 1; level < levels.length - 1; level++) {
    for (const node of levels[level]!) {
      if (node.children.length === 0) dead.add(node.subgraphLabel());
    }
  }
  return dead;
}
