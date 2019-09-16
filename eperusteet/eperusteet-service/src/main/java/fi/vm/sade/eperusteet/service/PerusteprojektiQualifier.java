package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Qualifier
@Target({
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.TYPE,
        ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface PerusteprojektiQualifier {
    KoulutustyyppiToteutus[] value();
}