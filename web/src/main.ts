/** Entry point: replace the still on the page with the running demo. */
import { Demo } from './app.js';

const mount = document.getElementById('demo-mount');
if (mount) {
  new Demo(mount);
  mount.removeAttribute('hidden');
  document.getElementById('demo-fallback')?.setAttribute('hidden', '');
}
