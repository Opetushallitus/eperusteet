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

import fi.vm.sade.eperusteet.domain.Arviointi.ArvioinninKohde;
import fi.vm.sade.eperusteet.domain.Arviointi.ArvioinninKohdealue;
import fi.vm.sade.eperusteet.domain.Arviointi.Arviointi;
import fi.vm.sade.eperusteet.domain.Arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.OsaamistasonKriteeri;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.Perusteprojekti_;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.MuodostumisSaanto;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuliRooli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteenOsaService;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author teele1
 */
public abstract class TestUtils {

    public static Peruste teePeruste() {
        Peruste p = new Peruste();
        p.setNimi(tekstiPalanenOf(Kieli.FI, "Nimi"));
        return p;
    }

    public static Arviointi teeArviointi(ArviointiAsteikko arviointiasteikko) {
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
        tov.setLaajuus(new BigDecimal(laajuus));

        RakenneOsa ro = new RakenneOsa();
        ro.setTutkinnonOsaViite(tov);
        return ro;
    }

    static public RakenneModuuli teeRyhma(Integer laajuusMinimi, Integer laajuusMaksimi, Integer kokoMinimi, Integer kokoMaksimi, AbstractRakenneOsa... osat) {
        RakenneModuuli rakenne = new RakenneModuuli();

        MuodostumisSaanto.Laajuus msl = laajuusMinimi != null && laajuusMinimi != -1 ? new MuodostumisSaanto.Laajuus(laajuusMinimi, laajuusMaksimi, LaajuusYksikko.OPINTOVIIKKO) : null;
        MuodostumisSaanto.Koko msk = kokoMinimi != null && kokoMinimi != -1 ? new MuodostumisSaanto.Koko(kokoMinimi, kokoMaksimi) : null;
        MuodostumisSaanto ms = (msl != null || msk != null) ? new MuodostumisSaanto(msl, msk) : null;

        ArrayList<AbstractRakenneOsa> aosat = new ArrayList<>();
        aosat.addAll(Arrays.asList(osat));
        rakenne.setOsat(aosat);
        rakenne.setMuodostumisSaanto(ms);
        rakenne.setRooli(RakenneModuuliRooli.NORMAALI);
        return rakenne;
    }

    static public RakenneModuuli teeOsaamisalaRyhma(Integer laajuusMinimi, Integer laajuusMaksimi, Integer kokoMinimi, Integer kokoMaksimi, AbstractRakenneOsa... osat) {
        RakenneModuuli rakenne = new RakenneModuuli();

        MuodostumisSaanto.Laajuus msl = laajuusMinimi != null && laajuusMinimi != -1 ? new MuodostumisSaanto.Laajuus(laajuusMinimi, laajuusMaksimi, LaajuusYksikko.OPINTOVIIKKO) : null;
        MuodostumisSaanto.Koko msk = kokoMinimi != null && kokoMinimi != -1 ? new MuodostumisSaanto.Koko(kokoMinimi, kokoMaksimi) : null;
        MuodostumisSaanto ms = (msl != null || msk != null) ? new MuodostumisSaanto(msl, msk) : null;

        ArrayList<AbstractRakenneOsa> aosat = new ArrayList<>();
        aosat.addAll(Arrays.asList(osat));
        rakenne.setOsat(aosat);
        rakenne.setMuodostumisSaanto(ms);
        rakenne.setRooli(RakenneModuuliRooli.OSAAMISALA);
        return rakenne;
    }

    static public PerusteenOsaViite teePerusteenOsaViite() {
        PerusteenOsaViite pov = new PerusteenOsaViite();
        return pov;
    }

    static Long uniikki = (long)0;
    static public String uniikkiString() {
        return "uniikki" + (++uniikki).toString();
    }

    static public Long uniikkiId() {
        return ++uniikki;
    }
}
