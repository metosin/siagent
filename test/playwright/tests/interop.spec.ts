// @ts-check
import { test, expect } from "@playwright/test";

test.beforeEach(async ({ page }) => {
  await page.goto("/");
});

test("reagent-with-hooks", async ({ page }) => {
  const article = page.getByTestId("interop").getByTestId("reagent-with-hooks");
  const aButton = article.getByRole("button").nth(0);
  const b1Button = article.getByRole("button").nth(1);
  const c1Button = article.getByRole("button").nth(2);
  const b2Button = article.getByRole("button").nth(3);
  const c2Button = article.getByRole("button").nth(4);

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 0
      - text: atom b = 0
      - text: state c = 0
      - text: value-a + atom-b + state-c = 0
      - text: atom b = 0
      - text: state c = 0
      - text: value-a + atom-b + state-c = 0
  `);

  await aButton.click();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 1
      - text: atom b = 0
      - text: state c = 0
      - text: value-a + atom-b + state-c = 1
      - text: atom b = 0
      - text: state c = 0
      - text: value-a + atom-b + state-c = 1
  `);

  await b1Button.click();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 1
      - text: atom b = 1
      - text: state c = 0
      - text: value-a + atom-b + state-c = 2
      - text: atom b = 0
      - text: state c = 0
      - text: value-a + atom-b + state-c = 1
  `);

  await c1Button.click();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 1
      - text: atom b = 1
      - text: state c = 1
      - text: value-a + atom-b + state-c = 3
      - text: atom b = 0
      - text: state c = 0
      - text: value-a + atom-b + state-c = 1
  `);

  await aButton.click();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 2
      - text: atom b = 1
      - text: state c = 1
      - text: value-a + atom-b + state-c = 4
      - text: atom b = 0
      - text: state c = 0
      - text: value-a + atom-b + state-c = 2
  `);

  await b2Button.click();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 2
      - text: atom b = 1
      - text: state c = 1
      - text: value-a + atom-b + state-c = 4
      - text: atom b = 1
      - text: state c = 0
      - text: value-a + atom-b + state-c = 3
  `);

  await c2Button.click();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 2
      - text: atom b = 1
      - text: state c = 1
      - text: value-a + atom-b + state-c = 4
      - text: atom b = 1
      - text: state c = 1
      - text: value-a + atom-b + state-c = 4
  `);
});

test("calling-react", async ({ page }) => {
  const article = page.getByTestId("interop").getByTestId("calling-react");

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: My title
      - list:
        - listitem: Child 1
        - listitem: Child 2
      - text: My title
      - list:
        - listitem: Child 1
        - listitem: Child 2
  `);
});

test("r/use-reactive", async ({ page }) => {
  const article = page.getByTestId("interop").getByTestId("r/use-reactive");
  const aButton = article.getByRole("button");

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 0
      - text: a + a = 0
  `);

  await aButton.click();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 1
      - text: a + a = 2
  `);

  await aButton.click();

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: a = 2
      - text: a + a = 4
  `);
});

test("r/reactify-component", async ({ page }) => {
  const article = page
    .getByTestId("interop")
    .getByTestId("r/reactify-component");

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: My title
      - list:
        - listitem: Child 1
        - listitem: Child 2
  `);
});

test("r/adapt-react-class", async ({ page }) => {
  const article = page
    .getByTestId("interop")
    .getByTestId("r/adapt-react-class");

  await expect(article).toMatchAriaSnapshot(`
    - article:
      - text: My title
      - list:
        - listitem: Child 1
        - listitem: Child 2
  `);
});
