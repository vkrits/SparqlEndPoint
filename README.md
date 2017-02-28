# README #

This is a Spring MVC (SpringBootApplication) + AngularJS project that implements a clinet for a Blazegraph Endpoint Repository.

### What is this repository for? ###

* This is a Sparql Endpointc clinet for the VRE4EIC Project
* Version 0.3

### How do I get set up? ###

#### Requirements ####

A Blazegraph running instance.

#### In eclipse (or any other IDE) ####

Simply run it as Java Application from the main method (Application class)

#### From CMD or UNIX Terminal ####

Go to the location where you have placed the fat jar and use:

*java -jar SparqlEndPoint-0.0.1-SNAPSHOT.jar*

To change Jetty default port use:

*java -jar SparqlEndPoint-0.0.1-SNAPSHOT.jar --server.port=8090*

### Web Access ###

To access the resolver from the web (what else), use the URLs:

Reslover:

* <span>http</span>://<IP_WHERE_CONTAINER_IS_DEPLOYED>:<PORT>/<PREFIX>/<THE_REST_OF_THE_URI>

Queries UI:

* <span>http</span>://<IP_WHERE_CONTAINER_IS_DEPLOYED>:<PORT>

i.e.

Reslover:

* <span>http</span>://83.212.97.61:8090/prefix/E1.CRM_Entity

Queries UI:

* <span>http</span>://83.212.97.61:8090/query

### SparQL Endpoint configuration (two files) ###

config.property file:

triplestore.url = <span>http</span>://<IP>:9999/blazegraph (The IP of the server where Blazegraph is deployed
triplestore.namespace = <NAMESPACE_NAME> (the namespace in Blazegraph)
rdf.base = <span>http</span>://<IP>:<CONTAINER_PORT> (the IP of the Server where the container is deployed
rdf.afterbase = <PREFIX> (can be whatever you like)

app.js file:
$routeProvider
   ...
   .when('/<PREFIX>/:uriToResolve',{
	...
   })
   ...

URL should be structured as:

* <span>http</span>://<IP>:<CONTAINER_PORT>/<PREFIX>/<THE_REST_OF_THE_URI>

##### Example #####

config.property file:
triplestore.url = <span>http</span>://83.212.97.61:9999/blazegraph
triplestore.namespace = test
rdf.base = <span>http</span>://83.212.97.61:8090
rdf.afterbase = localtest

app.js file::
$routeProvider
   ...
   .when('/localtest/:uriToResolve',{
	...
   })
   ...

URL should be structured as:

* <span>http</span>://83.212.97.61:8090/localtest/E37.Mark

### Who do I talk to? ###

* Vangelis Kritsotakis