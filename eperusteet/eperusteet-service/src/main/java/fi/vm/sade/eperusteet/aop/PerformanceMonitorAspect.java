package fi.vm.sade.eperusteet.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceMonitorAspect {

    private static final Logger LOG = LoggerFactory.getLogger("fi.vm.sade.eperusteet.performance");

    @Around("@within(fi.vm.sade.eperusteet.aop.PerformanceMonitor) || @annotation(fi.vm.sade.eperusteet.aop.PerformanceMonitor)")
    public Object monitor(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
            String type = joinPoint.getSignature().getDeclaringType().getSimpleName();
            String method = joinPoint.getSignature().getName();
            LOG.info("{}#{} took {} ms", type, method, elapsedMs);
        }
    }
}
