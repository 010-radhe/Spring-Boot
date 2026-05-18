package com.example.rest.webservices.restful_web_services.helloWorld;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("hello-world")
    public String helloWorld(){
        return "Hello World";
    }

    @GetMapping("hello-world-bean")
    public HelloWorldBean helloWorldBean(){
        return new HelloWorldBean("Hello Radhe from Bean");
    }
    
    @GetMapping("hello-world/path-variable/{name}")
    public String helloWorldName(@PathVariable String name)
    {
        return String.format("Hello World, %s", name);
    }
}
