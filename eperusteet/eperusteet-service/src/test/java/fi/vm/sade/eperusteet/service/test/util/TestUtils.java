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

import com.google.common.base.Optional;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.OsaamistasonKriteeri;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohde;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohdealue;
import fi.vm.sade.eperusteet.domain.arviointi.Arviointi;
import fi.vm.sade.eperusteet.domain.arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.MuodostumisSaanto;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuliRooli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEKurssiDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEOppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEVaiheDto;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.TekstiOsaDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * @author teele1
 */
public abstract class TestUtils {

    public static Peruste teePeruste() {
        Peruste p = new Peruste();
        p.setNimi(tekstiPalanenOf(Kieli.FI, "Nimi"));
        return p;
    }

    public static Koodi teeKoodi() {
        Koodi koodi = new Koodi();
        return koodi;
    }

    public static String validiDiaarinumero() {
        return "OPH-" + (++uniikki).toString() + "-1234";
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

    static public RakenneOsa teeRakenneOsa(long id, Integer laajuus, Integer laajuusMax) {
        assertTrue(laajuus < laajuusMax);
        RakenneOsa to = teeRakenneOsa(id, laajuus);
        to.getTutkinnonOsaViite().setLaajuusMaksimi(new BigDecimal(laajuusMax));
        return to;
    }

    static public RakenneModuuli teeRyhma(Integer laajuusMinimi, Integer laajuusMaksimi, Integer kokoMinimi, Integer kokoMaksimi, AbstractRakenneOsa... osat) {
        RakenneModuuli rakenne = new RakenneModuuli();

        MuodostumisSaanto.Laajuus msl = laajuusMinimi != null && laajuusMinimi != -1
                ? new MuodostumisSaanto.Laajuus(laajuusMinimi, laajuusMaksimi, LaajuusYksikko.OPINTOVIIKKO) : null;
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

        MuodostumisSaanto.Laajuus msl = laajuusMinimi != null && laajuusMinimi != -1
                ? new MuodostumisSaanto.Laajuus(laajuusMinimi, laajuusMaksimi, LaajuusYksikko.OPINTOVIIKKO) : null;
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

    static Long uniikki = (long) 0;

    static public String uniikkiString() {
        return "uniikki" + (++uniikki).toString();
    }

    static public Long uniikkiId() {
        return ++uniikki;
    }

    public static LokalisoituTekstiDto lt(String teksti) {
        return new LokalisoituTekstiDto(null, Collections.singletonMap(Kieli.FI, teksti));
    }

    public static Optional<LokalisoituTekstiDto> olt(String teksti) {
        return Optional.of(lt(teksti));
    }

    public static TekstiOsaDto to(String otsikko, String teksti) {
        return new TekstiOsaDto(olt(otsikko), olt(teksti));
    }

    public static Optional<TekstiOsaDto> oto(String otsikko, String teksti) {
        return Optional.of(new TekstiOsaDto(olt(otsikko), olt(teksti)));
    }

    public static AIPEVaiheDto createVaihe() {
        AIPEVaiheDto vaihe = new AIPEVaiheDto();
        vaihe.setNimi(olt(uniikkiString()));
        vaihe.setLaajaalainenOsaaminen(oto(uniikkiString(), uniikkiString()));
        vaihe.setTehtava(oto(uniikkiString(), uniikkiString()));
        return vaihe;
    }

    public static LaajaalainenOsaaminenDto createLaajaalainen() {
        LaajaalainenOsaaminenDto lDto = new LaajaalainenOsaaminenDto();
        lDto.setNimi(olt(uniikkiString()));
        lDto.setKuvaus(olt(uniikkiString()));
        return lDto;
    }

    public static AIPEKurssiDto createAIPEKurssi() {
        AIPEKurssiDto kurssi = new AIPEKurssiDto();
        kurssi.setNimi(olt(uniikkiString()));
        kurssi.setKuvaus(olt(uniikkiString()));
        return kurssi;
    }

    public static AIPEOppiaineDto createAIPEOppiaine() {
        AIPEOppiaineDto oppiaine = new AIPEOppiaineDto();
        oppiaine.setNimi(olt(uniikkiString()));
        oppiaine.setTehtava(oto(uniikkiString(), uniikkiString()));
        return oppiaine;
    }

}
