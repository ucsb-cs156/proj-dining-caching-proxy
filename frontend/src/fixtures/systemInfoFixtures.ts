import type { SystemInfo } from "main/utils/systemInfo";

export const systemInfoFixtures = {
  showingNeither: {
    springH2ConsoleEnabled: false,
    showSwaggerUILink: false,
    oauthLogin: "/oauth2/authorization/google",
    sourceRepo: "https://github.com/ucsb-cs156/proj-dining-caching-proxy",
  },
  showingBoth: {
    springH2ConsoleEnabled: true,
    showSwaggerUILink: true,
    oauthLogin: "/oauth2/authorization/google",
    sourceRepo: "https://github.com/ucsb-cs156/proj-dining-caching-proxy",
  },
} satisfies Record<string, SystemInfo>;
