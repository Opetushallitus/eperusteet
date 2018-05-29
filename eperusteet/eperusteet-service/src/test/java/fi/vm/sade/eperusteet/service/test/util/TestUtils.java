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
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohde;
import fi.vm.sade.eperusteet.domain.arviointi.ArvioinninKohdealue;
import fi.vm.sade.eperusteet.domain.arviointi.Arviointi;
import fi.vm.sade.eperusteet.domain.arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.*;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEKurssiDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEOppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEVaiheDto;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.TekstiOsaDto;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

import static org.junit.Assert.assertTrue;

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
        rakenne.setOsaamisala(new Koodi());
        rakenne.setMuodostumisSaanto(ms);
        rakenne.setRooli(RakenneModuuliRooli.OSAAMISALA);
        return rakenne;
    }

    static public class RakenneBuilder<T extends RakenneBuilder<T>> {
        @Getter
        private AbstractRakenneOsa osa;

        protected RakenneBuilder(AbstractRakenneOsa osa) {
            this.osa = osa;
        }

        public T kuvaus(TekstiPalanen kuvaus) {
            osa.setKuvaus(kuvaus);
            return (T)this;
        }

        public T pakollinen(boolean pakollinen) {
            osa.setPakollinen(pakollinen);
            return (T)this;
        }

        public T id(Long id) {
            osa.setId(id);
            return (T)this;
        }

        public T vieras(Koodi vieras) {
            osa.setVieras(vieras);
            return (T)this;
        }

    }

    static public class RakenneOsaBuilder extends RakenneBuilder<RakenneOsaBuilder> {

        public RakenneOsaBuilder() {
            super(new RakenneOsa());
        }

        protected RakenneOsa osa() {
            return (RakenneOsa)super.getOsa();
        }

        public RakenneOsaBuilder tutkinnonOsaViite(TutkinnonOsaViite viite) {
            osa().setTutkinnonOsaViite(viite);
            return this;
        }

        public RakenneOsaBuilder erikoisuus(String erikoisuus) {
            osa().setErikoisuus(erikoisuus);
            return this;
        }

        public RakenneOsa build() {
            return osa();
        }
    }

    static public class RakenneModuuliBuilder extends RakenneBuilder<RakenneModuuliBuilder> {
        protected RakenneModuuli rakenne() {
            return (RakenneModuuli)super.getOsa();
        }

        public RakenneModuuliBuilder() {
            super(new RakenneModuuli());
            rakenne().setOsat(new ArrayList<>());
            rakenne().setRooli(RakenneModuuliRooli.NORMAALI);
            rakenne().setMuodostumisSaanto(new MuodostumisSaanto());
            rakenne().setNimi(TekstiPalanen.of(Kieli.FI, uniikkiString()));
        }

        public RakenneModuuliBuilder laajuus(Integer minimi) {
            return this.laajuus(minimi, minimi);
        }

        public RakenneModuuliBuilder laajuus(Integer minimi, Integer maksimi) {
            rakenne().setMuodostumisSaanto(new MuodostumisSaanto(
                    new MuodostumisSaanto.Laajuus(minimi, maksimi, LaajuusYksikko.OSAAMISPISTE),
                    rakenne().getMuodostumisSaanto().getKoko()));
            return this;
        }

        public RakenneModuuliBuilder koko(Integer minimi) {
            return this.koko(minimi, minimi);
        }

        // "Täyttää" rakennemoduulin osilla
        public RakenneModuuliBuilder tayta() {
            Integer laajuus = rakenne().getOsat().stream()
                    .map(osa -> {
                        if (osa instanceof RakenneModuuli) {
                            MuodostumisSaanto saanto = ((RakenneModuuli) osa).getMuodostumisSaanto();
                            return saanto != null && saanto.getLaajuus() != null && saanto.getLaajuus().getMinimi() != null
                                    ? saanto.getLaajuus().getMinimi()
                                    : 0;
                        } else if (osa instanceof RakenneOsa) {
                            return ((RakenneOsa) osa).getTutkinnonOsaViite().getLaajuus().intValue();
                        } else {
                            return 0;
                        }
                    }).mapToInt(Integer::intValue).sum();
            Integer vaadittuMinimi = rakenne().getMuodostumisSaanto().laajuusMinimi();
            if (vaadittuMinimi != null && vaadittuMinimi > 0) {
                Integer lisaosanLaajuus = vaadittuMinimi - laajuus;
                if (lisaosanLaajuus > 0) {
                    TutkinnonOsa to = new TutkinnonOsa();
                    TutkinnonOsaViite tov = new TutkinnonOsaViite();
                    tov.setTutkinnonOsa(to);
                    tov.setLaajuus(new BigDecimal(lisaosanLaajuus));
                    osa(r -> r.tutkinnonOsaViite(tov));
                }
            }
            return this;
        }

        public RakenneModuuliBuilder koko(Integer minimi, Integer maksimi) {
            rakenne().setMuodostumisSaanto(new MuodostumisSaanto(
                    rakenne().getMuodostumisSaanto().getLaajuus(),
                    new MuodostumisSaanto.Koko(minimi, maksimi)));
            return this;
        }

        public RakenneModuuliBuilder osaamisala(Koodi osaamisala) {
            rakenne().setOsaamisala(osaamisala);
            return this;
        }

        public RakenneModuuliBuilder tutkintonimike(Koodi tutkintonimike) {
            rakenne().setTutkintonimike(tutkintonimike);
            return this;
        }

        public RakenneModuuliBuilder nimi(TekstiPalanen nimi) {
            rakenne().setNimi(nimi);
            return this;
        }

        public RakenneModuuliBuilder nimi(String nimi) {
            rakenne().setNimi(TekstiPalanen.of(Kieli.FI, nimi));
            return this;
        }

        public RakenneModuuliBuilder erikoisuus(RakenneModuuliErikoisuus erikoisuus) {
            rakenne().setErikoisuus(erikoisuus);
            return this;
        }

        public RakenneModuuliBuilder rooli(RakenneModuuliRooli rooli) {
            rakenne().setRooli(rooli);
            return this;
        }

        public RakenneModuuliBuilder ryhma(RakenneModuuliBuilder builder) {
            rakenne().getOsat().add(builder.build());
            return this;
        }

        public RakenneModuuliBuilder ryhma(Function<RakenneModuuliBuilder, RakenneModuuliBuilder> fn) {
            rakenne().getOsat().add(fn.apply(new RakenneModuuliBuilder()).build());
            return this;
        }

        public RakenneModuuliBuilder osa(Function<RakenneOsaBuilder, RakenneOsaBuilder> fn) {
            rakenne().getOsat().add(fn.apply(new RakenneOsaBuilder()).build());
            return this;
        }

        public RakenneModuuli build() {
            return rakenne();
        }
    }

    static public RakenneModuuliBuilder rakenneModuuli() {
        return new RakenneModuuliBuilder();
    }

    static public PerusteenOsaViite teePerusteenOsaViite() {
        PerusteenOsaViite pov = new PerusteenOsaViite();
        return pov;
    }

    static private Long uniikki = (long) 0;

    static public String uniikkiString() {
        return "uniikki" + (++uniikki).toString();
    }

    static public String uniikkiString(String prefix) {
        return prefix + (++uniikki).toString();
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

    public static TiedoteDto createTiedote() {
        TiedoteDto tiedoteDto = new TiedoteDto();
        tiedoteDto.setOtsikko(lt(uniikkiString()));
        tiedoteDto.setSisalto(lt(uniikkiString()));
        return tiedoteDto;
    }
}
