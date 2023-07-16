---
sidebar_position: 1
---

# What is Flagma?

**Flagma** is an open-source, low latency, high throughput Feature Flag manager built on top of Git using [Central Dogma](https://line.github.io/centraldogma/).

### What is a Feature Flag?

Feature flags are small variables used to control the availability and behavior of specific features or functionalities of an application at runtime **without needing to re-deploy or write code**. Some benefits of using feature flags are,

- **Progressive Rollouts**: Feature flags enable gradual feature rollouts by activating them for a subset of users or specific environments. This approach helps mitigate risks and allows for testing in a controlled manner before a full release.
- **A/B Testing**: By enabling feature flags for different user groups, developers can compare the impact and effectiveness of different variations or implementations of a feature.
- **Hotfixes and Rollbacks**: If a critical bug or issue arises, feature flags can be used to quickly disable the affected feature without deploying new code. This facilitates rapid response and minimizes downtime.
- **Continuous Integration and Delivery**: Feature flags decouple feature releases from code deployments, enabling continuous integration and delivery practices. Developers can merge code changes to the main branch and activate or deactivate features independently.
- **Feature Customization**: Feature flags allow developers to personalize or customize features for specific users or user groups. By enabling or disabling flags based on user attributes or preferences, developers can tailor the experience to different segments.
