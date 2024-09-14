package net.wickedshell.ticketz.adapter.web.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(Throwable.class)
    public ModelAndView exception(final Throwable throwable, final Model model) {
        final ModelAndView modelAndView = new ModelAndView("error");
        String errorMessage = (throwable != null ? throwable.getMessage() : "Unknown error");
        modelAndView.addObject("message", errorMessage);
        return modelAndView;
    }
}
