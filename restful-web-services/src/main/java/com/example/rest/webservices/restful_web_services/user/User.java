package com.example.rest.webservices.restful_web_services.user;

import java.time.LocalDate;


public class User {
    private Integer id;
    private String name;
    private LocalDate birthDate;
    public User(Integer id, String name, LocalDate birthDate) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
    }
    public Integer getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public LocalDate getBirthDate() {
        return birthDate;
    }
    
   
}
