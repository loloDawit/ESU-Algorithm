import { describe, expect, it } from 'vitest';
import { Demo } from '../src/app.js';

/**
 * The desktop app has no automated coverage of its views, which is how a
 * button went missing in a rebuild and stayed missing until someone read the
 * README. These check that the demo actually builds its controls and draws
 * something, without needing a browser.
 */
function mount(): HTMLElement {
  const root = document.createElement('div');
  document.body.append(root);
  new Demo(root, { autoplay: false });
  return root;
}

/**
 * Mount, then choose a graph that yields several shapes.
 *
 * The default is a bowtie, which at size four has exactly one shape, and a
 * test that every shape draws differently passes trivially against one shape.
 * It did: this is the second time a check here was vacuous because the case
 * chosen had no variety in it.
 */
function mountWithSeveralShapes(): HTMLElement {
  const root = mount();
  const graphs = root.querySelectorAll('.demo-graphs .demo-pill');
  (graphs[2] as HTMLButtonElement).click();   // Cluster
  return root;
}

describe('the demo', () => {
  it('draws the graph it was given', () => {
    const root = mount();

    // Bowtie: five vertices, six edges.
    expect(root.querySelectorAll('.g-vertex')).toHaveLength(5);
    expect(root.querySelectorAll('.g-edge')).toHaveLength(6);
  });

  it('starts before the first step, showing only the root', () => {
    const root = mount();

    expect(root.querySelectorAll('.t-box')).toHaveLength(1);
    expect(root.querySelector('.t-root')).not.toBeNull();
    expect(root.querySelector('.demo-status')?.textContent).toContain('Step 0 of');
  });

  it('grows the tree as it steps', () => {
    const root = mount();
    const next = root.querySelector('.demo-next') as HTMLButtonElement;

    next.click();
    next.click();

    expect(root.querySelectorAll('.t-box').length).toBeGreaterThan(1);
    expect(root.querySelector('.demo-log')?.children.length).toBeGreaterThan(0);
  });

  it('highlights the subgraph being built in the graph', () => {
    const root = mount();
    const next = root.querySelector('.demo-next') as HTMLButtonElement;

    next.click();

    expect(root.querySelectorAll('.g-chosen').length).toBeGreaterThan(0);
    expect(root.querySelector('.demo-subgraph')?.textContent).not.toBe('—');
  });

  it('offers every control', () => {
    const root = mount();

    for (const selector of ['.demo-play', '.demo-next', '.demo-prev',
      '.demo-restart', '.demo-scrubber']) {
      expect(root.querySelector(selector), selector).not.toBeNull();
    }
    expect(root.querySelectorAll('.demo-graphs .demo-pill')).toHaveLength(4);
    expect(root.querySelectorAll('.demo-sizes .demo-pill')).toHaveLength(4);
  });

  it('offers a chip per shape found', () => {
    const root = mountWithSeveralShapes();

    const chips = root.querySelectorAll('.demo-shape');
    expect(chips.length).toBeGreaterThan(0);
    // Each chip carries a drawing and a count.
    for (const chip of Array.from(chips)) {
      expect(chip.querySelector('.shape-drawing')).not.toBeNull();
      expect(chip.querySelector('.demo-shape-n')?.textContent).toMatch(/^\d+$/);
    }
  });

  it('draws each shape with the edges that shape actually has', () => {
    const root = mountWithSeveralShapes();

    // How many edges each named shape has, by definition.
    const edgesOf: Record<string, number> = {
      'path': 3, 'star': 3, 'cycle': 4, 'triangle and tail': 4,
      'diamond': 5, 'clique': 6,
    };

    const chips = Array.from(root.querySelectorAll('.demo-shape'));
    expect(chips.length).toBeGreaterThan(1);

    for (const chip of chips) {
      const name = (chip.getAttribute('title') ?? '').split(' \u2014 ')[0]!;
      const lines = chip.querySelectorAll('.shape-drawing line').length;
      expect(lines, `${name} drew ${lines} edges`).toBe(edgesOf[name]);
    }
  });

  it('draws every shape differently', () => {
    const root = mountWithSeveralShapes();

    const drawings = Array.from(root.querySelectorAll('.shape-drawing'))
      .map((svg) => Array.from(svg.querySelectorAll('line'))
        .map((line) => `${line.getAttribute('x1')},${line.getAttribute('y1')}`
          + `-${line.getAttribute('x2')},${line.getAttribute('y2')}`)
        .sort()
        .join(' '));

    expect(drawings.length).toBeGreaterThan(1);
    expect(new Set(drawings).size).toBe(drawings.length);
  });

  it('picks a shape out in the tree when its chip is chosen', () => {
    const root = mount();
    const scrubber = root.querySelector('.demo-scrubber') as HTMLInputElement;
    scrubber.value = scrubber.max;
    scrubber.dispatchEvent(new Event('input'));

    const chip = root.querySelector('.demo-shape') as HTMLButtonElement;
    chip.click();

    expect(chip.classList.contains('is-on')).toBe(true);
    expect(root.querySelectorAll('.t-picked').length).toBeGreaterThan(0);

    // Choosing it again clears the choice.
    chip.click();
    expect(root.querySelectorAll('.t-picked')).toHaveLength(0);
  });

  it('draws every subgraph behind a chosen shape', () => {
    const root = mountWithSeveralShapes();
    const chip = root.querySelector('.demo-shape') as HTMLButtonElement;
    const wanted = Number(chip.querySelector('.demo-shape-n')!.textContent);

    chip.click();

    // One drawing per subgraph the chip counted, and the count must be worth
    // checking: a single instance would make this pass against anything.
    expect(wanted).toBeGreaterThan(1);
    expect(root.querySelectorAll('.demo-instance')).toHaveLength(wanted);
    expect(root.querySelector('.demo-instances-title')?.textContent)
      .toContain(String(wanted));
  });

  it('labels each subgraph with the vertices it is made of', () => {
    const root = mountWithSeveralShapes();
    (root.querySelector('.demo-shape') as HTMLButtonElement).click();

    const labels = Array.from(root.querySelectorAll('.demo-instance-label'))
      .map((label) => label.textContent);

    // Same shape, different vertices, which is the reason to draw them.
    expect(new Set(labels).size).toBe(labels.length);
    for (const label of labels) {
      expect(label).toMatch(/^\d+( \d+)*$/);
    }
  });

  it('shows a chosen subgraph in the input graph', () => {
    const root = mountWithSeveralShapes();
    (root.querySelector('.demo-shape') as HTMLButtonElement).click();

    const tile = root.querySelector('.demo-instance') as HTMLButtonElement;
    const vertices = tile.querySelector('.demo-instance-label')!
      .textContent!.split(' ').length;
    tile.click();

    expect(tile.classList.contains('is-on')).toBe(true);
    expect(root.querySelectorAll('.g-chosen')).toHaveLength(vertices);
  });

  it('reaches the end and marks the results found', () => {
    const root = mount();
    const scrubber = root.querySelector('.demo-scrubber') as HTMLInputElement;

    scrubber.value = scrubber.max;
    scrubber.dispatchEvent(new Event('input'));

    expect(root.querySelectorAll('.t-complete').length).toBeGreaterThan(0);
  });
});
