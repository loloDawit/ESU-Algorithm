/**
 * The demo: pick a graph and a subgraph size, then step or play through the
 * search while the graph and the tree stay in step with each other.
 *
 * A cut-down version of the desktop app — no file loading, no random
 * generation, no export. Those need a filesystem and add nothing to a demo.
 */
import { UndirectedGraph } from './graph.js';
import { EsuSession } from './session.js';
import { TreeLayout, deadEnds } from './layout.js';
import { renderGraph, renderTree, shapeDrawing } from './render.js';
import { Shape } from './shape.js';
import { SAMPLES } from './samples.js';
import './demo.css';

const MIN_SIZE = 2;
const MAX_SIZE = 5;
const GRAPH_BOX = { width: 260, height: 220 };

export class Demo {
  private session!: EsuSession;
  private layout!: TreeLayout;
  private dead!: ReadonlySet<string>;
  private timer: number | null = null;

  private sampleIndex = 0;
  private size = 4;

  private readonly graphSvg: SVGSVGElement;
  private readonly treeSvg: SVGSVGElement;
  private readonly logEl: HTMLElement;
  private readonly statusEl: HTMLElement;
  private readonly subgraphEl: HTMLElement;
  private readonly extensionEl: HTMLElement;
  private readonly playButton: HTMLButtonElement;
  private readonly scrubber: HTMLInputElement;
  private readonly shapesEl: HTMLElement;
  private picked: ReadonlySet<string> = new Set();
  private pickedShape: Shape | null = null;
  private readonly sampleButtons: HTMLButtonElement[] = [];
  private readonly sizeButtons: HTMLButtonElement[] = [];

  constructor(
    private readonly root: HTMLElement,
    private readonly options: { autoplay?: boolean } = {},
  ) {
    root.classList.add('demo');
    root.innerHTML = TEMPLATE;

    this.graphSvg = this.find('.demo-graph') as unknown as SVGSVGElement;
    this.treeSvg = this.find('.demo-tree') as unknown as SVGSVGElement;
    this.logEl = this.find('.demo-log');
    this.statusEl = this.find('.demo-status');
    this.subgraphEl = this.find('.demo-subgraph');
    this.extensionEl = this.find('.demo-extension');
    this.shapesEl = this.find('.demo-shapes');
    this.playButton = this.find('.demo-play') as HTMLButtonElement;
    this.scrubber = this.find('.demo-scrubber') as HTMLInputElement;

    this.buildChoices();
    this.wire();
    this.load();
  }

  private find(selector: string): HTMLElement {
    const found = this.root.querySelector(selector);
    if (!found) throw new Error(`demo: missing ${selector}`);
    return found as HTMLElement;
  }

  private buildChoices(): void {
    const graphs = this.find('.demo-graphs');
    SAMPLES.forEach((sample, index) => {
      const button = document.createElement('button');
      button.type = 'button';
      button.className = 'demo-pill';
      button.textContent = sample.name;
      button.addEventListener('click', () => {
        this.sampleIndex = index;
        this.load();
      });
      graphs.append(button);
      this.sampleButtons.push(button);
    });

    const sizes = this.find('.demo-sizes');
    for (let size = MIN_SIZE; size <= MAX_SIZE; size++) {
      const button = document.createElement('button');
      button.type = 'button';
      button.className = 'demo-pill';
      button.textContent = String(size);
      button.addEventListener('click', () => {
        this.size = size;
        this.load();
      });
      sizes.append(button);
      this.sizeButtons.push(button);
    }
  }

  private wire(): void {
    this.playButton.addEventListener('click', () => {
      if (this.timer === null) this.play();
      else this.pause();
    });
    this.find('.demo-next').addEventListener('click', () => {
      this.pause();
      this.session.stepForward();
      this.draw();
    });
    this.find('.demo-prev').addEventListener('click', () => {
      this.pause();
      this.session.stepBack();
      this.draw();
    });
    this.find('.demo-restart').addEventListener('click', () => {
      this.pause();
      this.session.goToStep(0);
      this.draw();
    });
    // Replay makes any step reachable, so the scrubber costs nothing.
    this.scrubber.addEventListener('input', () => {
      this.pause();
      this.session.goToStep(Number(this.scrubber.value));
      this.draw();
    });
  }

  private load(): void {
    this.pause();
    const graph = UndirectedGraph.parse(SAMPLES[this.sampleIndex]!.text);
    this.session = new EsuSession(graph, this.size);
    this.layout = new TreeLayout(this.session.finalTree);
    this.dead = deadEnds(this.session.finalTree);

    this.picked = new Set();
    this.pickedShape = null;
    this.drawShapes();
    this.scrubber.max = String(this.session.totalSteps);
    this.sampleButtons.forEach((b, i) =>
      b.classList.toggle('is-on', i === this.sampleIndex));
    this.sizeButtons.forEach((b, i) =>
      b.classList.toggle('is-on', i + MIN_SIZE === this.size));

    this.draw();
    if (this.options.autoplay !== false) this.play();
  }

