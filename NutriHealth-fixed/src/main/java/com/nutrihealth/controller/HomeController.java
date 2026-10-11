package com.nutrihealth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "NutriHealth – Web-Based Diet Planning & Health Check-up System");
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About Us – NutriHealth");
        return "index";
    }

    @GetMapping("/features")
    public String features(Model model) {
        model.addAttribute("title", "Platform Capabilities – NutriHealth");
        return "index";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("title", "Contact Clinic Services – NutriHealth");
        return "index";
    }
}
