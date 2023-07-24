package com.example.sgm.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Article implements Serializable {
    private Long id;
    private String title;
    private String name;
    private String author;
    private String content;
}
