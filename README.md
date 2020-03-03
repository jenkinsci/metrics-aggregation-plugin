# Jenkins Metrics Aggregation Plugin

[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/metrics-aggregation.svg?label=latest%20version)](https://plugins.jenkins.io/metrics-aggregation)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/metrics-aggregation.svg)](https://plugins.jenkins.io/metrics-aggregation)
[![Jenkins Version](https://img.shields.io/badge/Jenkins-2.150.3-green.svg)](https://jenkins.io/download/)
![JDK8](https://img.shields.io/badge/jdk-8-yellow.svg?label=min.%20JDK)
[![License: MIT](https://img.shields.io/badge/license-MIT-yellow.svg)](https://opensource.org/licenses/MIT) 
[![Jenkins](https://ci.jenkins.io/job/Plugins/job/metrics-aggregation-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/metrics-aggregation-plugin/job/master/)
[![GitHub Actions](https://github.com/jenkinsci/metrics-aggregation-plugin/workflows/GitHub%20Actions/badge.svg)](https://github.com/jenkinsci/metrics-aggregation-plugin/actions)
[![Codacy](https://api.codacy.com/project/badge/Grade/5afc3b117acd4819b0ddeaf794114495)](https://www.codacy.com/manual/andipabst/metrics-aggregation-plugin?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jenkinsci/metrics-aggregation-plugin&amp;utm_campaign=Badge_Grade)

<!--[![Codecov](https://img.shields.io/codecov/c/github/jenkinsci/metrics-aggregation-plugin/master.svg)](https://codecov.io/gh/jenkinsci/metrics-aggregation-plugin/branch/master)-->

The Jenkins Metrics Aggregation plugin provides a unified view on metrics from various sources.

## Usage
There are two aspects on using this plugin:

1. To report metrics from Java code (via [PMD](https://pmd.github.io/)), add the post-build-step provided by this plugin (freestyle- and pipeline-builds are supported).
2. When the metrics-aggregation-plugin is installed, there is a metrics view available on every build, showing the metrics collected from the available sources for this build.

## Supported metrics sources
The metrics-aggregation-plugin supports collecting metrics from PMD. See Usage above for more details on how to use this source.

Currently, metrics from the following plugins are used:
- [warnings-ng-plugin](https://github.com/jenkinsci/warnings-ng-plugin) (This also includes metrics from the [forensics-api-plugin](https://github.com/jenkinsci/forensics-api-plugin))
- [code-coverage-api-plugin](https://github.com/jenkinsci/code-coverage-api-plugin)

If you are a Jenkins plugin maintainer and would like to to add your plugin, all you need to do is to implement the extension point [`MetricsProviderFactory`](plugin/src/main/java/io/jenkins/plugins/metrics/extension/MetricsProviderFactory.java). Please feel free to open a pull request for adding your plugin to the above list.

If there is anything you would like to add to the extension point, please open a ticket in the [Jenkins JIRA](https://issues.jenkins-ci.org/).

-------------------------------------------------------
This plugin was created as part of a masters thesis at University of Applied Sciences, Munich.

All source code is licensed under the MIT license.
