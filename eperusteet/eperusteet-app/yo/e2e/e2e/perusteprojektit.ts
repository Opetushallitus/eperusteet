const assertFailedHandles = (str: string) => expect(str.startsWith("{{") && str.endsWith("}}")).toBeFalsy();

describe("Sivuston näkymä", () => {
    it("etusivu latautuu", () => {
        browser.get("http://localhost:9000/");
        browser.driver.manage().window().setSize(1024, 768);
        browser.waitForAngular();
    });
});
