package com.javacademy.new_york_times.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageDto<T> {
    private List<T> list;
    private Integer totalPage;
    private Integer currentPage;
    private Integer maxPageSize;
    private Integer sizeCurrentPage;

}
