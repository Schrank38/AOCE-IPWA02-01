package com.sheasepherd.ghostnet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectToNetze() {
        return "redirect:/netze";
    }
}