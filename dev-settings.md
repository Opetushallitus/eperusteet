
### user.home\\.m2\settings.xml
- maven settings

```xml
  <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                        https://maven.apache.org/xsd/settings-1.0.0.xsd">

    <profiles>
      <profile>
        <id>oph</id>
          <properties>
          <eperusteet.devdb.user>oph</eperusteet.devdb.user>
          <eperusteet.devdb.password>test</eperusteet.devdb.password>
          <eperusteet.devdb.jdbcurl>jdbc:postgresql://localhost:5432/eperusteet</eperusteet.devdb.jdbcurl>

          <eperusteet-amosaa.devdb.user>oph</eperusteet-amosaa.devdb.user>
          <eperusteet-amosaa.devdb.password>test</eperusteet-amosaa.devdb.password>
          <eperusteet-amosaa.devdb.jdbcurl>jdbc:postgresql://localhost:5433/amosaa</eperusteet-amosaa.devdb.jdbcurl>

          <eperusteet-ylops.devdb.user>oph</eperusteet-ylops.devdb.user>
          <eperusteet-ylops.devdb.password>test</eperusteet-ylops.devdb.password>
          <eperusteet-ylops.devdb.jdbcurl>jdbc:postgresql://localhost:5434/ylops</eperusteet-ylops.devdb.jdbcurl>
        </properties>
      </profile>
    </profiles>

  </settings>
```

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
          <user name="ep"     password="{noop}ep"     authorities="ROLE_USER, ROLE_APP_EPERUSTEET,        ROLE_APP_EPERUSTEET_CRUD,       ROLE_APP_EPERUSTEET_ADMIN_1.2.246.562.10.00000000001,       ROLE_APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001" />
          <user name="ylops"  password="{noop}ylops"  authorities="ROLE_USER, ROLE_APP_EPERUSTEET_YLOPS,  ROLE_APP_EPERUSTEET_YLOPS_CRUD, ROLE_APP_EPERUSTEET_YLOPS_ADMIN_1.2.246.562.10.00000000001, ROLE_APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.10.00000000001" />
          <user name="amosaa" password="{noop}amosaa" authorities="ROLE_USER, ROLE_APP_EPERUSTEET_AMOSAA, ROLE_APP_EPERUSTEET_AMOSAA_CRUD,ROLE_APP_EPERUSTEET_AMOSAA_ADMIN_1.2.246.562.10.00000000001,ROLE_APP_EPERUSTEET_AMOSAA_CRUD_1.2.246.562.10.00000000001" />
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
