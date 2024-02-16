package fi.vm.sade.eperusteet.service.internal;

import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;

public interface SuoritustapaService {
    Suoritustapa createSuoritustapaWithSisaltoAndRakenneRoots(Suoritustapakoodi suoritustapakoodi, LaajuusYksikko yksikko);
    Suoritustapa createSuoritustapa(Suoritustapakoodi suoritustapakoodi, LaajuusYksikko yksikko);
    Suoritustapa createFromOther(final Long suoritustapaId);
}