  private draw(): void {
    const active = this.session.activeNode();
    const activeId = active?.subgraphLabel() ?? null;

    const present = new Set<string>();
    for (const level of this.session.tree.nodesByLevel().slice(1)) {
      for (const node of level) present.add(node.subgraphLabel());
    }

    renderTree(this.treeSvg, this.layout, {
      present,
      deadEnds: this.dead,
      foundAt: this.size,
      activeId,
      path: new Set(activeId ? this.layout.pathToRoot(activeId) : []),
      picked: this.picked,
    });

    const subgraph = this.session.activeSubgraph();
    const extension = this.session.activeExtension();
    renderGraph(
      this.graphSvg,
      this.session.graph,
      { subgraph: new Set(subgraph), extension: new Set(extension) },
      GRAPH_BOX,
    );

    this.subgraphEl.textContent = subgraph.length ? subgraph.join(' ') : '—';
    this.extensionEl.textContent = extension.length ? extension.join(' ') : '—';

    this.logEl.replaceChildren(
      ...this.session.log.map((entry) => {
        const line = document.createElement('li');
        line.textContent = entry.text;
        return line;
      }),
    );

    this.scrubber.value = String(this.session.currentStep);
    const found = this.session.subgraphCount;
    this.statusEl.textContent =
      `Step ${this.session.currentStep} of ${this.session.totalSteps}`
      + ` · ${found} subgraph${found === 1 ? '' : 's'} of size ${this.size}`;
  }

  /**
   * The shapes found, each drawn with its count. Choosing one picks its
   * subgraphs out in the tree, which is the question the row invites.
   */
  private drawShapes(): void {
    const shapes = this.session.shapes();
    this.shapesEl.replaceChildren();

    for (const { shape, count } of shapes) {
      const chip = document.createElement('button');
      chip.type = 'button';
      chip.className = 'demo-shape';
      chip.title = `${shape.describe()} — ${count}`;
      chip.append(shapeDrawing(shape), Object.assign(
        document.createElement('span'), { className: 'demo-shape-n', textContent: String(count) },
      ));
      chip.addEventListener('click', () => this.pickShape(shape));
      this.shapesEl.append(chip);
    }
  }

  /** Pick out one shape's subgraphs, or clear the choice by repeating it. */
  private pickShape(shape: Shape): void {
    const again = this.pickedShape !== null && this.pickedShape.equals(shape);
    this.pickedShape = again ? null : shape;
    this.picked = again ? new Set() : this.session.subgraphsWithShape(shape);

    for (const chip of Array.from(this.shapesEl.children)) {
      chip.classList.remove('is-on');
    }
    if (!again) {
      const at = this.session.shapes().findIndex((entry) => entry.shape.equals(shape));
      this.shapesEl.children[at]?.classList.add('is-on');
    }
    this.draw();
  }

  private play(): void {
    this.pause();
    if (this.session.currentStep >= this.session.totalSteps) {
      this.session.goToStep(0);
    }
    this.playButton.textContent = 'Pause';
    this.playButton.setAttribute('aria-label', 'Pause');
    this.timer = window.setInterval(() => {
      if (!this.session.stepForward()) {
        this.pause();
        return;
      }
      this.draw();
    }, 420);
  }

  private pause(): void {
    if (this.timer !== null) {
      window.clearInterval(this.timer);
      this.timer = null;
    }
    this.playButton.textContent = 'Play';
    this.playButton.setAttribute('aria-label', 'Play');
  }
}

const TEMPLATE = `
  <div class="demo-main">
    <div class="demo-side">
      <span class="demo-title">Input graph</span>
      <svg class="demo-graph" role="img"
           aria-label="The graph being searched, with the current subgraph highlighted"></svg>
      <dl class="demo-sets">
        <dt>Subgraph</dt><dd class="demo-subgraph">—</dd>
        <dt>Extension</dt><dd class="demo-extension">—</dd>
      </dl>
      <span class="demo-title">Shapes found</span>
      <div class="demo-shapes"></div>
      <span class="demo-title">This step</span>
      <ul class="demo-log"></ul>
    </div>
    <div class="demo-treewrap">
      <svg class="demo-tree" role="img"
           aria-label="The search tree, growing one step at a time"></svg>
    </div>
  </div>
  <div class="demo-bar">
    <div class="demo-choices">
      <span class="demo-caption">Graph</span>
      <span class="demo-graphs"></span>
      <span class="demo-caption">Size</span>
      <span class="demo-sizes"></span>
    </div>
    <div class="demo-transport">
      <button type="button" class="demo-btn demo-restart" aria-label="Back to the start">&#171;</button>
      <button type="button" class="demo-btn demo-prev" aria-label="Previous step">&#8249;</button>
      <button type="button" class="demo-btn demo-play">Play</button>
      <button type="button" class="demo-btn demo-next" aria-label="Next step">&#8250;</button>
      <input class="demo-scrubber" type="range" min="0" max="1" value="0" aria-label="Step">
    </div>
  </div>
  <div class="demo-footer">
    <span class="demo-status"></span>
    <span class="demo-legend">
      <span><i class="sw sw-active"></i>working on</span>
      <span><i class="sw sw-complete"></i>found</span>
      <span><i class="sw sw-dead"></i>dead end</span>
    </span>
  </div>
`;
