package com.namtg.egovernment.entity.test;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StudentEntity {
    private String name;
    private int age;

    public StudentEntity(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
