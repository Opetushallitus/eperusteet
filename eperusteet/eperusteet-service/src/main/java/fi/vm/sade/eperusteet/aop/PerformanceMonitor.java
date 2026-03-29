package fi.vm.sade.eperusteet.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Lokittaa metodin tai luokan kaikkien julkisten metodien suoritusajan.
 * Luokkatason annotaatio: lisää luokan itsensä metodeihin, ei rajapinnan toteutuksiin automaattisesti.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface PerformanceMonitor {
}
