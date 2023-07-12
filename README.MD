# Twitter Server
A pure-java implementation of Twitter's server using a REST API and no additional frameworks.

## About the project
This project has been done as the final project of the advanced programming course in [<b>AUT</b>](https://www.topuniversities.com/universities/amirkabir-university-technology).
As one of the projects' constraints, use of any backened-related frameworks was not allowed (e.g. Spring Boot etc). 

## Technologies
For addressing various needs in the Twitter server such as the database, serialization etc, different technologies were researched carefully and then chosen:

<div style="margin-left: 20px">

### Database
Regarding the relational nature of most of the saved data in the project, MySQL database was chosen as a robust and scalable SQL database that provides one with many feasible features. Some others databases were also investigated such as PostgreSQL and MongoDB but MySQL still seemed like a better fit for my use.
Querying the database was done using SQL queries only.

**used libraries:**
<div style="margin-left: 10px">
&bull; <a href="https://mvnrepository.com/artifact/mysql/mysql-connector-java/8.0.28">MySQL connector java v8.0.28</a>

&bull; <a href="https://mvnrepository.com/artifact/commons-dbutils/commons-dbutils/1.7">Apache Commons DbUtils v1.7</a>
</div>

### Serialization protocol
As one of the most robust and vastly used serialization protocols, JSON was used, but for larger data structures (photos, videos etc) Apache Commons IO was utilized as the serializer/deserializer.

**used libraries:**
<div style="margin-left: 10px">
&bull; <a href="https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core/2.14.2">Fasterxml jackson v2.14.2</a>

&bull; <a href="https://mvnrepository.com/artifact/com.google.code.gson/gson/2.10.1">Google gson v2.10.1</a>

</div>

### Request handling
A REST API was designed and implemented from scratch using HttpServer library (built-in to java). For method validation, request format validation, sending responses etc no frameworks were used due to project constraints.

**used libraries:**
<div style="margin-left: 10px">
&bull; <a href="https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html">HttpServer</a>
</div>

</div>


## Design and Architecture

To address various neeeds in the project and also achieving important software engineering principles such as separation of concerns, encapsulation, maintainability, scalability, etc, many different design patterns were used: 

&bull; As the main communication way between the server and the client, [**RSET API**](https://www.digitalocean.com/community/tutorials/restful-web-services-tutorial-java) was used. After the needed methods were identified, related actions were put in the well-defined hierarchy of the API which separated them according to their responsibilities.
An ER schema of the implemented design is shown below:
![ER schema](https://github.com/farbodbj/Twitter-Server/blob/master/architecture.png)

&bull; The main design in the server part was [**The Layered architecture**](https://www.oreilly.com/library/view/software-architecture-patterns/9781491971437/ch01.html) as it could best help encapsulate different components and separate the concerns of accessing data (both on disk and database) from concerns of request handling and controlling.

&bull; Although the [**MVVM design pattern**](https://www.techtarget.com/whatis/definition/Model-View-ViewModel) is mostly related to the client part, but in the whole project (server and client together) the so-called Model components were kept in a module named Commons and were only used as data entities with no buissiness related logic in them.
The UML schema for the commons module can be seen below:
![Commons module UML](https://github.com/farbodbj/Twitter-Server/blob/master/Commons.png)

&bull; [**DAO**](https://www.digitalocean.com/community/tutorials/dao-design-pattern) design pattern was effectively applied in the database part to encapsulate the database access logic from other parts. each table in the database has a corresponding class which is the only class that can add, remove or modify the data in that table.
The database schematic can be seen below:
![database schema](https://github.com/farbodbj/Twitter-Server/blob/master/database%20schema.png)

## Implementation details

### Authorization
<div style="margin-left: 20px">
As suggested in the project description, JSON Web Tokens (known as Jwt) were used for authorizing almost every action a user could take. the access token which were kept in the client would be sent in the header of every request and the server would let the user take the desired action after validating that token.
</div>

### Error handling
<div style="margin-left: 20px">
Mnay different custom exceptions had been defined in the project which are raised in related situation and they are most often handled by sending the client the appropriate Http status code and message related to that error.
</div>

### Handling files
<div style="margin-left: 20px">
As the clients could demand sending and recieving different types of video and pictures for various purposes, the need of being able to effectively was handled in the project. In almost all cases, the uniqueness of ids (e.g tweet_id, user_id etc) was used along with a prefix/suffix which showed the kind of file (e.g profile picture, tweet attachment etc).
</div>

## Future enhancements
&bull; Using a logger for better monitoring the server

&bull; Keeping hashed passwords instead of actuall passwords in the database

&bull; Encrypting the Http connection between the server and the client

&bull; Putting server configurations in a separate file instead of hardcoding.
