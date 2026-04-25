const { Given, When, Then } = require("@cucumber/cucumber");

Then("I tap on element with id {string}", async function (text) {
  const element = await this.driver.$(
    `android=new UiSelector().text("${text}")`,
  );
  await element.click();
});

Then("I see the text {string}", async function (text) {
  const element = await this.driver.$(
    `android=new UiSelector().textContains("${text}")`,
  );
  await element.waitForDisplayed({ timeout: 5000 });
  return element;
});

Then("I tap on coordinates {int} {int}", async function (x, y) {
  await this.driver.touchAction({ action: "tap", x: x, y: y });
});

Then("I take a screenshot", async function () {
  await this.driver.saveScreenshot(`reports/screenshot_${Date.now()}.png`);
});

Then("I tap on element with accessibility id {string}", async function (id) {
  const element = await this.driver.$(`~${id}`);
  await element.click();
});
