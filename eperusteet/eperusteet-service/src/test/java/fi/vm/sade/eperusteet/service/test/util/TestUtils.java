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
import fi.vm.sade.eperusteet.domain.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.ArvioinninKohde;
import fi.vm.sade.eperusteet.domain.ArvioinninKohdealue;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.OsaamistasonKriteeri;
import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.MuodostumisSaanto;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuliRooli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import java.util.ArrayList;
import java.util.Arrays;
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
        p.setNimi(tekstiPalanenOf(Kieli.FI, "Nimi"));

        return p;
    }

    public static Arviointi createArviointi(ArviointiAsteikko arviointiasteikko) {
        Arviointi arviointi = new Arviointi();
        arviointi.setLisatiedot(tekstiPalanenOf(Kieli.FI, "lisätieto"));

        ArvioinninKohdealue arvioinninKohdealue = new ArvioinninKohdealue();
        arvioinninKohdealue.setOtsikko(tekstiPalanenOf(Kieli.FI, "otsikko"));
        arviointi.setArvioinninKohdealueet(Collections.singletonList(arvioinninKohdealue));

        ArvioinninKohde arvioinninKohde = new ArvioinninKohde();
        arvioinninKohde.setOtsikko(tekstiPalanenOf(Kieli.FI, "otsikko"));
        arvioinninKohde.setArviointiAsteikko(arviointiasteikko);
        arvioinninKohdealue.setArvioinninKohteet(Collections.singletonList(arvioinninKohde));

        Set<OsaamistasonKriteeri> kriteerit = new HashSet<>();
        for (Osaamistaso osaamistaso : arviointiasteikko.getOsaamistasot()) {
            OsaamistasonKriteeri osaamistasonKriteeri = new OsaamistasonKriteeri();
            osaamistasonKriteeri.setOsaamistaso(osaamistaso);
            osaamistasonKriteeri.setKriteerit(Collections.singletonList(tekstiPalanenOf(Kieli.FI, "tekstialue")));
            kriteerit.add(osaamistasonKriteeri);
        }
        arvioinninKohde.setOsaamistasonKriteerit(kriteerit);

        return arviointi;
    }

    public static TekstiPalanen tekstiPalanenOf(Kieli k, String teksti) {
        return TekstiPalanen.of(Collections.singletonMap(k, teksti));
    }

    static public RakenneOsa teeRakenneOsa(long id, Integer laajuus) {
        TutkinnonOsa to = new TutkinnonOsa();
        to.setId(id);

        TutkinnonOsaViite tov = new TutkinnonOsaViite();
        tov.setTutkinnonOsa(to);
        tov.setLaajuus(laajuus);

        RakenneOsa ro = new RakenneOsa();
        ro.setTutkinnonOsaViite(tov);
        return ro;
    }

    static public RakenneModuuli teeRyhma(Integer minimi, Integer maksimi, AbstractRakenneOsa... osat) {
        RakenneModuuli rakenne = new RakenneModuuli();
        MuodostumisSaanto ms = new MuodostumisSaanto(new MuodostumisSaanto.Laajuus(minimi, maksimi, LaajuusYksikko.OPINTOVIIKKO));
        ArrayList<AbstractRakenneOsa> aosat = new ArrayList<>();
        aosat.addAll(Arrays.asList(osat));
        rakenne.setOsat(aosat);
        rakenne.setMuodostumisSaanto(ms);
        rakenne.setRooli(RakenneModuuliRooli.NORMAALI);
        return rakenne;
    }
}
