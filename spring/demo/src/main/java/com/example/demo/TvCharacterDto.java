package com.example.demo;

import jakarta.validation.constraints.NotNull;

import javax.validation.constraints.NotNull;

public class TvCharacterDto {
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getTvSeriesId() {
        return tvSeriesId;
    }

    public void setTvSeriesId(int tvSeriesId) {
        this.tvSeriesId = tvSeriesId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private Integer id;
    private int tvSeriesId;
    @NotNull
    private String name;
}
