---
sidebar_position: 3
---

# Creating a Project

Each _Flagma_ application needs a _Project_ to start with. You can create a project by adding a new folder in your _Git_ repository and adding an empty `flags.json` file in it.

```json title="projects/new_project/flags.json"
{}
```

Alternatively, you can use API calls to create projects.

```bash
curl -X POST http://<YOUR_FLAGMA_SERVER>/projects/new_project
```
