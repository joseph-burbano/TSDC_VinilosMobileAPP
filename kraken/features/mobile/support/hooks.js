const { After, Before } = require("@cucumber/cucumber");
const { AndroidClient } = require("kraken-node");

Before(async function () {
  this.deviceClient = new AndroidClient(
    this.device.id,
    this.apkPath,
    this.apkPackage,
    this.apkLaunchActivity,
    {
      "appium:skipDeviceInitialization": true,
      "appium:dontStopAppOnReset": true,
      "appium:noReset": true,
      "appium:ignoreHiddenApiPolicyError": true,
      "appium:uiautomator2ServerInstallTimeout": 120000,
      "appium:skipServerInstallation": false,
    },
    this.userId,
  );
  this.driver = await this.deviceClient.startKrakenForUserId(this.userId);
  await this.driver.pause(2000);
});

After(async function () {
  await this.deviceClient.stopKrakenForUserId(this.userId);
});
