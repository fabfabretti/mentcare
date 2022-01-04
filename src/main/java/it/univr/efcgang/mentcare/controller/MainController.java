package it.univr.efcgang.mentcare.controller;


import it.univr.efcgang.mentcare.config.AuthService;
import it.univr.efcgang.mentcare.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
public class MainController implements ErrorController {

    private final String fakeUser = "maria"; // null | "admin" | "maria" | ...

    @Autowired
    AuthService authService;

    @GetMapping("/")
    public String getIndex() {
        //TODO: remove this, it's autologin for manual testing purposes only
        User userAuth = authService.UserAuth();
        if (userAuth==null && fakeUser != null) {
            authService.UserSet(fakeUser);
        }
        return "index";
    }

    @GetMapping("/login")
    public String getLogin() {
        User userAuth = authService.UserAuth();
        if (userAuth==null){
            return "login";
        }
        return "redirect:/profile";
    }

    @GetMapping("/profile")
    public String getProfile(Model model) {
        User authUser = authService.UserAuth();
        model.addAttribute("user",authUser);
        return "profile";
    }

    @GetMapping("/logout")
    public String getLogout (HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login"; //You can redirect wherever you want, but generally it's a good practice to show login screen again.
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        String error_message = "Unknown Error";
        int statusCode=-1;

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
            error_message = "Error "+statusCode;
        }

        if(statusCode == HttpStatus.FORBIDDEN.value()) {
            error_message += ":Access forbidden.\nYou need higher powers to access this resource.\nThis incident will be reported.";
        }
        else if(statusCode == HttpStatus.NOT_FOUND.value()) {
            error_message = ": Not found.\nThe resource you are looking for has never existed.";
        }
        else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            error_message = ": Access forbidden.\nServer is on fire.";
        }

        model.addAttribute("error_message",error_message);
        return "error";
    }


    @Override
    public String getErrorPath() {return null;}
}