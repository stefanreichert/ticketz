package net.wickedshell.ticketz.adapter.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.web.Action;
import net.wickedshell.ticketz.adapter.web.model.Signup;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.access.UserService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

import static net.wickedshell.ticketz.adapter.web.Action.*;
import static net.wickedshell.ticketz.adapter.web.View.VIEW_SIGNUP;
import static net.wickedshell.ticketz.service.model.Role.ROLE_USER;

@RequiredArgsConstructor
@Controller
public class SignupController {

    private static final String ATTRIBUTE_NAME_SIGNUP = "signup";
    private static final String ATTRIBUTE_NAME_MESSAGE = "message";

    private final UserService userService;
    private final MessageSource messageSource;

    private static boolean validationFailed(Signup signup, BindingResult bindingResult) {
        if (!signup.getPassword().equals(signup.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "message.password_mismatch");
        }
        return bindingResult.hasErrors();
    }

    @GetMapping(ACTION_SHOW_SIGNUP)
    public String showSignup(Model model) {
        model.addAttribute(ATTRIBUTE_NAME_SIGNUP, new Signup());
        return VIEW_SIGNUP;
    }

    @PostMapping(Action.ACTION_SIGNUP)
    public ModelAndView signup(@Valid @ModelAttribute Signup signup, BindingResult bindingResult,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        if (validationFailed(signup, bindingResult)) {
            return new ModelAndView(VIEW_SIGNUP).addObject(ATTRIBUTE_NAME_SIGNUP, signup);
        }
        User user = new User();
        user.setFirstname(signup.getFirstname());
        user.setLastname(signup.getLastname());
        user.setEmail(signup.getEmail());
        userService.create(user, signup.getPassword(), Set.of(ROLE_USER));
        String message = messageSource.getMessage("message.signup_succeeded", null, request.getLocale());
        redirectAttributes.addFlashAttribute(ATTRIBUTE_NAME_MESSAGE, message);
        return new ModelAndView(redirectTo(ACTION_SHOW_LOGIN));
    }
}