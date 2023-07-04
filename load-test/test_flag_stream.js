import http from "k6/http";

export const options = {
  scenarios: {
    concurrent_10: {
      executor: "shared-iterations",
      vus: 10,
      iterations: 100,
      startTime: "0s",
    },
  },
};

export default function () {
  const responseCallback = http.expectedStatuses(408);

  const params = {
    headers: {
      "Content-Type": "text/event-stream",
    },
    timeout: "10s",
    responseCallback: responseCallback,
  };

  http.get("http://127.0.0.1:9000/stream/flags/test", params);
}
