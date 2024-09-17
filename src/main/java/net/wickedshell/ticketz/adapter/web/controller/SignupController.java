package net.wickedshell.ticketz.adapter.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.web.WebAction;
import net.wickedshell.ticketz.adapter.web.WebView;
import net.wickedshell.ticketz.adapter.web.model.Signup;
import net.wickedshell.ticketz.service.model.Role;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.rest.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Set;

@RequiredArgsConstructor
@Controller
public class SignupController {

    private final UserService userService;

    @GetMapping(WebAction.ACTION_SHOW_SIGNUP)
    public String showSignup(Model model) {
        model.addAttribute("signup", new Signup());
        return WebView.VIEW_SIGNUP;
    }

    @PostMapping(WebAction.ACTION_SIGNUP)
    public ModelAndView signup(@Valid @ModelAttribute Signup signup, BindingResult bindingResult) {
        if (validationFailed(signup, bindingResult)) {
            return new ModelAndView(WebView.VIEW_SIGNUP);
        }
        User user = new User();
        user.setFirstname(signup.getFirstname());
        user.setLastname(signup.getLastname());
        user.setEmail(signup.getEmail());
        userService.create(user, signup.getPassword(), Set.of(Role.ROLE_USER));
        return new ModelAndView("redirect:" + WebAction.ACTION_SHOW_LOGIN);
    }

    private static boolean validationFailed(Signup signup, BindingResult bindingResult) {
        if (!signup.getPassword().equals(signup.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword","signup.password.mismatch", "Passwords do not match");
        }
        return bindingResult.hasErrors();
    }
}