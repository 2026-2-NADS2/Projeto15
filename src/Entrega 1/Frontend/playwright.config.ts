import { defineConfig } from "@playwright/test";
export default defineConfig({
  testDir: "./tests",
  timeout: 60000,
  use: {
    actionTimeout: 10000,
    navigationTimeout: 20000,
    baseURL: process.env.KFKA_TEST_FRONTEND || "http://localhost:5173",
    trace: "retain-on-failure",
    launchOptions: { executablePath: process.env.KFKA_TEST_BROWSER },
  },
  workers: 1,
  reporter: "list",
});
