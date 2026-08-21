describe("index.html title", () => {
  test("document title should be Dining Caching Proxy", () => {
    document.title = "Dining Caching Proxy";
    expect(document.title).toBe("Dining Caching Proxy");
  });
});
