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

package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.MuodostumisSaanto;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuliRooli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author nkala
 */
public class PerusteenRakenne {

    @Getter
    @Setter
    static public class Validointi {
        public List<Ongelma> ongelmat = new ArrayList<>();
        public BigDecimal laskettuLaajuus = new BigDecimal(0);
        public Integer sisakkaisiaOsaamisalaryhmia = 0;
    }

    @Getter
    @Setter
    static public class Ongelma {
        public String ongelma;
        public TekstiPalanen ryhma;
        public Integer syvyys;

        Ongelma(String ongelma, TekstiPalanen ryhma, Integer syvyys) {
            this.ongelma = ongelma;
            this.ryhma = ryhma;
            this.syvyys = syvyys;
        }
    }

    static private class ValidointiCtx {
        Set<Koodi> osaamisalat;
        RakenneModuuli osaamisalaryhma; // Kaikkien löydettyjen osaamisalojen tätyyy löytyä tästä
    }

    static public Validointi validoiRyhma(Set<Koodi> osaamisalat, RakenneModuuli rakenne) {
        return validoiRyhma(osaamisalat, rakenne, 0, false);
    }

    static public Validointi validoiRyhma(Set<Koodi> osaamisalat, RakenneModuuli rakenne, boolean useMax) {
        return validoiRyhma(osaamisalat, rakenne, 0, useMax);
    }

    static private Validointi validoiRyhma(Set<Koodi> osaamisalat, RakenneModuuli rakenne, final int syvyys, boolean useMax) {
        ValidointiCtx ctx = new ValidointiCtx();
        if (osaamisalat == null) {
            ctx.osaamisalat = new HashSet<>();
        }
        else {
            ctx.osaamisalat = osaamisalat;
        }
        return validoiRyhma(ctx, rakenne, null, syvyys, useMax);
    }

