# Mobility-RPC Technologies Used #

## Serialization of Java objects ##

  * [Kryo](http://code.google.com/p/kryo/) - An excellent object serialization library for the Java platform. Significantly outperforms Java's built-in serialization in speed and data sizes. Used to serialize Java object graphs supplied by the application for sending to remote machines, prior to encapsulating in protocol messages.

  * [Objenesis](http://code.google.com/p/objenesis/) - Used to instantiate objects during deserialization without calling their constructors. Mobility-RPC extends Kryo to use this for side-effect-free deserialization and compatibility with objects without no-arg constructors.

## Serialization for network protocol ##
  * [Google Protocol Buffers](http://code.google.com/p/protobuf/) - Used to serialize framework-internal Java objects representing protocol messages to binary, for sending over the network. Very efficient and allows protocol messages to be defined clearly in .proto files.

## Build tools ##
  * [Maven](http://maven.apache.org/) - Software project build and dependency management tool.