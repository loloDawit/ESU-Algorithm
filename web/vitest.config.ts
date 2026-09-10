import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    // The engine tests need no DOM; the rendering test does, and a real DOM
    // is what catches a control that quietly stopped being built.
    environment: 'happy-dom',
    // browser/ belongs to Playwright. Vitest's default pattern matches
    // .spec.ts anywhere, and it picked those up and failed on them.
    include: ['test/**/*.test.ts'],
  },
});
