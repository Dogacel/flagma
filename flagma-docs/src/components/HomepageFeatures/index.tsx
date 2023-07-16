import React from "react";
import clsx from "clsx";
import styles from "./styles.module.css";

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<"svg">>;
  description: JSX.Element;
};

const FeatureList: FeatureItem[] = [
  {
    title: "Highly Scalable, Easy to Use",
    Svg: require("@site/static/img/undraw_docusaurus_mountain.svg").default,
    description: (
      <>
        Flagma is a feature flag manager that operates over HTTP, making it
        incredibly simple to set up and utilize. You can easily interact with
        Flagma using either the traditional HTTP/1.1 or HTTP/2 protocols. For
        optimized performance, Flagma leverages the Server-Sent Events
        framework, ensuring minimal latency and maximizing throughput for
        seamless communication between clients and the feature flag manager.
      </>
    ),
  },
  {
    title: "Integrated with Git",
    Svg: require("@site/static/img/undraw_docusaurus_tree.svg").default,
    description: (
      <>
        Flagma is built on Git, ensuring that your feature flags are stored
        alongside your source code. This integration allows for seamless control
        of flag changes through your regular code review process. With Flagma,
        you have complete visibility into the history of flag modifications,
        enabling you to track deployments and easily perform rollbacks. As
        Flagma is fully based on Git, you have the freedom to manage your
        feature flags in a way that aligns with your preferences and workflows.
      </>
    ),
  },
  {
    title: "Open-source Powered by Armeria & Central Dogma",
    Svg: require("@site/static/img/undraw_docusaurus_react.svg").default,
    description: (
      <>
        Flagma is an open-source project powered by the Armeria framework and
        built on top of Central Dogma. Unlike its competitors, Flagma is
        completely free and designed to be a straightforward feature flag
        manager. It possesses all the essential capabilities needed for feature
        flag management. We strongly believe in the individuality of developers
        and wholeheartedly encourage them to utilize Flagma to unleash their
        creativity, without any limitations imposed.
      </>
    ),
  },
];

function Feature({ title, Svg, description }: FeatureItem) {
  return (
    <div className={clsx("col col--4")}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
