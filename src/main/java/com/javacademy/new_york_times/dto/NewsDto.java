package com.javacademy.new_york_times.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewsDto {
    private Integer number;
    private String title;
    private String text;
    private String author;
}
