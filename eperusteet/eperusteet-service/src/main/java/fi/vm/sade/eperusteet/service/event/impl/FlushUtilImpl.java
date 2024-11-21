package fi.vm.sade.eperusteet.service.event.impl;

import fi.vm.sade.eperusteet.service.event.FlushUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class FlushUtilImpl implements FlushUtil {
    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void flush() {
        em.flush();
    }
}
