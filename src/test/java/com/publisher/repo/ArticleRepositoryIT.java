package com.publisher.repo;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.ConstraintViolationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.publisher.model.Article;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ArticleRepositoryIT {
	@Autowired
	private ArticleRepository articleRepo;
	
	private final SimpleDateFormat sd = new SimpleDateFormat("ddMMyyHHmmss");
	
	@Test(expected = ConstraintViolationException.class)
	public void shouldNotCreateArticleBecauseOfValidationFailure() {
		articleRepo.save(Article.builder().build()).block();
	}

	@Before
	public void setup() throws ParseException {
		articleRepo.deleteAll().block();

		Article a1 = Article.builder().text("text1").authors(new String[] { "author1" })
				.keywords(new String[] { "keyword1" }).header("header1").shortDescription("shortDescription1")
				.publishDate(sd.parse("010118000000")).build();

		Article a2 = Article.builder().text("text2").authors(new String[] { "author2" })
				.keywords(new String[] { "keyword1" }).header("header2").shortDescription("shortDescription2")
				.publishDate(sd.parse("020118000000")).build();

		Article a3 = Article.builder().text("text3").authors(new String[] { "author3" })
				.keywords(new String[] { "keyword3" }).header("header3").shortDescription("shortDescription3")
				.publishDate(sd.parse("020118000001")).build();

		Article a4 = Article.builder().text("text4").authors(new String[] { "author1" })
				.keywords(new String[] { "keyword4" }).header("header4").shortDescription("shortDescription4")
				.publishDate(sd.parse("030118000000")).build();

		articleRepo.saveAll(Arrays.asList(a1, a2, a3, a4)).blockLast();
	}

	@Test
	public void shouldFindArticlesByAuthor() {
		List<Article> articles = articleRepo.findByAuthor("author1").collectList().block();
		assertThat(articles.size()).isEqualTo(2);

		Article a1 = articles.stream().filter(a -> "header1".equals(a.getHeader())).findFirst().get();
		assertThat(a1.getText()).isNull();
	}

	@Test
	public void shouldFindArticlesByKeyword() {
		List<Article> articles = articleRepo.findByKeyword("keyword1").collectList().block();
		assertThat(articles.size()).isEqualTo(2);

		Article a1 = articles.stream().filter(a -> "header1".equals(a.getHeader())).findFirst().get();
		assertThat(a1.getText()).isNull();
	}

	@Test
	public void shouldFindArticlesInPeriod() throws ParseException {
		List<Article> articles = articleRepo
				.findByPublishDateBetween(sd.parse("010118000000"), sd.parse("030118000000")).collectList().block();
		assertThat(articles.size()).isEqualTo(2);

		Article a1 = articles.stream().filter(a -> "header2".equals(a.getHeader())).findFirst().get();
		assertThat(a1.getText()).isNull();
	}

}
