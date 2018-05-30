package com.publisher.dto;

import java.util.Date;

import com.publisher.model.Article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleEditDto {
	private String header;
	private String shortDescription;
	private String text;
	private String[] authors;
	private String[] keywords;
	private Date publishDate;

	public Article toArticle() {
		return Article.builder().authors(authors).keywords(keywords).header(header).text(text).publishDate(publishDate)
				.shortDescription(shortDescription).build();
	}
}
