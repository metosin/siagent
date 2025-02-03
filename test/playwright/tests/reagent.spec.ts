// @ts-check
import { test, expect } from "@playwright/test";

test.beforeEach(async ({ page }) => {
  await page.goto("/");
});

test("reactivity", async ({ page }) => {
  const article = page.getByTestId("reagent").getByTestId("reactivity");
  const aButton = article.getByRole("button").nth(0);
  const bButton = article.getByRole("button").nth(1);

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 0
      - text: b = 0
      - text: atom-a + value-b = 0
  `);

  await aButton.click();
  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 1
      - text: b = 0
      - text: atom-a + value-b = 1
  `);

  await bButton.click();
  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 1
      - text: b = 1
      - text: atom-a + value-b = 2
  `);

  await aButton.click();
  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 2
      - text: b = 1
      - text: atom-a + value-b = 3
  `);
});

const testCounter3 = async article => {
  const aButton = article.getByRole("button").nth(0);
  const bButton = article.getByRole("button").nth(1);
  const cButton = article.getByRole("button").nth(2);

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 0
      - text: b = 0
      - text: c = 0
      - text: value-a + atom-b + atom-c = 0
  `);

  await aButton.click();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 1
      - text: b = 0
      - text: c = 0
      - text: value-a + atom-b + atom-c = 1
  `);

  await bButton.click();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 1
      - text: b = 1
      - text: c = 0
      - text: value-a + atom-b + atom-c = 2
  `);

  await cButton.click();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 1
      - text: b = 1
      - text: c = 1
      - text: value-a + atom-b + atom-c = 3
  `);

  await aButton.click();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 2
      - text: b = 1
      - text: c = 1
      - text: value-a + atom-b + atom-c = 4
  `);

  await bButton.click();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 2
      - text: b = 2
      - text: c = 1
      - text: value-a + atom-b + atom-c = 5
  `);

  await cButton.click();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 2
      - text: b = 2
      - text: c = 2
      - text: value-a + atom-b + atom-c = 6
  `);
};

test("fn-in-fn", async ({ page }) => {
  const article = page.getByTestId("reagent").getByTestId("fn-in-fn");
  await testCounter3(article);
});

test("with-let", async ({ page }) => {
  const article = page.getByTestId("reagent").getByTestId("fn-in-fn");
  await testCounter3(article);
});

test("with-let-finally", async ({ page }) => {
  const article = page.getByTestId("reagent").getByTestId("with-let-finally");
  const aButton = article.getByRole("button").nth(0);
  const checkbox = article.getByRole("checkbox");

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 0
      - text: The content was finalized 0 times.
  `);
  await expect(checkbox).not.toBeChecked();

  await checkbox.click();
  await expect(checkbox).toBeChecked();

  const bButton = article.getByRole("button").nth(1);
  const cButton = article.getByRole("button").nth(2);

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 0
      - text: The content was finalized 0 times.
      - text: b = 0
      - text: c = 0
      - text: value-a + atom-b + atom-c = 0
  `);

  await aButton.click();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 1
      - text: The content was finalized 0 times.
      - text: b = 0
      - text: c = 0
      - text: value-a + atom-b + atom-c = 1
  `);

  await bButton.click();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 1
      - text: The content was finalized 0 times.
      - text: b = 1
      - text: c = 0
      - text: value-a + atom-b + atom-c = 2
  `);

  await cButton.click();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 1
      - text: The content was finalized 0 times.
      - text: b = 1
      - text: c = 1
      - text: value-a + atom-b + atom-c = 3
  `);

  await checkbox.click();
  await expect(checkbox).not.toBeChecked();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 1
      - text: The content was finalized 1 times.
  `);

  await checkbox.click();
  await expect(checkbox).toBeChecked();

  await aButton.click();
  await bButton.click();
  await cButton.click();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 2
      - text: The content was finalized 1 times.
      - text: b = 1
      - text: c = 1
      - text: value-a + atom-b + atom-c = 4
  `);

  await checkbox.click();
  await expect(checkbox).not.toBeChecked();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 2
      - text: The content was finalized 2 times.
  `);
});

test("controlled-input-element", async ({ page }) => {
  const article = page
    .getByTestId("interop")
    .getByTestId("controlled-input-element");
  const input = page.getByRole("textbox");

  await input.fill("hello,");
  await input.pressSequentially(" world!");

  await expect(input).toHaveValue("hello, world!");
});
