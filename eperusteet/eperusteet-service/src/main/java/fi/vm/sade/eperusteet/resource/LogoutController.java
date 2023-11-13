package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.config.InternalApi;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/")
@Api("Logout")
@InternalApi
public class LogoutController {

    @PostMapping(value = "/logout")
    public void logoutPOST(HttpServletRequest request, HttpServletResponse response) throws IOException {
        deleteCookies(request, response);
    }

    @GetMapping(value = "/logout")
    public void logoutGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        deleteCookies(request, response);

        String url = request.getRequestURL().toString().replace(request.getRequestURI(),"");
        response.sendRedirect(url + "/service-provider-app/saml/logout");
    }

    private static void deleteCookies(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().invalidate();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }
}
