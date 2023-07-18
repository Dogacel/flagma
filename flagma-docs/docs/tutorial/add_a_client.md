---
sidebar_position: 5
---

# Add to Your Client

Now, as you created your first **Flag**, we need someone to use that! Feature flags can be consumed by anyone, ranging from servers to mobile devices, even including IoT devices.

There are **2** main ways to connect your clients to use feature flags:

- Request a flag each time.
- Subscribe to flag updates.

:::tip

**Flagma** currently doesn't have client libraries. Because we are HTTP based, implementing a library for your favorite language/framework is very easy. Please **contribute** by opening a PR for a new client library. For more info on **implementing client libraries**, visit **Advanced/Developing Client Libraries** section.

:::

## Flag Request

Every time you need to access a flag, you can send a single HTTP request to the **Flagma** server.

:::info

This is the recommended way for applications that can operate with **a slight latency** such as mobile or web applications. This will cause the **least load on your server** and **least network usage** for your client.

:::

```typescript
let showNewDashboard = (await fetch(
  "https://<YOUR_SERVER_URL>/flags/financeApp/show_new_dashboard"
))?.value ?: false;
```

This implementation will cause some latecy. Try to use async callbacks as much as you can. For example, if you are using _React_, you can try the following implementation with _hooks_.

```typescript
const [showNewDashboard, setShowNewDashboard] = useState(false); // The default value for flag.

useEffect(async () => {
    setShowNewDashboard(
        (await fetch("https://<YOUR_SERVER_URL>/flags/financeApp/show_new_dashboard"))?.value ?: false
    );
}, [setShowNewDashboard]);
```

## Subscriptions

Sometimes the client can request a flag **thousands of times per second**. In those scenarios, it makes sense to cache the flag. But a cache would mean mis-evaluation of a flag. Thus we use **Server-Sent Events** to create subscriptions for clients.

:::info

This is the recommended way for applications that need **super-low latency** or **very high throughput** feature flags.

:::

```typescript
const flags = {}
const source = new EventSource("https://<YOUR_SERVER_URL>/stream/flags/financeApp)
source.onmessage = e => {
  flags[e.data.id] = e.data.value;
}
```
