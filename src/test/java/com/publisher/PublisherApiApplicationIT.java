package com.publisher;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.Credentials.basicAuthenticationCredentials;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.publisher.dto.ArticleEditDto;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PublisherApiApplicationIT {
	@Autowired
	ApplicationContext context;

	private WebTestClient client;

	@Before
	public void setUp() throws Exception {
		this.client = WebTestClient.bindToApplicationContext(context).build().mutate().filter(basicAuthentication())
				.build();
	}

	@Test
	public void shouldCreateReadUpdateList() throws Exception {
		ArticleEditDto editDto = ArticleEditDto.builder().text("text").authors(new String[] { "auth1" })
				.keywords(new String[] { "keyword" }).publishDate(new Date()).header("header1")
				.shortDescription("shortDesc").build();

		// create
		URI location = this.client.post().uri("/api/article").attributes(userCredentials())
				.body(BodyInserters.fromObject(editDto)).exchange().expectStatus().isCreated().expectHeader()
				.valueMatches("location", "/api/article/.*").expectBody(Void.class).returnResult().getResponseHeaders()
				.getLocation();

		// read
		this.client.get().uri(location).attributes(userCredentials()).exchange().expectStatus().isOk().expectBody()
				.jsonPath("$.text", "text");

		// patch
		editDto.setText("text1");
		this.client.patch().uri(location).attributes(userCredentials()).body(BodyInserters.fromObject(editDto))
				.exchange().expectStatus().isOk().expectBody(Void.class);

		// list by author
		this.client.get().uri("/api/article?author=auth1").accept(MediaType.TEXT_EVENT_STREAM).exchange().expectStatus()
				.isOk().expectBody().jsonPath("$[0].header").isEqualTo("header1").jsonPath("$[0].text").isEqualTo(null);

		// list by keyword
		this.client.get().uri("/api/article?keyword=keyword").accept(MediaType.TEXT_EVENT_STREAM).exchange()
				.expectStatus().isOk().expectBody().jsonPath("$[0].header").isEqualTo("header1").jsonPath("$[0].text")
				.isEqualTo(null);

		// list by publishDate
		this.client.get().uri("/api/article?from=2000-12-03T00:00:00&to=2050-12-03T00:00:00")
				.accept(MediaType.TEXT_EVENT_STREAM).exchange().expectStatus().isOk().expectBody()
				.jsonPath("$[0].header").isEqualTo("header1").jsonPath("$[0].text").isEqualTo(null);

	}

	@Test
	public void shouldBeUnAuthorizedToCreateArticle() throws Exception {
		ArticleEditDto editDto = ArticleEditDto.builder().text("text").authors(new String[] { "auth1" })
				.keywords(new String[] { "keyword" }).publishDate(new Date()).header("header1")
				.shortDescription("shortDesc").build();

		// create
		this.client.post().uri("/api/article").attributes(invalidCredentials()).body(BodyInserters.fromObject(editDto))
				.exchange().expectStatus().isUnauthorized();
	}

	private Consumer<Map<String, Object>> userCredentials() {
		return basicAuthenticationCredentials("user1", "password");
	}

	private Consumer<Map<String, Object>> invalidCredentials() {
		return basicAuthenticationCredentials("user1", "xxx");
	}

}
