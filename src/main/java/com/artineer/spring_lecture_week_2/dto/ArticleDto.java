package com.artineer.spring_lecture_week_2.dto;

import lombok.Builder;
import lombok.Getter;

public class ArticleDto {
    @Getter
    public static class ReqPost{
        String title;
        String content;
    }
    @Builder
    public static class Res{
        private String id;
        private String title;
        private String content;
    }
}
