package com.publisher.handler;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.publisher.dto.ArticleEditDto;
import com.publisher.model.Article;
import com.publisher.repo.ArticleRepository;

import reactor.core.publisher.Mono;

@Component
public class ArticleHandler {
	@Autowired
	private ArticleRepository articleRepo;

	public Mono<ServerResponse> createArticle(ServerRequest request) {
		Mono<ServerResponse> unprocessed = ServerResponse.unprocessableEntity().build(),
				forbidden = ServerResponse.status(HttpStatus.FORBIDDEN).build();

		return request.principal().flatMap(p -> request.bodyToMono(ArticleEditDto.class).map(ar -> {
			Article a = ar.toArticle();
			a.setUser(p.getName());
			return a;
		}).flatMap(articleRepo::save).map(Article::getId)
				.flatMap(id -> ServerResponse.created(URI.create("/api/article/" + id)).build())
				.switchIfEmpty(unprocessed)).switchIfEmpty(forbidden);
	}

	public Mono<ServerResponse> updateArticle(ServerRequest request) {
		String id = request.pathVariable("id");

		Mono<ServerResponse> unprocessed = ServerResponse.unprocessableEntity().build(),
				forbidden = ServerResponse.status(HttpStatus.FORBIDDEN).build(),
				notfound = ServerResponse.notFound().build(), ok = ServerResponse.ok().build();

		return articleRepo.findArticleUserById(id)
				.flatMap(found -> request.principal().filter(p -> found.getUser().equals(p.getName()))
						.flatMap(p -> request.bodyToMono(ArticleEditDto.class).map(ar -> {
							Article a = ar.toArticle();
							a.setId(id);
							return a;
						}).flatMap(articleRepo::save).flatMap(s -> ok).switchIfEmpty(unprocessed))
						.switchIfEmpty(forbidden))
				.switchIfEmpty(notfound);
	}

	public Mono<ServerResponse> getArticle(ServerRequest request) {
		String id = request.pathVariable("id");
		Mono<ServerResponse> notfound = ServerResponse.notFound().build();

		return articleRepo.findById(id).flatMap(found -> ServerResponse.ok().body(fromObject(found)))
				.switchIfEmpty(notfound);
	}

	public Mono<ServerResponse> deleteArticle(ServerRequest request) {
		String id = request.pathVariable("id");

		Mono<ServerResponse> unprocessed = ServerResponse.unprocessableEntity().build(),
				forbidden = ServerResponse.status(HttpStatus.FORBIDDEN).build(),
				notfound = ServerResponse.notFound().build(), ok = ServerResponse.ok().build();

		return articleRepo.findArticleUserById(id)
				.flatMap(found -> request.principal().filter(p -> found.getUser().equals(p.getName()))
						.flatMap(p -> articleRepo.deleteById(id).flatMap(v -> ok).switchIfEmpty(unprocessed))
						.switchIfEmpty(forbidden))
				.switchIfEmpty(notfound);
	}

	public Mono<ServerResponse> listArticles(ServerRequest request) {
		return listArticlesByAuthor(request).orElseGet(() -> listArticlesByKeyword(request)
				.orElseGet(() -> listArticlesInPeriod(request).orElseGet(() -> ServerResponse.noContent().build())));
	}

	private Optional<Mono<ServerResponse>> listArticlesByAuthor(ServerRequest request) {
		return request.queryParam("author")
				.map(author -> ServerResponse.ok().body(articleRepo.findByAuthor(author), Article.class));
	}

	private Optional<Mono<ServerResponse>> listArticlesByKeyword(ServerRequest request) {
		return request.queryParam("keyword")
				.map(key -> ServerResponse.ok().body(articleRepo.findByKeyword(key), Article.class));
	}

	private Optional<Mono<ServerResponse>> listArticlesInPeriod(ServerRequest request) {
		try {
			Optional<Date> fromDate = request.queryParam("from")
					.map(f -> Date.from(LocalDateTime.parse(f).atZone(ZoneId.systemDefault()).toInstant()));
			Optional<Date> toDate = request.queryParam("to")
					.map(t -> Date.from(LocalDateTime.parse(t).atZone(ZoneId.systemDefault()).toInstant()));

			if (fromDate.isPresent() && toDate.isPresent()) {
				return Optional.of(ServerResponse.ok()
						.body(articleRepo.findByPublishDateBetween(fromDate.get(), toDate.get()), Article.class));
			}

		} catch (DateTimeParseException dtpe) {
			return Optional.of(ServerResponse.badRequest()
					.body(fromObject("Invalid date/time format. Use ISO_LOCAL_DATE_TIME e.g.2007-12-03T10:15:30 ")));
		}

		return Optional.empty();
	}

}
