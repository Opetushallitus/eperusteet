package fi.vm.sade.eperusteet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
@Profile("!test")
public class ScheduledConfiguration implements SchedulingConfigurer {
    private static final Logger log = LoggerFactory.getLogger(ScheduledConfiguration.class);
    private static AtomicBoolean isUpdating = new AtomicBoolean(false);
    private final ThreadPoolTaskScheduler scheduler;

    @Autowired
    private ThreadPoolTaskExecutor pool;

    @Autowired
    private List<ScheduledTask> tasks = new ArrayList<>();

    ScheduledConfiguration() {
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.setErrorHandler(err -> log.error(err.getMessage(), err));
        scheduler.initialize();
    }

    @PostConstruct
    public void sortTasks() {
        // Järjestetään tehtävät prioriteetin mukaan
        tasks = tasks.stream()
                .sorted(Comparator.comparing(ScheduledTask::getPriority)
                        .thenComparing(ScheduledTask::getName)
                        .reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        registrar.setScheduler(this.scheduler);
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledValidationTask() {
        if (!isUpdating.get()) {
            if (isUpdating.compareAndSet(false, true)) {
                log.debug("Starting " + tasks.size() + " background jobs.");
                int done = 0;
                try {

                    // Käytetään pääkäyttäjän oikeuksia.
                    Authentication token = new UsernamePasswordAuthenticationToken("system",
                            "ROLE_ADMIN", AuthorityUtils.createAuthorityList("ROLE_ADMIN",
                            "ROLE_APP_EPERUSTEET_CRUD_1.2.246.562.10.00000000001"));
                    SecurityContextHolder.getContext().setAuthentication(token);

                    // Suoritetaan tehtävät
                    for (ScheduledTask task : tasks) {
                        try {
                            log.debug("Starting " + task.getName() + " job");
                            task.execute();
                            done++;
                            log.debug(task.getName() + " is done.");
                        } catch (RuntimeException e) {
                            log.error("Error occurred while running " + task.getName() + " job:", e);
                        }
                    }

                    SecurityContextHolder.getContext().setAuthentication(null);

                }
                catch (Exception e) {
                    log.debug("Fatal error occurred while running background jobs");
                    isUpdating.set(false);
                    return;
                }

                if (done != tasks.size()) {
                    log.info("Only " + done + "/" + tasks.size() + " background jobs are successfully done.");
                } else {
                    log.info("All background jobs are successfully done.");
                }

                isUpdating.set(false);
            } else {
                log.debug("Background jobs are already running.");
            }
        }
    }
}
