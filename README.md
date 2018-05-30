# Publisher API

Publisher backend API which allows to
* create, update, delete article by an editor
* read article
* find articles by author, keyword or period

## Endpoints

* To create article 

	POST: /api/article/

Content-Type: application/json. Requires Basic Authentication.

```
{
"header": "aheader",
"shortDescription": "ashortDescription",
"text": "atext",
"authors": ["author1", "author2"],
"keywords": ["keyword1"],
"publishDate": "2012-04-23T18:25:43.511Z"
}
```

Returns Location URI of the new resource.

* To update article 

	PATCH: /api/article/{id}

Content-Type: application/json. Requires Basic Authentication. Only the user which created the article can update it.

```
{
"header": "aheader",
"shortDescription": "ashortDescription",
"text": "atext",
"authors": ["author1", "author2"],
"keywords": ["keyword1"],
"publishDate": "2012-04-23T18:25:43.511Z"
}
```

* To delete article 

	DELETE: /api/article/{id}

Requires Basic Authentication. Only the user which created the article can delete it.

* To read article 

	GET: /api/article/{id}

Accept: application/json. Anonymous access.

* List articles by author, keyword or period

	GET: /api/article/?author={author}
	GET: /api/article/?keyword={keyword}
	GET: /api/article/?from={e.g.2007-12-03T10:15:30}&to={e.g.2007-12-03T10:15:30}

Accept: text/event-stream. Anonymous access.

## Getting Started


### Prerequisites

* JDK 8
* Maven 3.x

### Running

	mvn spring-boot:run

Application is available at: http://localhost:8080

## Running the tests

	mvn verify
	
## Built With
* Spring Boot 2
* Mongo

#### Spring Boot 2 & Webflux: 
* Spring Boot makes microservices development easy with its embedded container and framework support for end-to-end development. 
* Spring webflux enables reactive programming which suits this application as it is I/O intensive.
* Spring webflux also allows to scale better, by limiting the no. of threads required for I/O.
* Spring webflux by default uses Netty which is asynchronous and non-blocking.

#### MongoDB
* A NoSql db which allows evolution of schema with minimum effort.
* Spring reactive has support for Mongo which makes the whole stream i.e. from Web to Mongo  reactive.

## Future Enhancements
* Enable Paging support in list endpoints
* Enable Gzip compression for all endpoints
* Implement better documentation with Swagger
* Implement HATEOS
* Implement monitoring and statistics with Spring Actuator
* Enhance security to use JWT
* Implement Caching and ETags

