# prime_number_server
Server which returns a stream of prime numbers through gRPC channel

## Architecture

Since parallelism, streaming and potential scalability are the most important factors for this solution, I've decided to use Akka actors
and streaming solution. There is a plugin for gRPC compatible with Akka http. It's nice since we can extend the server to
support REST API as well.

### 1) Connecting to the prime number server

In order to connect to the server, the other service needs to implement the gRPC client and also import the
[protobuf](https://github.com/alar17/proxy_server/blob/main/src/main/protobuf/rpc.proto) definitions of the data model and the service. Please have a look at the [proxy_server](https://github.com/alar17/proxy_server) project

### 2) Running the server locally

In order to run the server locally, we need to install sbt and run the following commands:

* sbt: Runs sbt
* clean: Cleans the project (We don't need to do this every time)
* eclipse: Creates a java project (We need to do it only once)
* compile: Compiles the project
* test: Runs the tests of the project to make sure that everything is fine (Not needed for just running)
* run: Runs the server
