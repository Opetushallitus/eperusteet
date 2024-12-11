package fi.vm.sade.eperusteet.config;

import fi.vm.sade.eperusteet.repository.version.JpaWithVersioningRepositoryFactoryBean;
import fi.vm.sade.eperusteet.service.security.PermissionEvaluator;
import jakarta.persistence.EntityManager;
import org.flywaydb.core.Flyway;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.id.enhanced.SingleNamingStrategy;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import javax.sql.DataSource;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Profile("!test")
@Configuration
@ComponentScan(basePackages  = {"fi.vm.sade.eperusteet.utils"})
@EnableAsync
@EnableCaching
@EnableTransactionManagement
@EnableMethodSecurity(securedEnabled = true)
@EnableAspectJAutoProxy // (proxyTargetClass = true)
@EnableJpaRepositories(basePackages = "fi.vm.sade.eperusteet.repository", repositoryFactoryBeanClass = JpaWithVersioningRepositoryFactoryBean.class)
@PropertySource(
        ignoreResourceNotFound = true, value={
            "file:///${user.home:''}/oph-configuration/eperusteet.properties",
            "file:///${user.home:''}/oph-configuration/override.properties"
        })
public class DefaultConfigs {

    @Autowired
    private DataSource dataSource;

    @Bean
    public TaskExecutor defaultExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.initialize();

        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }

    @Bean
    public PermissionEvaluator eperusteetPermissionEvaluator() {
        return new PermissionEvaluator();
    }

    @Bean
    public DefaultMethodSecurityExpressionHandler expressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(eperusteetPermissionEvaluator());
        return expressionHandler;
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        return Flyway.configure()
                .dataSource(dataSource)
                .outOfOrder(true)
                .table("schema_version")
                .load();
    }

    @Bean
    public MessageSource messageSource() {
        return new ResourceBundleMessageSource() {
            @Override
            protected MessageFormat resolveCode(String code, Locale locale) {
                return null; // Disable message resolution
            }
        };
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setPersistenceUnitName("eperusteet-pu");
        entityManagerFactory.setDataSource(dataSource);
        entityManagerFactory.setPackagesToScan("fi.vm.sade.eperusteet.domain");
        entityManagerFactory.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        entityManagerFactory.setEntityManagerInterface(EntityManager.class);
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "validate");
        props.put("hibernate.show_sql", false);
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("jakarta.persistence.sharedCache.mode", "ENABLE_SELECTIVE");
        props.put("org.hibernate.envers.audit_strategy", "org.hibernate.envers.strategy.internal.DefaultAuditStrategy");
        props.put("org.hibernate.envers.revision_listener", "fi.vm.sade.eperusteet.service.impl.AuditRevisionListener");
        props.put("hibernate.jdbc.batch_size", 20);
        props.put("hibernate.jdbc.fetch_size", 20);
        props.put(AvailableSettings.ID_DB_STRUCTURE_NAMING_STRATEGY, SingleNamingStrategy.STRATEGY_NAME);
        entityManagerFactory.setJpaPropertyMap(props);
        return entityManagerFactory;
    }

    @Bean
    public JpaTransactionManager transactionManager() {
        return new JpaTransactionManager(entityManagerFactory().getObject());
    }

    @Bean
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }

}
