package com.radhe.microservices.microservice_y.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.radhe.microservices.microservice_y.bean.Limit;

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
