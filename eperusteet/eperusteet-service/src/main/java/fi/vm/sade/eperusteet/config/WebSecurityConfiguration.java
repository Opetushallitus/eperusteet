package fi.vm.sade.eperusteet.config;

import fi.vm.sade.eperusteet.repository.OphSessionMappingStorage;
import fi.vm.sade.eperusteet.service.util.RestClientFactoryImpl;
import fi.vm.sade.java_utils.security.OpintopolkuCasAuthenticationFilter;
import fi.vm.sade.javautils.http.auth.CasAuthenticator;
import fi.vm.sade.javautils.kayttooikeusclient.OphUserDetailsServiceImpl;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Profile({"!dev & !test"})
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableWebSecurity
public class WebSecurityConfiguration {

    @Value("${cas.key}")
    private String casKey;

    @Value("${cas.service}")
    private String casService;

    @Value("${cas.sendRenew}")
    private boolean casSendRenew;

    @Value("${cas.login}")
    private String casLogin;

    @Value("${host.alb}")
    private String hostAlb;

    @Value("${web.url.cas}")
    private String webUrlCas;

    @Value("${fi.vm.sade.eperusteet.oph_username}")
    private String eperusteet_username;

    @Value("${fi.vm.sade.eperusteet.oph_password}")
    private String eperusteet_password;

    @Autowired
    private OphSessionMappingStorage ophSessionMappingStorage;

    @Bean
    public CasAuthenticator casAuthenticator() {
        return new CasAuthenticator(this.webUrlCas, eperusteet_username, eperusteet_password, hostAlb, null, false, null);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new OphUserDetailsServiceImpl(this.hostAlb, RestClientFactoryImpl.CALLER_ID, casAuthenticator());
    }

    @Bean
    public ServiceProperties serviceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(this.casService + "/j_spring_cas_security_check");
        serviceProperties.setSendRenew(this.casSendRenew);
        serviceProperties.setAuthenticateAllArtifacts(true);
        return serviceProperties;
    }

    @Bean
    public CasAuthenticationProvider casAuthenticationProvider() {
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
        casAuthenticationProvider.setUserDetailsService(userDetailsService());
        casAuthenticationProvider.setServiceProperties(serviceProperties());
        casAuthenticationProvider.setTicketValidator(ticketValidator());
        casAuthenticationProvider.setKey(this.casKey);
        return casAuthenticationProvider;
    }

    @Bean
    public TicketValidator ticketValidator() {
        Cas20ProxyTicketValidator ticketValidator = new Cas20ProxyTicketValidator(this.webUrlCas);
        ticketValidator.setAcceptAnyProxy(true);
        return ticketValidator;
    }

    @Bean
    public CasAuthenticationFilter casAuthenticationFilter(HttpSecurity http) throws Exception {
        OpintopolkuCasAuthenticationFilter casAuthenticationFilter = new OpintopolkuCasAuthenticationFilter(serviceProperties());
        casAuthenticationFilter.setAuthenticationManager(authenticationManager(http));
        casAuthenticationFilter.setFilterProcessesUrl("/j_spring_cas_security_check");
        casAuthenticationFilter.setAuthenticationSuccessHandler(new SavedRequestAwareAuthenticationSuccessHandler());
        return casAuthenticationFilter;
    }

    //
    // CAS single logout filter
    // requestSingleLogoutFilter is not configured because our users always sign out through CAS logout (using virkailija-raamit
    // logout button) when CAS calls this filter if user has ticket to this service.
    //
    @Bean
    public SingleSignOutFilter singleSignOutFilter() {
        SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
        singleSignOutFilter.setIgnoreInitConfiguration(true);
        singleSignOutFilter.setSessionMappingStorage(ophSessionMappingStorage);
        return singleSignOutFilter;
    }

    @Bean
    public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
        CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
        casAuthenticationEntryPoint.setLoginUrl(this.casLogin);
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
        return casAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .headers().disable()
                .csrf().disable()
                .authorizeRequests()
                    .antMatchers("/buildversion.txt").permitAll()
                    .antMatchers(HttpMethod.GET, "/api/**").permitAll()
                    .antMatchers(HttpMethod.POST, "/api/palaute").permitAll()
                    .anyRequest().authenticated()
                .and()
                .addFilter(casAuthenticationFilter(http))
                    .exceptionHandling()
                    .authenticationEntryPoint(casAuthenticationEntryPoint())
                .and()
                .addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(casAuthenticationProvider());
        return authenticationManagerBuilder.build();
    }
}
