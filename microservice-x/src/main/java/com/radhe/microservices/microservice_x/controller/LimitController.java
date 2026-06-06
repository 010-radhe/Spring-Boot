package com.radhe.microservices.microservice_x.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.radhe.microservices.microservice_x.bean.Limit;

@RestController
public class LimitController {
    private final Limit limit;
    public LimitController(Limit limit) {
        this.limit = limit;
    }

    @GetMapping("/limits-config")
    public Limit getLimits(){
        return limit;
    }
}
