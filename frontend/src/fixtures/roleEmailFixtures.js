const roleEmailFixtures = {
  oneItem: {
    email: "hostmanager1@example.com",
  },
  threeItems: [
    {
      email: "hostmanager1@example.com",
    },
    {
      email: "admin1@example.com",
    },
    {
      email: "hostmanager2@example.com",
    },
  ],
  threeItemsWithIsInAdminEmailField: [
    {
      email: "hostmanager1@example.com",
      isInAdminEmails: true,
    },
    {
      email: "admin1@example.com",
      isInAdminEmails: false,
    },
    {
      email: "hostmanager2@example.com",
      isInAdminEmails: false,
    },
  ],
};

export { roleEmailFixtures };
