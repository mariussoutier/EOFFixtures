EOFFixtures.framework
=====================

Overview
--------

This is a small framework that allows using YAML files to populate WebObjects/EOF based applications.
 
I found this really useful for the following use cases:

* Development
* Initial users for newly deployed applications
* Marketing/demo deployments
 
A reference of the YAML markup language can be found here: [YAML Reference](http://www.yaml.org/).

Inspired by the fixtures mechanism in the [Play! framework](http://www.playframework.org/).
 
Usage
-----
* Add EOFFixtures to your WebObjects/WOnder application's build path (as a WebObjects framework in Eclipse).
* Create a YAML file (default name is fixtures.yaml, you can change this via the EOFFixtures.fileName property) in your Resources folder.
    (For an example see the FixturesClient project.)
* In your application's Properties, set "EOFFixtures.loadInitialData" to true or manually call Fixtures.load().
* Pro-tip: Using different Properties.username files, you can define different fixtures for different environments.
* To build this as a framework, you have to set ${scala.home} to your Scala install dir
* Use the custom timestamp tags to ease the creation of NSTimestamp
 * !now for the current date (aka `new NSTimestamp()`)
 * !ts to create a calculated timestamp, in the format `<signed-number> <days|weeks|months>`, for example `!ts +3 days`, `!ts -3 weeks`, `!ts -1 month`.
 
Used libaries
-------------
* [Scala](http://www.scala-lang.org/)
* [Scalaj-collection](https://github.com/scalaj/scalaj-collection)
* [Snake YAML](http://code.google.com/p/snakeyaml/)
* [Project WOnder](http://projectwonder.blogspot.com/)
 
 
**Enjoy!**
