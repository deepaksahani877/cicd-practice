package org.example.cicdpractice;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping
    public Map<String, String> index() {
        Map<String,String> map = new HashMap<>();
        map.put("message","Hello , CI/CD working fine😊");
        map.put("status","ok");
        return  map;
    }
}
