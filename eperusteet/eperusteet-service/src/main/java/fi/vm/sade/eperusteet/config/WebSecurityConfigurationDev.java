package fi.vm.sade.eperusteet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import java.util.List;

@Profile({"local"})
@Configuration
@EnableMethodSecurity(securedEnabled = true)
@EnableWebSecurity
public class WebSecurityConfigurationDev {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/dokumentit/pdf/**").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .logout(logout -> {
                    logout.logoutUrl("/api/logout");
                    logout.logoutSuccessUrl("http://localhost:9001");
                    logout.addLogoutHandler(new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.ALL)));
                    logout.invalidateHttpSession(true);
                })
                .requestCache(cache -> cache.requestCache(requestCache));

        return http.build();
    }

    @Bean
    public UserDetailsService users() {
        UserDetails test = User.withDefaultPasswordEncoder()
                .username("test")
                .password("test")
                .roles("USER", "APP_EPERUSTEET", "APP_EPERUSTEET_READ", "APP_EPERUSTEET_MAARAYS_READ", "APP_EPERUSTEET_MAARAYS_CRUD", "APP_EPERUSTEET_VST", "APP_EPERUSTEET_TUVA", "APP_EPERUSTEET_KOTO", "APP_EPERUSTEET_CRUD", "APP_EPERUSTEET_ADMIN_1.2.246.562.10.00000000001", "APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001", "APP_EPERUSTEET_CRUD_1.2.246.562.28.55860281986", "APP_EPERUSTEET_CRUD_1.2.246.562.28.11287634288", "APP_EPERUSTEET_CRUD_1.2.246.562.28.85557110211", "APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001", "APP_EPERUSTEET_YLOPS", "APP_EPERUSTEET_YLOPS_CRUD", "APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.10.22840843613", "APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.10.68534785412", "APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.10.00000000001", "APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.10.61057016927", "APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.10.13649470005", "APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.10.11902547485", "APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.28.11332956371", "APP_EPERUSTEET_YLOPS_ADMIN_1.2.246.562.10.00000000001", "APP_EPERUSTEET_YLOPS_ADMIN_1.2.246.562.10.20516711478", "APP_EPERUSTEET_YLOPS_ADMIN_1.2.246.562.10.83037752777", "APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.10.83037752777", "APP_EPERUSTEET_YLOPS_ADMIN_1.2.246.562.10.346830761110", "APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.10.346830761110", "APP_EPERUSTEET_AMOSAA_ADMIN_1.2.246.562.10.15738250156", "APP_EPERUSTEET_AMOSAA_ADMIN_1.2.246.562.10.54645809036", "APP_EPERUSTEET_YLOPS_ADMIN_1.2.246.562.10.90008375488", "APP_EPERUSTEET_AMOSAA_ADMIN_1.2.246.562.10.00000000001", "APP_EPERUSTEET_AMOSAA_ADMIN_1.2.246.562.28.37193106103", "APP_EPERUSTEET_YLOPS_ADMIN_1.2.246.562.28.25927836418", "APP_EPERUSTEET_ADMIN_1.2.246.562.28.39318578962", "APP_EPERUSTEET_VST_ADMIN_1.2.246.562.10.54645809036", "APP_EPERUSTEET_TUVA_ADMIN_1.2.246.562.10.54645809036", "APP_EPERUSTEET_KOTO_ADMIN_1.2.246.562.10.54645809036")
                .build();
        return new InMemoryUserDetailsManager(test);
    }

    @Bean
    public CookieCsrfTokenRepository cookieCsrfTokenRepository() {
        CookieCsrfTokenRepository cookieCsrfTokenRepository = new CookieCsrfTokenRepository();
        cookieCsrfTokenRepository.setCookieHttpOnly(false);
        cookieCsrfTokenRepository.setCookieName("CSRF");
        cookieCsrfTokenRepository.setHeaderName("CSRF");
        cookieCsrfTokenRepository.setCookiePath("/");
        return cookieCsrfTokenRepository;
    }

    @Bean
    public AffirmativeBased affirmativeBased() {
        AffirmativeBased affirmativeBased = new AffirmativeBased(List.of(new RoleVoter()));
        affirmativeBased.setAllowIfAllAbstainDecisions(true);
        return affirmativeBased;
    }
}
