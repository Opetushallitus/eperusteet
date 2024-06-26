package fi.vm.sade.eperusteet.service.event.aop;

import fi.vm.sade.eperusteet.service.event.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Aspect
public class MarkPerusteUpdatedAspect {
    @Autowired
    private PerusteUpdateStore perusteUpdateStore;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private ResolverUtil resolverUtil;

    @Autowired
    private FlushUtil flushUtil;

    @Pointcut("execution(* fi.vm.sade.eperusteet.service..*ServiceImpl.*(..)) " +
            " && ( within(@org.springframework.transaction.annotation.Transactional *) " +
            "       || @annotation(org.springframework.transaction.annotation.Transactional) )" +
            " && !@annotation(fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck)")
    public void transactionalServiceMethods() {}

    @Order(150) // must have higher than the one specified for tx:annotation-driven to perform within transaction (just to be sure)
    @Around("transactionalServiceMethods()")
    public Object aroundServiceMethods(ProceedingJoinPoint pjp) throws Throwable {
        perusteUpdateStore.enter();
        Object ret = null;
        int txStackDept;
        try {
            ret = pjp.proceed();
        } finally {
            txStackDept = perusteUpdateStore.leave();
        }
        if (txStackDept == 0) {
            // TODO: transactional REQUIRES_NEW-propagation mode, which, however, luckily
            // is not used in this project. (Seems impossible to use named parameters with || operands in pointcut :/)

            // Leaving the outermost transactional service method, doing flush so that all changes
            // are marked (and possibly newly added Perustees are persisted etc.) and only once:
            flushUtil.flush();
            publishPerusteEvents();
        }
        return ret;
    }

    private void publishPerusteEvents() {
        Set<Long> perusteIds = perusteUpdateStore.getAndClearUpdatedPerusteIds();
        publishPerusteChanges(perusteIds);
        if (perusteIds.isEmpty()) {
            // no direct relation to peruste found
            publishPerusteUpdatedByResolvableReferences(perusteIds);
        }
    }

    private void publishPerusteUpdatedByResolvableReferences(Set<Long> perusteIds) {
        for (ResolvableReferenced resolvable : perusteUpdateStore.getAndClearReferenced()) {
            // but there was an edited entity where references can be made from multiple places
            Set<Long> ids = resolverUtil.findPerusteIdsByFirstResolvable(resolvable);
            if (!ids.isEmpty()) {
                publishPerusteChanges(ids);
                // continuing with others:
                return;
            }
        }
    }

    private void publishPerusteChanges(Set<Long> perusteIds) {
        for (Long id : perusteIds) {
            applicationEventPublisher.publishEvent(new PerusteUpdatedEvent(this, id));
        }
    }
}
