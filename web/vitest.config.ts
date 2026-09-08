import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    // The engine tests need no DOM; the rendering test does, and a real DOM
    // is what catches a control that quietly stopped being built.
    environment: 'happy-dom',
  },
});
