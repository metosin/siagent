// @ts-check
import { test, expect } from "@playwright/test";

test.beforeEach(async ({ page }) => {
  await page.goto("/");
});

test("Complex keywords and strings", async ({ page }) => {
  const element1 = page
    .getByTestId("hiccup")
    .getByTestId("element-1")
    .locator(":nth-child(2)");
  await expect(element1).toHaveJSProperty("tagName", "SPAN");
  await expect(element1).toHaveId("my-id");
  await expect(element1).toHaveClass("my-class1 my-class2 my-class3 my-class4");
  await expect(element1).toHaveText("Some text");

  const element2 = page
    .getByTestId("hiccup")
    .getByTestId("element-2")
    .locator(":nth-child(2)");
  await expect(element2).toHaveJSProperty("tagName", "SPAN");
  await expect(element2).toHaveId("my-id");
  await expect(element2).toHaveClass("my-class1 my-class2 my-class3 my-class4");
  await expect(element2).toHaveText("Some text");
});

test("Default tag element is a div", async ({ page }) => {
  const element3 = page.getByTestId("hiccup").getByTestId("element-3");

  const element3a = element3.locator(":nth-child(2)");
  await expect(element3a).toHaveJSProperty("tagName", "DIV");
  await expect(element3a).toHaveId("some-id");
  await expect(element3a).toHaveText("This is in a div.");

  const element3b = element3.locator(":nth-child(3)");
  await expect(element3b).toHaveJSProperty("tagName", "DIV");
  await expect(element3b).toHaveClass("some-class");
  await expect(element3b).toHaveText("This too.");
});

test("Inline style", async ({ page }) => {
  const inlineStyle = page.getByTestId("hiccup").getByTestId("inline-style");
  await expect(inlineStyle).toHaveAttribute("style", "color: pink;");
});

test("Sequences in hiccup are inlined", async ({ page }) => {
  const sequence = page.getByTestId("hiccup").getByTestId("sequence");
  await expect(sequence).toMatchAriaSnapshot(`
    - article:
      - heading "Sequences are inlined" [level=3]
      - list:
        - listitem: "^{:key 0} [:li 0]"
        - listitem: "^{:key 1} [:li 1]"
        - listitem: "[:li single element in the middle]"
        - listitem: "[:li {:key 0} 0]"
        - listitem: "[:li {:key 1} 1]"
  `);
});

test("React fragment", async ({ page }) => {
  const fragment = page.getByTestId("hiccup").getByTestId("fragment");
  await expect(fragment).toMatchAriaSnapshot(`
    - article:
      - heading "React fragment" [level=3]
      - list:
        - listitem: element 0a
        - listitem: element 0b
        - listitem: element 1a
        - listitem: element 1b
  `);
});
