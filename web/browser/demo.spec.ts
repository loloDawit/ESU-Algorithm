import { expect, test, type Page } from '@playwright/test';

/**
 * The demo in a real browser.
 *
 * The unit tests run against a simulated DOM: no layout, no real stylesheets,
 * so they can say a node exists but never that it is visible, the right size,
 * or a readable colour against what is behind it. Every visual bug this
 * project has shipped was of that kind.
 */

/** Wait for the demo to replace the fallback clip. */
async function open(page: Page): Promise<void> {
  await page.goto('/');
  await expect(page.locator('#demo-mount')).toBeVisible();
  await expect(page.locator('.demo-tree')).toBeVisible();
}

/** Perceived brightness, for deciding whether text can be read on a ground. */
function luminance(colour: string): number {
  const [red, green, blue] = (colour.match(/[\d.]+/g) ?? ['0', '0', '0'])
    .slice(0, 3)
    .map(Number) as [number, number, number];
  return (0.299 * red + 0.587 * green + 0.114 * blue) / 255;
}

test('replaces the clip with the running demo', async ({ page }) => {
  await open(page);

  await expect(page.locator('#demo-fallback')).toBeHidden();
  await expect(page.locator('.demo-graph .g-vertex').first()).toBeVisible();
});

test('draws the tree inside the panel it is given', async ({ page }) => {
  await open(page);

  // The tree opened part way into empty canvas once, and nothing caught it.
  const panel = await page.locator('.demo-treewrap').boundingBox();
  const tree = await page.locator('.demo-tree').boundingBox();

  expect(tree).not.toBeNull();
  expect(panel).not.toBeNull();
  expect(tree!.width).toBeGreaterThan(100);
  expect(tree!.x).toBeGreaterThanOrEqual(panel!.x - 1);
  expect(tree!.x + tree!.width).toBeLessThanOrEqual(panel!.x + panel!.width + 1);
});

test('keeps every step-log line readable against its background', async ({ page }) => {
  await open(page);
  await page.locator('.demo-next').click();

  // A stylesheet once left this text dark on a dark ground, which no
  // assertion about the DOM could have seen.
  const log = page.locator('.demo-log');
  const background = await log.evaluate((el) => getComputedStyle(el).backgroundColor);
  const lines = log.locator('li');
  await expect(lines.first()).toBeVisible();

  for (const line of await lines.all()) {
    const colour = await line.evaluate((el) => getComputedStyle(el).color);
    expect(Math.abs(luminance(colour) - luminance(background)),
      `text ${colour} on ${background}`).toBeGreaterThan(0.2);
  }
});

test('fits the shapes it found without scrolling them out of sight', async ({ page }) => {
  await open(page);

  // The desktop panel was sized for three shapes and cramped five.
  const shapes = page.locator('.demo-shapes');
  await expect(shapes.locator('.demo-shape').first()).toBeVisible();

  const overflow = await shapes.evaluate(
    (el) => el.scrollHeight - el.clientHeight);
  expect(overflow).toBeLessThanOrEqual(1);
});

test('draws each shape with the edges it claims', async ({ page }) => {
  await open(page);
  await page.locator('.demo-graphs .demo-pill', { hasText: 'Cluster' }).click();

  const edgesOf: Record<string, number> = {
    path: 3, star: 3, cycle: 4, 'triangle and tail': 4, diamond: 5, clique: 6,
  };

  const chips = await page.locator('.demo-shape').all();
  expect(chips.length).toBeGreaterThan(1);

  for (const chip of chips) {
    const name = ((await chip.getAttribute('title')) ?? '').split(' — ')[0]!;
    await expect(chip.locator('.shape-drawing line')).toHaveCount(edgesOf[name]!);
  }
});

test('shows a chosen subgraph in the input graph', async ({ page }) => {
  await open(page);
  await page.locator('.demo-graphs .demo-pill', { hasText: 'Cluster' }).click();
  await page.locator('.demo-shape').first().click();

  const tile = page.locator('.demo-instance').first();
  await expect(tile).toBeVisible();
  const vertices = (await tile.locator('.demo-instance-label').innerText())
    .trim().split(/\s+/).length;

  await tile.click();

  await expect(page.locator('.demo-graph .g-chosen')).toHaveCount(vertices);
});

test('plays through the search when told to', async ({ page }) => {
  await open(page);
  const status = page.locator('.demo-status');
  await page.locator('.demo-restart').click();
  await expect(status).toContainText('Step 0 of');

  await page.locator('.demo-play').click();

  // A real click on a real button, advancing on a real timer.
  await expect(status).not.toContainText('Step 0 of', { timeout: 5000 });
  await page.locator('.demo-play').click();
  await expect(page.locator('.demo-play')).toHaveText('Play');
});

test('stays usable on a narrow window', async ({ page }) => {
  await page.setViewportSize({ width: 420, height: 900 });
  await open(page);

  // Nothing may spill sideways off the page.
  const overflow = await page.evaluate(
    () => document.documentElement.scrollWidth - document.documentElement.clientWidth);
  expect(overflow).toBeLessThanOrEqual(1);
  await expect(page.locator('.demo-play')).toBeVisible();
});
