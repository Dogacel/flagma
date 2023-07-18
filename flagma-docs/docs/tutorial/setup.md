---
sidebar_position: 2
---

# Setup

To start using **Flagma** in your projects, you only need an instance of **Flagma Server**. Even using a Git repository is optional because _Flagma_ already stores a git repository internally. But note that if you don't have a Git repository you can't use multiple instances of _Flagma_.

We are shipping **Docker images** for _Flagma_. You can start using by running the docker image **dogacel/flagma-server**.

:::tip

If you don't want to deal with deploying _Flagma_, you can use our **Managed** model with a cost.

:::

## Git

Even though having a Git repository is _optional_, it is **highly recommended**. To use a remote Git repository for your application, you should set the environment variable `GIT_URL`.

```bash
export GIT_URL="git+ssh://git.example.com/foo.git"
```

### GitHub

You should have an access key with necessary read/write permissions to flag repository to make _Flagma_ work with your GitHub repository.

```bash
export GITHUB_ACCESS_TOKEN="..."
```

We encourage developers to make changes to the feature flags using _Pull Requests_. But you might realize there are no safeguards that prevent you from commiting some invalid JSON. Thus we have created the [Flagma Format GitHub Action](#). You can add this github action as a workflow to your repository.

```yml
name: Flagma JSON Format Check

on:
  push:
    branches:
      - main
  pull_request:

permissions:
  contents: read
  pull-requests: read

jobs:
  check-format:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v3

      - name: Check Flagma Format
        uses: dogacel-flagma@v1
```

## Local Setup

We suggest using [docker-compose](https://docs.docker.com/compose/) for local development.

```yml
services:
  flagma=server:
    image: dogacel/flagma-server
    volumes:
      - ./data:/var/lib/data
    environment:
      - GIT_URL="git+ssh://git.example.com/foo.git"
      - GITHUB_ACCESS_TOKEN="..."
    port:
      - "9000:9000"
```

## Deploying to Cloud

Most of the time, you want to deploy **Flagma** to cloud to make it **production-ready**. Because we are shipping _docker-compose_ files and _docker_ images of _Flagma_, you can use your Cloud provider's solution for deploying docker images.

### AWS

For more info,

https://aws.amazon.com/blogs/containers/deploy-applications-on-amazon-ecs-using-docker-compose/

### GCP

For more info,

https://cloud.google.com/container-optimized-os/docs/how-to/run-container-instance
