import { chromium } from '@playwright/test';
import { mkdir, readdir, rename, rm } from 'node:fs/promises';
import { join } from 'node:path';

/**
 * Records the demo for the README and the project page.
 *
 * The asset it replaces was a hand-made screen recording of the desktop app,
 * which went out of date the moment the tree layout changed and nobody
 * noticed for weeks. This drives the real published page, so re-running it is
 * all that keeping the picture honest takes.
 *
 *   npm run demo:record
 */

/** Long enough to read what changed, short enough not to drag. */
const BEAT = 900;

const OUT = 'recording';
/** The frame is the demo and nothing else; see isolate() below. */
const SIZE = { width: 1120, height: 720 };

/**
 * Strip the page down to the demo.
 *
 * The demo lives well below the fold under a sticky header, so recording the
 * page records mostly prose with the interesting part half off screen.
 */
const ISOLATE = `
  header, footer, section, .shot-note, .shot-caption { display: none !important; }
  body { margin: 0 !important; background: #fff !important; }
  .wrap { padding: 0 !important; max-width: none !important; }
  .hero { padding: 0 !important; }
  .cta, .hero h1, .hero .lede { display: none !important; }
  .shot { border: 0 !important; border-radius: 0 !important; box-shadow: none !important; }
  .demo-main { min-height: 430px !important; }
`;

async function main(): Promise<void> {
  await rm(OUT, { recursive: true, force: true });
  await mkdir(OUT, { recursive: true });

  const browser = await chromium.launch();
  const context = await browser.newContext({
    viewport: SIZE,
    deviceScaleFactor: 2,
    recordVideo: { dir: OUT, size: SIZE },
    // A cursor would wander about with nothing to explain it.
    reducedMotion: 'no-preference',
  });
  const page = await context.newPage();

  await page.goto(process.env.DEMO_URL ?? 'http://127.0.0.1:4173/');
  await page.locator('#demo-mount').waitFor({ state: 'visible' });
  await page.addStyleTag({ content: ISOLATE });
  await page.waitForTimeout(BEAT);

  // Kite: small enough that the tree fills the frame rather than being
  // scaled down to fit a wide one, and it still finds three shapes.
  await page.locator('.demo-graphs .demo-pill', { hasText: 'Kite' }).click();
  await page.waitForTimeout(BEAT);

  // Watch the search run.
  await page.locator('.demo-restart').click();
  await page.locator('.demo-play').click();
  // Let it run to the end on its own; the button returns to Play there.
  await page.locator('.demo-play').filter({ hasText: 'Play' })
    .waitFor({ timeout: 30_000 })
    .catch(() => undefined);
  await page.waitForTimeout(BEAT);

  // The shapes, and where they are.
  await page.locator('.demo-shape').first().click();
  await page.waitForTimeout(BEAT * 1.4);
  await page.locator('.demo-instance').nth(1).click();
  await page.waitForTimeout(BEAT * 1.6);

  await context.close();
  await browser.close();

  const [recorded] = (await readdir(OUT)).filter((name) => name.endsWith('.webm'));
  if (!recorded) {
    throw new Error('playwright recorded nothing');
  }
  await rename(join(OUT, recorded), join(OUT, 'demo.webm'));
  console.log(`recorded ${join(OUT, 'demo.webm')}`);
}

main().catch((failure) => {
  console.error(failure);
  process.exit(1);
});
