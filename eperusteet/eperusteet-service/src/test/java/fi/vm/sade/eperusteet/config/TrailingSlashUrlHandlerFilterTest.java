package fi.vm.sade.eperusteet.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.UrlHandlerFilter;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class TrailingSlashUrlHandlerFilterTest {

    @Test
    public void catchAllPatternStripsTrailingSlashWhenContextPathPresent() throws Exception {
        UrlHandlerFilter filter = UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build();

        AtomicReference<String> requestUri = new AtomicReference<>();
        AtomicReference<String> servletPath = new AtomicReference<>();
        filter.doFilter(requestWithTrailingSlash(), new MockHttpServletResponse(), (req, res) -> {
            HttpServletRequest http = (HttpServletRequest) req;
            requestUri.set(http.getRequestURI());
            servletPath.set(http.getServletPath());
        });

        assertThat(requestUri.get()).isEqualTo("/eperusteet-service/api/perusteet/419550/kaikki");
        assertThat(servletPath.get()).isEqualTo("/api/perusteet/419550/kaikki");
    }

    @Test
    public void apiPatternDoesNotMatchFullPathThatIncludesContextPath() throws Exception {
        UrlHandlerFilter filter = UrlHandlerFilter.trailingSlashHandler("/api/**").wrapRequest().build();

        AtomicReference<String> requestUri = new AtomicReference<>();
        filter.doFilter(requestWithTrailingSlash(), new MockHttpServletResponse(), (req, res) -> {
            requestUri.set(((HttpServletRequest) req).getRequestURI());
        });

        assertThat(requestUri.get()).isEqualTo("/eperusteet-service/api/perusteet/419550/kaikki/");
    }

    private static MockHttpServletRequest requestWithTrailingSlash() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setContextPath("/eperusteet-service");
        request.setServletPath("/api/perusteet/419550/kaikki/");
        request.setRequestURI("/eperusteet-service/api/perusteet/419550/kaikki/");
        return request;
    }
}
