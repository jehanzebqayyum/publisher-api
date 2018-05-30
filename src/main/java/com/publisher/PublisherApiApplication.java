package com.publisher;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.fasterxml.jackson.core.JsonParseException;
import com.publisher.handler.ArticleHandler;

@SpringBootApplication
@EnableReactiveMongoRepositories
@EnableWebFluxSecurity
public class PublisherApiApplication {
	private final Logger logger = LoggerFactory.getLogger(PublisherApiApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(PublisherApiApplication.class, args);
	}

	@Bean
	public RouterFunction<ServerResponse> routes(ArticleHandler articleHandler) {
		return nest(path("/api/article"),
				route(GET("/{id}").and(accept(APPLICATION_JSON)), articleHandler::getArticle)
						.andRoute(GET("/").and(accept(TEXT_EVENT_STREAM)), articleHandler::listArticles)
						.andRoute(DELETE("/{id}"), articleHandler::deleteArticle)
						.andRoute(POST("/").and(contentType(APPLICATION_JSON)), articleHandler::createArticle)
						.andRoute(PATCH("/{id}").and(contentType(APPLICATION_JSON)), articleHandler::updateArticle))
								.filter(errorHandler());
	}

	HandlerFilterFunction<ServerResponse, ServerResponse> errorHandler() {
		return (request, next) -> {
			return next.handle(request).onErrorResume(e -> {
				logger.error(e.getMessage(), e);
				if (e instanceof ConstraintViolationException) {
					return ServerResponse.badRequest().body(BodyInserters.fromObject(e.getMessage()));
				}
				if (e instanceof JsonParseException) {
					return ServerResponse.badRequest().build();
				}
				return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			});

		};
	}

	@Bean
	public ValidatingMongoEventListener validatingMongoEventListener(Validator validator) {
		return new ValidatingMongoEventListener(validator);
	}

	@Bean
	public LocalValidatorFactoryBean validator() {
		return new LocalValidatorFactoryBean();
	}

	@Bean
	SecurityWebFilterChain security(ServerHttpSecurity httpSecurity) {
		return httpSecurity.csrf().disable().authorizeExchange().pathMatchers(HttpMethod.POST).authenticated()
				.pathMatchers(HttpMethod.DELETE).authenticated().pathMatchers(HttpMethod.PATCH).authenticated().and()
				.httpBasic().and().authorizeExchange().anyExchange().permitAll().and().build();
	}

	@Bean
	MapReactiveUserDetailsService userDetailsService(PasswordEncoder encoder) {
		UserDetails user1 = User.withUsername("user1").password(encoder.encode("password")).roles("EDITOR").build();
		UserDetails user2 = User.withUsername("user2").password(encoder.encode("password")).roles("EDITOR").build();
		return new MapReactiveUserDetailsService(user1, user2);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
