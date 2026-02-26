package com.spring.app.ih.dining.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class DiningDTO {
    private Long id;
    private String name;
    private String address;
    private String tel;
}