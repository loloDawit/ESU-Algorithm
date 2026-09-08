import { defineConfig } from 'vite';

/**
 * Builds the demo straight into the site, so publishing is one directory and
 * needs no copying step. docs/demo/ is generated and not committed.
 *
 * One JS file and one CSS file, unhashed: the page references them by name.
 */
export default defineConfig({
  base: './',
  build: {
    outDir: '../docs/demo',
    emptyOutDir: true,
    lib: {
      entry: 'src/main.ts',
      formats: ['es'],
      fileName: () => 'demo.js',
    },
    cssCodeSplit: false,
    rollupOptions: {
      output: { assetFileNames: 'demo.[ext]' },
    },
  },
});
