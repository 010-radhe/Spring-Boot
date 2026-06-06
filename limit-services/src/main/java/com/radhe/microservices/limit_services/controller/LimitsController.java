package com.radhe.microservices.limit_services.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.radhe.microservices.limit_services.bean.Limit;

@RestController
public class LimitsController {

	private final Limit limit;

	public LimitsController(Limit limit) {
		this.limit = limit;
	}

	@GetMapping("/limits")
	public Limit getLimits() {
		return new Limit(limit.getMinimum(), limit.getMaximum());
	}
    @GetMapping("/limits-config")
    public Limit getLimitsConfig() {
        return limit;
    }

}
