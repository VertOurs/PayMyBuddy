package fr.vertours.buddtwo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {


    @GetMapping
    public ModelAndView showBuddyIndexPage() {
        return new ModelAndView("index");
    }
}
