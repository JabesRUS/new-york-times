package com.javacademy.new_york_times.web;

import com.javacademy.new_york_times.dto.NewsDto;
import com.javacademy.new_york_times.dto.PageDto;
import com.javacademy.new_york_times.service.NewsService;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.NoSuchElementException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NewsControllerTest {
    @Autowired
    private NewsService newsService;
    private RequestSpecification requestSpecification = new RequestSpecBuilder()
            .setBasePath("/news")
            .setContentType(ContentType.JSON)
            .log(LogDetail.ALL)
            .build();

    private ResponseSpecification responseSpecification = new ResponseSpecBuilder()
            .log(LogDetail.ALL)
            .build();

    @Test
    @DisplayName("Получить новость по id")
    public void getNewsByIdSuccess() {
        NewsDto newsDto = RestAssured.given(requestSpecification)
                .get("/1")
                .then()
                .spec(responseSpecification)
                .statusCode(200)
                .extract()
                .body()
                .as(NewsDto.class);

        Assertions.assertEquals(1, newsDto.getNumber());
        Assertions.assertEquals("News #1", newsDto.getTitle());
        Assertions.assertEquals("Today is Groundhog Day #1", newsDto.getText());
        Assertions.assertEquals("Molodyko Yuri", newsDto.getAuthor());
    }

    @Test
    @DisplayName("Получить новости со страницы")
    public void getAllNewsByPageSuccess() {
        PageDto<NewsDto> pageNumber = RestAssured.given(requestSpecification)
                .queryParam("pageNumber", 0)
                .get()
                .then()
                .spec(responseSpecification)
                .statusCode(200)
                .extract()
                .body()
                .as(new TypeRef<>() {
                });

        ArrayList<NewsDto> listNewsByPage = new ArrayList<>(pageNumber.getList());
        NewsDto news2 = listNewsByPage.get(1);

        Assertions.assertEquals(2, news2.getNumber());
        Assertions.assertEquals("News #2", news2.getTitle());
        Assertions.assertEquals("Today is Groundhog Day #2", news2.getText());
        Assertions.assertEquals("Molodyko Yuri", news2.getAuthor());
        Assertions.assertEquals(100, pageNumber.getTotalPage());
        Assertions.assertEquals(0, pageNumber.getCurrentPage());
        Assertions.assertEquals(10, pageNumber.getMaxPageSize());
        Assertions.assertEquals(10, pageNumber.getSizeCurrentPage());
    }

    @Test
    @DisplayName("Удалить новость по номеру")
    public void deleteNewsByNumber() {
        boolean result = RestAssured.given(requestSpecification)
                .delete("/1")
                .then()
                .spec(responseSpecification)
                .statusCode(200)
                .extract()
                .body()
                .as(Boolean.class);

        Assertions.assertTrue(result);
        Assertions.assertThrows(NoSuchElementException.class, () -> newsService.findByNumber(1),
                "Новость не удалена из БД");
    }

    @Test
    @DisplayName("Создание новости")
    public void createNewsSuccess() {
        NewsDto news = NewsDto.builder()
                .title("Заголовок новости")
                .text("Текс Новости")
                .author("Автор новости")
                .build();
        int countAllNewsBeforeCreate = newsService.findAll().size();

        RestAssured.given(requestSpecification)
                .body(news)
                .post()
                .then()
                .spec(responseSpecification)
                .defaultParser(Parser.JSON)
                .statusCode(201);

        int countAllNewsAfterCreate = newsService.findAll().size();
        NewsDto createdNews = newsService.findByNumber(countAllNewsAfterCreate);

        Assertions.assertNotEquals(countAllNewsBeforeCreate, countAllNewsAfterCreate);
        Assertions.assertEquals(countAllNewsAfterCreate, createdNews.getNumber());
        Assertions.assertEquals("Заголовок новости", createdNews.getTitle());
        Assertions.assertEquals("Текс Новости", createdNews.getText());
        Assertions.assertEquals("Автор новости", createdNews.getAuthor());
    }

    @Test
    @DisplayName("Создание новости")
    public void updateNewsSuccess() {
        NewsDto updateNews = NewsDto.builder()
                .number(1)
                .title("Новый заголовок")
                .text("Новый текст")
                .author("Новый автор")
                .build();

        RestAssured.given(requestSpecification)
                .body(updateNews)
                .patch("/1")
                .then()
                .spec(responseSpecification)
                .statusCode(202);

        NewsDto updatedNews = newsService.findByNumber(1);

        Assertions.assertEquals(updateNews, updatedNews);
    }

    @Test
    @DisplayName("Получение текста новости")
    public void getTextFieldSuccess() {

        RestAssured.given(requestSpecification)
                .get("1/field/text")
                .then()
                .spec(responseSpecification)
                .defaultParser(Parser.TEXT)
                .statusCode(200)
                .body(Matchers.equalTo("Today is Groundhog Day #1"));
    }

    @Test
    @DisplayName("Получение автора новости")
    public void getAuthorFieldSuccess() {

        RestAssured.given(requestSpecification)
                .get("1/field/author")
                .then()
                .spec(responseSpecification)
                .defaultParser(Parser.TEXT)
                .statusCode(200)
                .body(Matchers.equalTo("Molodyko Yuri"));
    }


}