    static private Validointi validoiRyhma(ValidointiCtx ctx, RakenneModuuli rakenne, RakenneModuuli parent, final int syvyys, boolean useMax) {
        final TekstiPalanen nimi = rakenne.getNimi();
        final RakenneModuuliRooli rooli = rakenne.getRooli();
        List<AbstractRakenneOsa> osat = rakenne.getOsat();
        MuodostumisSaanto ms = rakenne.getMuodostumisSaanto();

        Validointi validointi = new Validointi();

        // Juurisolmulle pitää aina määrittää muodostumissääntö
        if (syvyys == 0) {
            MuodostumisSaanto rootms = rakenne.getMuodostumisSaanto();
            if (rootms == null || rootms.laajuusMinimi() == null) {
                validointi.ongelmat.add(new Ongelma("tutkinnolle-ei-maaritetty-kokonaislaajuutta", rakenne.getNimi(), 0));
            }
        }

        BigDecimal laajuusSummaMin = new BigDecimal(0);
        BigDecimal laajuusSummaMax = new BigDecimal(0);
        Integer ryhmienMaara = 0;
        Set<Long> uniikit = new HashSet<>();

        for (AbstractRakenneOsa x : osat) {
            if (x instanceof RakenneOsa) {
                RakenneOsa ro = (RakenneOsa) x;
                if (ro.getTutkinnonOsaViite() != null) {
                    TutkinnonOsaViite tov = ro.getTutkinnonOsaViite();
                    BigDecimal laajuus = ro.getTutkinnonOsaViite().getLaajuus();
                    laajuus = laajuus == null ? new BigDecimal(0) : laajuus;
                    if (useMax
                            && tov.getLaajuusMaksimi() != null
                            && tov.getLaajuusMaksimi().compareTo(laajuus) > 0) {
                        laajuus = tov.getLaajuusMaksimi();
                    }
                    laajuusSummaMin = laajuusSummaMin.add(laajuus);
                    laajuusSummaMax = laajuusSummaMax.add(laajuus);
                    uniikit.add(tov.getTutkinnonOsa().getId());
                }
            } else if (x instanceof RakenneModuuli) {
                RakenneModuuli rm = (RakenneModuuli) x;
                ++ryhmienMaara;
                Validointi validoitu = validoiRyhma(ctx, rm, rakenne, syvyys + 1, useMax);
                validointi.ongelmat.addAll(validoitu.ongelmat);
                validointi.laskettuLaajuus = validointi.laskettuLaajuus.add(validoitu.laskettuLaajuus);
                validointi.sisakkaisiaOsaamisalaryhmia = validoitu.sisakkaisiaOsaamisalaryhmia;
                laajuusSummaMin = laajuusSummaMin.add(validoitu.laskettuLaajuus);
                laajuusSummaMax = laajuusSummaMax.add(validoitu.laskettuLaajuus);
            }
        }

        if (rooli == RakenneModuuliRooli.OSAAMISALA) {
            validointi.sisakkaisiaOsaamisalaryhmia = validointi.sisakkaisiaOsaamisalaryhmia + 1;
            // Tarkista löytyykö perusteesta valittua osaamisalaa
            Koodi roa = rakenne.getOsaamisala();
            if (roa != null) {
                boolean osaamisalaaEiPerusteella = true;
                for (Koodi oa : ctx.osaamisalat) {
                    if (roa.equals(oa)) {
                        osaamisalaaEiPerusteella = false;
                        break;
                    }
                }
                if (osaamisalaaEiPerusteella) {
                    validointi.ongelmat.add(new Ongelma("ryhman-osaamisalaa-ei-perusteella", nimi, syvyys));
                }
            }
        }

        // Ylin taso ei voi olla osaamisala
        if (rooli != null && rooli != RakenneModuuliRooli.NORMAALI && syvyys == 0) {
            validointi.ongelmat.add(new Ongelma("paatason-muodostumisen-rooli-virheellinen", nimi, syvyys));
        }

        // Osaamisaloja saa löytyä ainoastaan yhdestä ryhmästä
        if (rooli == RakenneModuuliRooli.OSAAMISALA) {
            if (ctx.osaamisalaryhma == null) {
                ctx.osaamisalaryhma = parent;
            }
            else if (!Objects.equals(parent.getTunniste(), ctx.osaamisalaryhma.getTunniste())) {
                validointi.ongelmat.add(new Ongelma("rakenteessa-osaamisaloja-useassa-ryhmassa", nimi, syvyys));
            }
        }

        if (rooli == RakenneModuuliRooli.VIRTUAALINEN) {
            if (osat.size() > 0) {
                validointi.ongelmat.add(new Ongelma("Rakennehierarkia ei saa sisältää tutkinnossa määriteltäviä ryhmiä, joihin liitetty osia", nimi, syvyys));
            }
        }

        if (validointi.sisakkaisiaOsaamisalaryhmia > 1) {
            validointi.sisakkaisiaOsaamisalaryhmia = 1;
            validointi.ongelmat.add(new Ongelma("Rakenteessa sisäkkäisiä osaamisalaryhmiä", nimi, syvyys));
        }

        if (rooli == RakenneModuuliRooli.NORMAALI && uniikit.size() + ryhmienMaara != osat.size()) {
            validointi.ongelmat.add(new Ongelma("Ryhmässä on samoja tutkinnon osia (" + uniikit.size() + " uniikkia).", nimi, syvyys));
        }

        if (ms != null) {
            final Integer kokoMin = ms.kokoMinimi();
            final Integer kokoMax = ms.kokoMaksimi();
            final BigDecimal laajuusMin = new BigDecimal(ms.laajuusMinimi());
            final BigDecimal laajuusMax = new BigDecimal(ms.laajuusMaksimi());

            if (rooli == RakenneModuuliRooli.NORMAALI) {
                if (laajuusSummaMin.compareTo(laajuusMin) == -1) {
                    validointi.ongelmat.add(new Ongelma("Laskettu laajuuksien summan minimi on pienempi kuin ryhmän vaadittu minimi (" + laajuusSummaMin + " < " + laajuusMin + ").", nimi, syvyys));
                } else if (laajuusSummaMax.compareTo(laajuusMax) == -1) {
                    validointi.ongelmat.add(new Ongelma("Laskettu laajuuksien summan maksimi on pienempi kuin ryhmän vaadittu maksimi (" + laajuusSummaMax + " > " + laajuusMax + ").", nimi, syvyys));
                }

                if (osat.size() < kokoMin) {
                    validointi.ongelmat.add(new Ongelma("Laskettu koko on pienempi kuin vaadittu minimi (" + osat.size() + " < " + kokoMin + ").", nimi, syvyys));
                } else if (osat.size() < kokoMax) {
                    validointi.ongelmat.add(new Ongelma("Laskettu koko on pienempi kuin ryhmän vaadittu maksimi (" + osat.size() + " < " + kokoMax + ").", nimi, syvyys));
                }
                validointi.laskettuLaajuus = ms.laajuusMaksimi() != null && ms.laajuusMaksimi() > 0 ? laajuusMax : laajuusSummaMax;
            } else {
                validointi.laskettuLaajuus = laajuusMax;
            }
        }
        return validointi;
    }
}