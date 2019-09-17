package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class PerusteFactory<T> {

    @Autowired(required = false)
    @PerusteprojektiQualifier(KoulutustyyppiToteutus.LOPS2019)
    private T strategyLops2019;

    @Autowired(required = false)
    @PerusteprojektiQualifier(KoulutustyyppiToteutus.PERUSOPETUS)
    private T strategyPerusopetus;

    @Autowired(required = false)
    @PerusteprojektiQualifier(KoulutustyyppiToteutus.YKSINKERTAINEN)
    private T strategyYksinkertainen;

    @PreAuthorize("permitAll()")
    public T getStrategy(KoulutustyyppiToteutus toteutus) {
        if (toteutus == null) {
            throw new BusinessRuleViolationException("toteutus-on-pakollinen");
        }

        switch (toteutus) {
            case LOPS2019: return strategyLops2019;
            case PERUSOPETUS: return strategyPerusopetus;
            case YKSINKERTAINEN: return strategyYksinkertainen;
            default:
                throw new BusinessRuleViolationException("toteutusta-ei-loytynyt");
        }
    }
}
