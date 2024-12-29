package com.javacademy.new_york_times.controller;

import com.javacademy.new_york_times.dto.NewsDto;
import com.javacademy.new_york_times.dto.PageDto;
import com.javacademy.new_york_times.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Сделать 7 операций внутри контроллера.
 * 1. Создание новости. Должно чистить кэш.
 * 2. Удаление новости по id. Должно чистить кэш.
 * 3. Получение новости по id. Должно быть закэшировано.
 * 4. Получение всех новостей (новости должны отдаваться порциями по 10 штук). Должно быть закэшировано.
 * 5. Обновление новости по id. Должно чистить кэш.
 * 6. Получение текста конкретной новости.
 * 7. Получение автора конкретной новости.
 *
 */
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
@Tag(name = "NY Times", description = "Контроллер по обработке новостей")
public class NewsController {
    public static final int PAGE_SIZE = 10;
    private final NewsService newsService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    @CacheEvict(value = "news")
    @Operation(description = "Создание новости")
    public void create(@RequestBody NewsDto dto) {
        newsService.save(dto);
    }

    @DeleteMapping("/{number}")
    @CacheEvict(value = "news")
    @Operation(description = "Удаление новости по number")
    public boolean deleteByNumber(@PathVariable  Integer number) {
       return newsService.deleteByNumber(number);
    }

    @GetMapping("/{number}")
    @Cacheable(value = "news")
    @Operation(description = "Получить новость по number")
    public ResponseEntity<?> getNewsById(@PathVariable Integer number) {
        return ResponseEntity.ok(newsService.findByNumber(number));
    }

    @GetMapping()
    @Cacheable(value = "all_news")
    @Operation(description = "Получить новости на указанной странице")
    public ResponseEntity<?> getAllNewsByPage(@RequestParam Integer pageNumber) {
        return ResponseEntity.ok(getNewsDtoPage(pageNumber));
    }

    @PatchMapping("/{number}")
    @CacheEvict(value = "news", key = "#number")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    @Operation(description = "Обновление новости")
    public void update(@PathVariable Integer number,
                       @RequestBody NewsDto dto) {
        newsService.update(dto);
    }

    @GetMapping("/{number}/field/{fieldName}")
    @Operation(description = "Получение указанного поля в указанной новости")
    public String getFieldById(@PathVariable Integer number,
                               @PathVariable String fieldName) {
        if (fieldName.equals("text")) {
            return newsService.getNewsText(number);
        }
        return newsService.getNewsAuthor(number);
    }

    private PageDto<NewsDto> getNewsDtoPage(int pageNumber) {
        List<NewsDto> listNews = newsService.findAll();
        List<NewsDto> listNewsPagin = listNews.stream()
                .skip(pageNumber * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .toList();

        Integer countPage = listNews.size() / PAGE_SIZE;
        Integer currentPage = pageNumber;
        Integer maxPageSize = PAGE_SIZE;
        Integer sizeCurrentPage = listNewsPagin.size();

        return new PageDto<>(listNewsPagin, countPage, currentPage, maxPageSize, sizeCurrentPage);
    }

}
