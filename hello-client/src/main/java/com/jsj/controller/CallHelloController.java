package com.jsj.controller;

import com.jsj.service.ConsumeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/call")
public class CallHelloController {

    @Resource
    private ConsumeService consumeService;

    @GetMapping("")
    public String callHello() {
        return consumeService.callHello();
    }
}
