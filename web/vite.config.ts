import { defineConfig } from 'vite';

/**
 * Builds the demo into a single JS file and a single CSS file, which the
 * project page loads. No hashing: the page references them by name and Pages
 * serves them with its own caching.
 */
export default defineConfig({
  base: './',
  build: {
    outDir: 'dist',
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
