package com.publisher.model;

import java.util.Date;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article {
	@Id
	private String id;

	@NotEmpty
	@Size(max = 500)
	private String header;

	@NotEmpty
	@Size(max = 1000)
	private String shortDescription;

	@NotEmpty
	@Size(max = 5000)
	private String text;

	@NotNull
	@Indexed
	private Date publishDate;

	@NotEmpty
	@Size(max = 10)
	@Indexed
	private String[] authors;

	@NotEmpty
	@Size(max = 50)
	@Indexed
	private String[] keywords;

	@CreatedBy
	private String user;

}
