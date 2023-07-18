---
sidebar_position: 4
---

# Creating Your First Flag

You can commit changes to your project JSON file directly to create a new flag.

```json title="projects/new_project/flags.json"
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

There are 4 types of flags:

- **Boolean**: `{ "type": "BOOLEAN", "value": false }`
- **Number**: `{ "type": "NUMBER", "value": 3.14 }`
- **String**: `{ "type": "STRING", "value": "Hello World!" }`
- **JSON**: `{ "type": "JSON", "value": { "foo": "bar" } }`

Alternatively you can use API calls to create flags.

```bash

curl -X POST \
-H 'Content-type: application/json' \
https://<YOUR_FLAGMA_SERVER>/flags/new_project/banner_enabled \
-d '{"name": "banner_enabled", "tags": [], "type": "BOOLEAN", "value": false}'

```
