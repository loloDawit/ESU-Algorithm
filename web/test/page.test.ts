import { describe, expect, it, beforeAll } from 'vitest';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { Demo } from '../src/app.js';

/**
 * The real docs/index.html, not a fixture.
 *
 * Everything else tests the demo in isolation; this checks the page it has to
 * live in still has the hook it mounts into, still ships the fallback, and
 * still loads the bundle by the name the build writes.
 */
// Resolved from the working directory: happy-dom replaces import.meta.url
// with a document URL, which readFileSync will not take.
const page = readFileSync(resolve(process.cwd(), '../docs/index.html'), 'utf8');

describe('the project page', () => {
  it('references the built bundle by the name vite writes', () => {
    expect(page).toContain('demo/demo.js');
    expect(page).toContain('demo/demo.css');
  });

  it('keeps the clip as a fallback', () => {
    expect(page).toContain('id="demo-fallback"');
    expect(page).toContain('demo.mp4');
  });

  describe('once the demo mounts into it', () => {
    beforeAll(() => {
      document.body.innerHTML = page.slice(page.indexOf('<body>') + 6);
    });

    it('has the hook the script looks for, hidden until then', () => {
      const mount = document.getElementById('demo-mount');

      expect(mount).not.toBeNull();
      expect(mount!.hasAttribute('hidden')).toBe(true);
    });

    it('shows the demo and hides the clip', () => {
      const mount = document.getElementById('demo-mount')!;
      const fallback = document.getElementById('demo-fallback')!;

      new Demo(mount, { autoplay: false });
      mount.removeAttribute('hidden');
      fallback.setAttribute('hidden', '');

      expect(mount.querySelectorAll('.g-vertex').length).toBeGreaterThan(0);
      expect(mount.querySelector('.demo-play')).not.toBeNull();
      expect(fallback.hasAttribute('hidden')).toBe(true);
    });
  });
});
