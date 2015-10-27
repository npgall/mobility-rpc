# Mobility-RPC Release Notes #

### Version 1.1.0 - 2013-06-20 ###
  * Added ClassLoader.getResource() support to access resources on a remote machine, which are located on the classpath of the client machine. Allows configuration files etc. to be uploaded automatically ([issue 8](https://code.google.com/p/mobility-rpc/issues/detail?id=8))
  * Minor fixes to system tray icon support on Linux

### Version 1.0.0 - 2012-11-18 ###
  * Added support for running Mobility-RPC as a standalone server, simply by double-clicking the jar
  * Version 1.0.0 because the API is stable

### Version 0.5.5 - 2012-05-16 ###
  * Fixed backward/forward compatibility issue in protocol
  * Updated pom.xml maven-license-plugin to validate open source licenses in deploy phase rather than verify phase, to allow "mvn install" to run and install locally for testing or experimental builds

### Version 0.5.4 - 2012-04-06 ###
  * Updated the project's `pom.xml` with additional metadata required for deploying to Maven Central
  * GPG-signed and deployed version 0.5.4 to Sonatype public repo, and requested sync with Maven Central

### Version 0.5.2 - 2012-03-03 ###
  * Added support to the protocol for transferring arbitrary classpath resources. Objects sent to remote machines can now load properties/configuration files etc. via usual methods (e.g. `this.getClass().getResourceAsStream(String name)`) and these will be uploaded transparently
  * Added `MobilitySession.execute(String, Runnable)` and `execute(String, Callable)`, convenience methods to send objects to a `String` machine name using default port

### Version 0.5.1 - 2012-02-20 ###
  * First release version
  * Built with maven-shade-plugin, includes all dependencies
  * Jar can be used as a library in an application, or run as a `StandaloneMobilityServer`: java -jar mobility-rpc-0.5.1-all.jar