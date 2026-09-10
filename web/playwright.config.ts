import { defineConfig, devices } from '@playwright/test';

/**
 * Browser tests for the demo, against the site as it is published.
 *
 * These exist for what the unit tests cannot see. Those run against a
 * simulated DOM with no layout engine and no real stylesheets, so a panel can
 * be the wrong size, text can be invisible against its background, and the
 * tree can sit off screen without a single assertion noticing. Every one of
 * those has happened.
 */
export default defineConfig({
  testDir: 'browser',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? 'github' : 'list',

  use: {
    baseURL: 'http://127.0.0.1:4173',
    trace: 'on-first-retry',
  },

  // The real page, built and served exactly as it is deployed.
  webServer: {
    command: 'npm run build && npx http-server ../docs -p 4173 --silent',
    url: 'http://127.0.0.1:4173',
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
  },

  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
});
