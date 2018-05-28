package fi.vm.sade.eperusteet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
@EnableScheduling
@Profile("!test")
public class ScheduledConfiguration implements SchedulingConfigurer {
    private static final Logger log = LoggerFactory.getLogger(ScheduledConfiguration.class);
    private static AtomicBoolean isUpdating = new AtomicBoolean(false);
    private final ThreadPoolTaskScheduler scheduler;

    ScheduledConfiguration() {
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.setErrorHandler(err -> {
            log.error(err.getMessage());
        });
        scheduler.initialize();
    }

    @Autowired
    PerusteprojektiService perusteprojektiService;

    @Autowired
    ThreadPoolTaskExecutor pool;

    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledValidationTask() {
        task();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        registrar.setScheduler(this.scheduler);
    }

    private void task() {
        if (!isUpdating.get()) {
            if (isUpdating.compareAndSet(false, true)) {
                log.debug("Starting background job");
                perusteprojektiService.validoiPerusteetTask();
                isUpdating.set(false);
                log.debug("Background job done");
            }
            else {
                log.debug("Already updating");
            }
        }

    }
}
