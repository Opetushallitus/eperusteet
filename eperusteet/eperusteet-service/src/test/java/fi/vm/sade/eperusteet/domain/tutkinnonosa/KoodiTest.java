package fi.vm.sade.eperusteet.domain.tutkinnonosa;

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import org.junit.Test;

public class KoodiTest {
    @Test
    public void validKoodisto() {
        Koodi koodi = new Koodi();
        koodi.setKoodisto("koodisto");
        koodi.setUri("koodisto_uri");
        koodi.onPrePersist();
    }

    @Test(expected = BusinessRuleViolationException.class)
    public void invalidKoodisto() {
        Koodi koodi = new Koodi();
        koodi.setKoodisto("koodisto");
        koodi.setUri("koodisto2_uri");
        koodi.onPrePersist();
    }
}
