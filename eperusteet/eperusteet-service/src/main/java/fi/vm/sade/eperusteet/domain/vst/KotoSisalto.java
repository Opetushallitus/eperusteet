package fi.vm.sade.eperusteet.domain.vst;

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import java.util.List;

public interface KotoSisalto {
    Koodi getNimiKoodi();
    TekstiPalanen getKuvaus();
    List<KotoTaitotaso> getTaitotasot();
}
