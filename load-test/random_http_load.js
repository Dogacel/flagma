import http from "k6/http";
import { check } from "k6";
import { randomItem } from "https://jslib.k6.io/k6-utils/1.2.0/index.js";

function repeat(stages, times) {
  let final_stages = [];

  for (let i = 0; i < times; i++) {
    final_stages = final_stages.concat(stages);
  }
  return final_stages;
}

export const options = {
  scenarios: {
    all_projects: {
      executor: "constant-vus",
      vus: 3,
      duration: "3m",
      startTime: "0s",
      exec: "allProjects",
    },
    all_flags: {
      startTime: "0s",
      executor: "ramping-vus",
      startvus: 0,
      stages: repeat(
        [
          { duration: "35s", target: 5 },
          { duration: "10s", target: 12 },
        ],
        4
      ),
      exec: "allFlags",
    },
    single_flag: {
      startTime: "0s",
      executor: "ramping-vus",
      startvus: 0,
      stages: repeat(
        [
          { duration: "1m", target: 20 },
          { duration: "1m", target: 0 },
          { duration: "1m", target: 10 },
        ],
        1
      ),
      exec: "singleFlag",
    },
  },
};

const flagNames = [
  "test_bool",
  "test_int",
  "test_double",
  "test_string",
  "test_empty_json",
  "test_simple_json",
  "test_nested_json",
];

export function allProjects() {
  let result = http.get("http://127.0.0.1:9000/projects");
  check(result, { "Status is 200": (r) => r.status === 200 });
}

export function allFlags() {
  let result = http.get("http://127.0.0.1:9000/flags/testProject");
  check(result, { "Status is 200": (r) => r.status === 200 });
}

export function singleFlag() {
  let result = http.get(
    "http://127.0.0.1:9000/flags/testProject/" + randomItem(flagNames)
  );
  check(result, { "Status is 200": (r) => r.status === 200 });
}
