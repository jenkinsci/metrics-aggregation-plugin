# Jenkins Metrics Aggregation Plugin

[![License: MIT](https://img.shields.io/badge/license-MIT-yellow.svg)](https://opensource.org/licenses/MIT) 
[![Jenkins](https://ci.jenkins.io/job/Plugins/job/metrics-aggregation-plugin/job/main/badge/icon)](https://ci.jenkins.io/job/Plugins/job/metrics-aggregation-plugin/job/main/)
[![GitHub Actions](https://github.com/jenkinsci/metrics-aggregation-plugin/workflows/GitHub%20Actions/badge.svg)](https://github.com/jenkinsci/metrics-aggregation-plugin/actions)

The Jenkins Metrics Aggregation plugin provides a unified view on metrics from various sources.

## Usage

When the metrics-aggregation-plugin is installed, there is a metrics view available on every build, showing the metrics collected from the available sources for this build.

## Supported metrics sources

The metrics-aggregation-plugin supports collecting metrics from all plugins that implement the extension point. 

Currently, metrics from the following plugins are available:
- [warnings-ng-plugin](https://github.com/jenkinsci/warnings-ng-plugin) (This also includes metrics from the [forensics-api-plugin](https://github.com/jenkinsci/forensics-api-plugin))

If you are a Jenkins plugin maintainer and would like to add your plugin, all you need to do is to implement the extension point [`MetricsProviderFactory`](src/main/java/io/jenkins/plugins/metrics/extension/MetricsProviderFactory.java). 
Please feel free to open a pull request for adding your plugin to the above list.

If there is anything you would like to add to the extension point, please open a ticket in the [Jenkins JIRA](https://issues.jenkins-ci.org/).

-------------------------------------------------------
This plugin was created as part of a master thesis at the University of Applied Sciences, Munich.

All source code is licensed under the MIT license.
