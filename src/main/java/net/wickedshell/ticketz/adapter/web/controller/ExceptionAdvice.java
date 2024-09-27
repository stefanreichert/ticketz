package net.wickedshell.ticketz.adapter.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import net.wickedshell.ticketz.adapter.web.WebView;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;

@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler(Exception.class)
    public ModelAndView exception(final Exception exception, HttpServletRequest request) {
        LOGGER.error(exception.getMessage(), exception);
        final ModelAndView modelAndView = new ModelAndView(WebView.VIEW_ERROR);
        modelAndView.addObject("url", request.getRequestURL().toString());
        modelAndView.addObject("timestamp", Instant.now());
        modelAndView.addObject("message", exception.getMessage());
        modelAndView.addObject("exception", exception);
        return modelAndView;
    }
}
