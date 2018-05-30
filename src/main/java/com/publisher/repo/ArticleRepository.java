package com.publisher.repo;

import java.util.Date;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;

import com.publisher.model.Article;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ArticleRepository extends ReactiveSortingRepository<Article, String> {
	@Query(value = "{'authors':  ?0}", fields = "{text : 0}")
	public Flux<Article> findByAuthor(String author);

	@Query(value = "{'keywords':  ?0}", fields = "{text : 0}")
	public Flux<Article> findByKeyword(String keyword);

	@Query(value = "{'publishDate':  {'$gt' : ?0, '$lt' : ?1}}", fields = "{text : 0}")
	public Flux<Article> findByPublishDateBetween(Date from, Date to);

	@Query(value = "{'id':  ?0}", fields = "{user : 1}")
	public Mono<Article> findArticleUserById(String id);
}
