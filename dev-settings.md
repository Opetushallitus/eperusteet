
### user.home\oph-configuration\override.properties
- ulkopuolisten palveluiden sijaintien configuraatiot
- korvaa [username] ja [password] käyttäjän testiopintopolku tunnuksiin

```properties
cas.service.authentication-service=https://virkailija.testiopintopolku.fi/authentication-service
cas.service.organisaatio-service=https://virkailija.testiopintopolku.fi/organisaatio-service
cas.service.oppijanumerorekisteri-service=https://virkailija.testiopintopolku.fi/oppijanumerorekisteri-service
cas.service.kayttooikeus-service=https://virkailija.testiopintopolku.fi/kayttooikeus-service
fi.vm.sade.eperusteet.ylops.eperusteet-service=https://virkailija.testiopintopolku.fi/eperusteet-service
fi.vm.sade.eperusteet.amosaa.eperusteet-service=https://virkailija.testiopintopolku.fi/eperusteet-service
koodisto.service.url=https://virkailija.testiopintopolku.fi/koodisto-service

fi.vm.sade.eperusteet.salli_virheelliset=true

fi.vm.sade.eperusteet.amosaa.oph_username=[username]
fi.vm.sade.eperusteet.amosaa.oph_password=[password]
fi.vm.sade.eperusteet.ylops.oph_username=[username]
fi.vm.sade.eperusteet.ylops.oph_password=[password]
fi.vm.sade.eperusteet.oph_username=[username]
fi.vm.sade.eperusteet.oph_password=[password]

web.url.cas=https\://virkailija.testiopintopolku.fi/cas
hibernate.show_sql=false
```


### user.home\oph-configuration\security-context-backend.xml
- sovelluksen käyttämä kirjautumisconfiguraation
- authority muodossa ROLE_APP_[palvelu] _[oikeus] _[organisaatio]

```xml
<beans:beans xmlns="http://www.springframework.org/schema/security"
           xmlns:beans="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.springframework.org/schema/beans
                               http://www.springframework.org/schema/beans/spring-beans-3.1.xsd
                               http://www.springframework.org/schema/security
                               http://www.springframework.org/schema/security/spring-security.xsd">
    
  <http use-expressions="true" request-matcher="ant">
      <http-basic/>
      <!-- todo: default rooli -->
      <intercept-url pattern="/" access="permitAll"/>
      <intercept-url pattern="/index.html" access="permitAll"/>
      <intercept-url pattern="/**" access="${spring_security_default_access}"/>
      <!--<logout logout-url="/logout"/>--> <!-- no server side logout for basic auth -->
      <csrf disabled="true"/>
  </http>
    
  <beans:bean id="tokenRepository" class="org.springframework.security.web.csrf.CookieCsrfTokenRepository">
      <beans:property name="cookieHttpOnly" value="false"/>
      <beans:property name="cookieName" value="CSRF"/>
      <beans:property name="headerName" value="CSRF"/>
      <beans:property name="cookiePath" value="/"/>
  </beans:bean>
    
  <authentication-manager alias="authenticationManager">
      <authentication-provider>
        <user-service>
        <user name="test" 
              password="{noop}test" 
              authorities="ROLE_USER,
                           ROLE_APP_EPERUSTEET,
                           ROLE_APP_EPERUSTEET_CRUD,
                           ROLE_APP_EPERUSTEET_ADMIN,
                           ROLE_APP_EPERUSTEET_AMOSAA,
                           ROLE_APP_EPERUSTEET_AMOSAA_ADMIN_1.2.246.562.10.2013120512391252668625
                           ROLE_APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.10.61057016927,
                           ROLE_APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.10.20516711478,
                           ROLE_APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.28.55860281986,
                           ROLE_APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.28.11287634288,
                           ROLE_APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.28.85557110211,
                           ROLE_APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.10.79499343246,
                           ROLE_APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001,
                           ROLE_APP_EPERUSTEET_ADMIN_1.2.246.562.10.00000000001,
                           ROLE_APP_EPERUSTEET_YLOPS,
                           ROLE_APP_EPERUSTEET_YLOPS_CRUD,
                           ROLE_APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.10.22840843613,
                           ROLE_APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.10.68534785412,
                           ROLE_APP_EPERUSTEET_YLOPS_ADMIN_1.2.246.562.10.00000000001,
                           ROLE_APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.10.00000000001" />
          <user name="ep"     
                password="{noop}ep"     
                authorities="ROLE_USER, 
                             ROLE_APP_EPERUSTEET,        
                             ROLE_APP_EPERUSTEET_CRUD,       
                             ROLE_APP_EPERUSTEET_ADMIN_1.2.246.562.10.00000000001,       
                             ROLE_APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001" />
          <user name="ylops"  
                password="{noop}ylops"  
                authorities="ROLE_USER, 
                             ROLE_APP_EPERUSTEET_YLOPS,  
                             ROLE_APP_EPERUSTEET_YLOPS_CRUD, 
                             ROLE_APP_EPERUSTEET_YLOPS_ADMIN_1.2.246.562.10.00000000001, 
                             ROLE_APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.10.00000000001" />
          <user name="amosaa" 
                password="{noop}amosaa" 
                authorities="ROLE_USER, 
                             ROLE_APP_EPERUSTEET_AMOSAA, 
                             ROLE_APP_EPERUSTEET_AMOSAA_CRUD,
                             ROLE_APP_EPERUSTEET_AMOSAA_ADMIN_1.2.246.562.10.00000000001,
                             ROLE_APP_EPERUSTEET_AMOSAA_CRUD_1.2.246.562.10.00000000001" />
        </user-service>
      </authentication-provider>
  </authentication-manager>
    
  <beans:bean id="accessDecisionManager" class="org.springframework.security.access.vote.AffirmativeBased">
      <beans:constructor-arg>
          <beans:list>
              <beans:bean class="org.springframework.security.access.vote.RoleVoter"/>
          </beans:list>
      </beans:constructor-arg>
      <beans:property name="allowIfAllAbstainDecisions" value="true"/>
  </beans:bean>
</beans:beans>
```
