# Mandrel
[![Join the chat at https://gitter.im/Treydone/mandrel](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Treydone/mandrel?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://api.travis-ci.org/Treydone/mandrel.svg?branch=dev)](https://travis-ci.org/Treydone/mandrel)
[![Download](https://api.bintray.com/packages/treydone/maven/mandrel/images/download.svg)](https://bintray.com/treydone/maven/mandrel/_latestVersion)

# Introduction

Mandrel is a distributed mining engine built for the cloud. Features include:

* Distributed and Highly Available mining Engine.
* Powerful and customizable
* HTTP RESTful API
* Open Source under the Apache License, version 2 ("ALv2")

Mandrel is designed as a whole product, with the idea of bringing a complete mining engine solution that runs out of the box. It is designed for performance and scalability. Event if it has been tested on small and on medium-sized cluster (~30 nodes), Mandrel can scale on cloud-sized cluster without any effort.

Mandrel can be used to build various mining jobs:

- Web crawlers: make the web your in-house database by turning web pages into data
- SEO analytics: test web pages and links for valid syntax and structure, find broken links and 404 pages
- Monitor sites to see when their structure or contents change
- Build a special-purpose search index, intranet/extranet indexation...
- Maintain mirror sites for popular Web sites
- Search for copyright infringements: find duplicate of content

# Getting Started


## System Requirements

Mandrel works on Linux, Mac and Windows. All you need is Java 8.


## Installation

* Download and unzip the Mandrel official distribution
* Run @bin/standalone@
* Go to @http://localhost:8080/@.
* Enjoy

OR

* Checkout the code
* Build it via a "mvn package -DskipTests"
* Run @bin/standalone@
* Go to @http://localhost:8080/@.
* Enjoy

# Where to go from here? 

Refer to the Mandrel online "documentation":http://treydone.github.io/mandrel/ .


# License

This software is licensed under the Apache License, version 2 ("ALv2"), quoted below.

Copyright 2009-2015 Mandrel

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
