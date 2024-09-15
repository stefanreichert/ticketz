package net.wickedshell.ticketz.adapter.web.controller;

import net.wickedshell.ticketz.adapter.web.WebView;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {
    @RequestMapping(value = "/index")
    public String index() {
        return WebView.VIEW_INDEX;
    }
}