/**
 * Entry point: replace the clip on the page with the running demo.
 *
 * The clip is what everyone sees until this script arrives, and what they keep
 * if it never does, so nothing here runs before the demo has been built
 * successfully.
 */
import { Demo } from './app.js';

const mount = document.getElementById('demo-mount');
const fallback = document.getElementById('demo-fallback');

if (mount) {
  // Someone who has asked for less motion gets the demo paused on step 0
  // rather than playing at them.
  const stillPlease = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  new Demo(mount, { autoplay: !stillPlease });

  mount.removeAttribute('hidden');
  if (fallback) {
    fallback.querySelector('video')?.pause();
    fallback.setAttribute('hidden', '');
  }
}
