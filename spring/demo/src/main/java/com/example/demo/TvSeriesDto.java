package com.example.demo;

import java.util.Date;

public class TvSeriesDto {
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    public Date getOriginRelease() {
        return originRelease;
    }

    public void setOriginRelease(Date originRelease) {
        this.originRelease = originRelease;
    }

    private Date originRelease;

    public TvSeriesDto() {

    }

    public TvSeriesDto(int id, String name, Date originRelease) {
        this.id = id;
        this.name = name;
        this.originRelease = originRelease;
    }
}
