package fi.vm.sade.eperusteet.resource.util;

import java.util.Date;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Lisää ResponseEntityyn Cache-Control ja Expires -headerit jos niitä ei ole erikseen lisätty.
 */
@Aspect
@Component
public class CacheHeadersAspect {

    private static final Logger LOG = LoggerFactory.getLogger(CacheHeadersAspect.class);

    @Pointcut("execution(org.springframework.http.ResponseEntity fi.vm.sade.eperusteet.resource..*Controller.*(..))")
    public void controller() {}

    @Around("controller() && !@annotation(fi.vm.sade.eperusteet.resource.util.CacheControl)")
    public Object aroundResponse(ProceedingJoinPoint jp) throws Throwable {
        Object rv = jp.proceed();
        if (rv instanceof ResponseEntity) {
            return addCacheHeaders((ResponseEntity<?>) rv, -1, CacheControls.PRIVATE_NOT_CACHEABLE);
        } else {
            return rv;
        }
    }

    @Around("controller() && @annotation(cacheControl)")
    public Object aroundResponse(ProceedingJoinPoint jp, CacheControl cacheControl) throws Throwable {
        Object rv = jp.proceed();
        if (rv instanceof ResponseEntity) {
            return addCacheHeaders((ResponseEntity<?>) rv, cacheControl.age(), CacheControls.buildCacheControl(cacheControl));
        } else {
            return rv;
        }
    }

    private <T> Object addCacheHeaders(ResponseEntity<T> responseEntity, long age, String cacheControl) {
        if (responseEntity.getHeaders().getCacheControl() == null) {
            HttpHeaders headers = new HttpHeaders();
            headers.putAll(responseEntity.getHeaders());
            if ( age < 0 ) {
                headers.setExpires(0);
            } else {
                headers.setExpires((new Date().getTime()) + age * 1000);
            }
            headers.setCacheControl(cacheControl);
            return new ResponseEntity<>(responseEntity.getBody(), headers, responseEntity.getStatusCode());
        }
        return responseEntity;
    }
}
