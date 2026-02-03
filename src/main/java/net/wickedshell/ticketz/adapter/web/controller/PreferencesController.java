package net.wickedshell.ticketz.adapter.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.web.model.PasswordChangeWeb;
import net.wickedshell.ticketz.adapter.web.model.PreferencesWeb;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.access.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static net.wickedshell.ticketz.adapter.web.Action.*;
import static net.wickedshell.ticketz.adapter.web.View.VIEW_PREFERENCES;

@Controller
@RequiredArgsConstructor
public class PreferencesController {

    private static final String ATTRIBUTE_NAME_PREFERENCES = "preferences";
    private static final String ATTRIBUTE_NAME_PASSWORD_CHANGE = "passwordChange";
    private static final String ATTRIBUTE_NAME_MESSAGE = "message";

    @Qualifier("webModelMapper")
    private final ModelMapper mapper;
    private final UserService userService;
    private final MessageSource messageSource;

    @GetMapping(ACTION_SHOW_PREFERENCES)
    public String showPreferences(@PathVariable String email, Model model) {
        User user = userService.findByEmail(email).orElseThrow();
        PreferencesWeb preferencesWeb = mapper.map(user, PreferencesWeb.class);
        model.addAttribute(ATTRIBUTE_NAME_PREFERENCES, preferencesWeb);
        model.addAttribute(ATTRIBUTE_NAME_PASSWORD_CHANGE, new PasswordChangeWeb());
        return VIEW_PREFERENCES;
    }

    @PostMapping(ACTION_SAVE_PREFERENCES_NAME)
    public ModelAndView saveName(@PathVariable String email,
                                 @Valid @ModelAttribute(ATTRIBUTE_NAME_PREFERENCES) PreferencesWeb preferencesWeb,
                                 BindingResult bindingResult,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView(VIEW_PREFERENCES)
                    .addObject(ATTRIBUTE_NAME_PREFERENCES, preferencesWeb)
                    .addObject(ATTRIBUTE_NAME_PASSWORD_CHANGE, new PasswordChangeWeb());
        }
        User user = mapper.map(preferencesWeb, User.class);
        userService.updateName(user);
        String message = messageSource.getMessage("message.preferences.name_saved", null, request.getLocale());
        redirectAttributes.addFlashAttribute(ATTRIBUTE_NAME_MESSAGE, message);
        return new ModelAndView(redirectTo(ACTION_SHOW_PREFERENCES.replace("{email}", email)));
    }

    @PostMapping(ACTION_SAVE_PREFERENCES_PASSWORD)
    public ModelAndView savePassword(@PathVariable String email,
                                     @Valid @ModelAttribute(ATTRIBUTE_NAME_PASSWORD_CHANGE) PasswordChangeWeb passwordChangeWeb,
                                     BindingResult bindingResult,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        if (!passwordChangeWeb.getNewPassword().equals(passwordChangeWeb.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "message.password_mismatch");
        }
        if (bindingResult.hasErrors()) {
            User user = userService.findByEmail(email).orElseThrow();
            PreferencesWeb preferencesWeb = mapper.map(user, PreferencesWeb.class);
            return new ModelAndView(VIEW_PREFERENCES)
                    .addObject(ATTRIBUTE_NAME_PREFERENCES, preferencesWeb)
                    .addObject(ATTRIBUTE_NAME_PASSWORD_CHANGE, passwordChangeWeb);
        }
        userService.updatePassword(email, passwordChangeWeb.getCurrentPassword(), passwordChangeWeb.getNewPassword());
        String message = messageSource.getMessage("message.preferences.password_saved", null, request.getLocale());
        redirectAttributes.addFlashAttribute(ATTRIBUTE_NAME_MESSAGE, message);
        return new ModelAndView(redirectTo(ACTION_SHOW_PREFERENCES.replace("{email}", email)));
    }
}
