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

  it('reaches the end and marks the results found', () => {
    const root = mount();
    const scrubber = root.querySelector('.demo-scrubber') as HTMLInputElement;

    scrubber.value = scrubber.max;
    scrubber.dispatchEvent(new Event('input'));

    expect(root.querySelectorAll('.t-complete').length).toBeGreaterThan(0);
  });
});
