package com.example.lab1;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CounterController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/counter")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "{\"result\":" + counter.incrementAndGet() + "}";
    }
}
