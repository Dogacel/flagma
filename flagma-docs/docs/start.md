---
sidebar_position: 2
---

# Getting Started

**Flagma** is a self-hosted service. But for learning purposes, we have a public website that you can use to start learning about **Flagma**.

### What you'll need

Nothing! Well, we have some client libraries for your convience but **Flagma** is implemented as an HTTP service so you can basically take a look at [API documentation of Flagma](#) and try it by yourself.

## Concepts

Flagma is built on top of two concepts:

- **Flag**: A feature flag that contains a value.
- **Project**: A collection of flags.

There is no additional complexity such as environments, toggles, rollouts, we built **Flagma** to be simple. You can have separate project for your applications or different environments such as test and production. Toggles, rollouts and other structures can be represented as a Flag. A flag value can be one of:

- **Number**
- **String**
- **Boolean**
- **JSON**

Using those 4 types of flags, you are free to represent whatever you want. For example a toggle can be a simple boolean. A rollout can be a number between 0 and 1. For more complex examples such as [Targeted Values](#) please visit the doc page.

## Create a Git Repository

To start using Flagma, you should initialize a Git repository. Start the Flagma instance and Central Dogma instances, configure your Git repository mirroring settings and you are ready to start.

## Create a Project

Create a project by initializing a folder under `projects/` such as `fooProject` and add an empty `flags.json` file with a content of empty JSON object `{}`.

## Create a Flag

Add your first flag by modifying `projects/fooProject/flags.json` to be,

```json
{
  "banner_enabled": {
    "name": "banner_enabled",
    "id": "fooProject_banner_enabled",
    "tags": ["banner", "ui"],
    "description": "Whether the banner on homepage is enabled or not",
    "type": "BOOLEAN",
    "value": false
  }
}
```

## Using Flags

To use a flag, you can send get requests on UI.

```javascript
const showBanner = (
  await fetch("https://flagma.dev/flags/fooProject/banner_enabled")
).value;

if (showBanner) {
  return <h1>This banner is new!</h1>;
}

return <div />;
```
