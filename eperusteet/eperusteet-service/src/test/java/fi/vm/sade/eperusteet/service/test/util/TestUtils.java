/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 * 
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.service.test.util;

import fi.vm.sade.eperusteet.domain.Arviointi;
import fi.vm.sade.eperusteet.domain.Arviointiasteikko;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Kohde;
import fi.vm.sade.eperusteet.domain.Kohdealue;
import fi.vm.sade.eperusteet.domain.Kriteeri;
import fi.vm.sade.eperusteet.domain.LokalisoituTeksti;
import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author teele1
 */
public abstract class TestUtils {

    public static Peruste createPeruste() {
        Peruste p = new Peruste();
        p.setNimi(new TekstiPalanen(Collections.singletonMap(Kieli.FI, new LokalisoituTeksti(Kieli.FI, "Nimi"))));

        return p;
    }

    public static Arviointi createArviointi(Arviointiasteikko arviointiasteikko) {
        Arviointi arviointi = new Arviointi();
        arviointi.setLisatiedot(new TekstiPalanen(Collections.singletonMap(Kieli.FI, new LokalisoituTeksti(Kieli.FI, "lisätieto"))));

        Kohdealue kohdealue = new Kohdealue();
        kohdealue.setOtsikko(new TekstiPalanen(Collections.singletonMap(Kieli.FI, new LokalisoituTeksti(Kieli.FI, "otsikko"))));
        arviointi.setKohdealueet(Collections.singletonList(kohdealue));

        Kohde kohde = new Kohde();
        kohde.setOtsikko(new TekstiPalanen(Collections.singletonMap(Kieli.FI, new LokalisoituTeksti(Kieli.FI, "otsikko"))));
        kohde.setArviointiasteikko(arviointiasteikko);
        kohdealue.setKohteet(Collections.singletonList(kohde));

        Set<Kriteeri> kriteerit = new HashSet<>();
        for (Osaamistaso osaamistaso : arviointiasteikko.getOsaamistasot()) {
            Kriteeri kriteeri = new Kriteeri();
            kriteeri.setOsaamistaso(osaamistaso);
            kriteeri.setTekstialueet(Collections.singletonList(
                    new TekstiPalanen(Collections.singletonMap(Kieli.FI, new LokalisoituTeksti(Kieli.FI, "tekstialue")))));
            kriteerit.add(kriteeri);
        }
        kohde.setKriteerit(kriteerit);

        return arviointi;
    }
}
