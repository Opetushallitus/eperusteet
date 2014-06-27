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

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.MuodostumisSaanto;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuliRooli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author nkala
 */
public class PerusteenRakenne {
    static public class Ongelma {
        public String ongelma;
        public TekstiPalanen ryhmä;
        public Integer syvyys;

        Ongelma(String ongelma, TekstiPalanen ryhmä, Integer syvyys) {
            this.ongelma = ongelma;
            this.ryhmä = ryhmä;
            this.syvyys = syvyys;
        }
    }

    static public class Validointi {
        public List<Ongelma> ongelmat = new ArrayList<>();
        public Integer laskettuLaajuus = 0;
    }

    static public Validointi validoiRyhma(RakenneModuuli rakenne) {
        return validoiRyhma(rakenne, 0);
    }

    static private Validointi validoiRyhma(RakenneModuuli rakenne, final Integer syvyys) {
        List<AbstractRakenneOsa> osat = rakenne.getOsat();
        MuodostumisSaanto ms = rakenne.getMuodostumisSaanto();

        Validointi validointi = new Validointi();

        Integer laajuusSummaMin = 0;
        Integer laajuusSummaMax = 0;
        Integer ryhmienMäärä = 0;
        Set<Long> uniikit = new HashSet<>();

        for (AbstractRakenneOsa x : osat) {
            if (x instanceof RakenneOsa) {
                RakenneOsa ro = (RakenneOsa)x;
                Integer laajuus = ro.getTutkinnonOsaViite().getLaajuus();
                laajuusSummaMin += laajuus;
                laajuusSummaMax += laajuus;
                uniikit.add(ro.getTutkinnonOsaViite().getTutkinnonOsa().getId());
            }
            else if (x instanceof RakenneModuuli) {
                RakenneModuuli rm = (RakenneModuuli)x;
                ++ryhmienMäärä;
                Validointi validoitu = validoiRyhma(rm, syvyys + 1);
                validointi.ongelmat.addAll(validoitu.ongelmat);
                validointi.laskettuLaajuus += validoitu.laskettuLaajuus;
                laajuusSummaMin += validoitu.laskettuLaajuus;
                laajuusSummaMax += validoitu.laskettuLaajuus;
            }
        }
        
        if (ms == null) {
            return validointi;
        }

        final TekstiPalanen nimi = rakenne.getNimi();
        final Integer kokoMin = ms.getKoko() != null ? ms.getKoko().getMinimi() : 0;
        final Integer kokoMax = ms.getKoko() != null ? ms.getKoko().getMaksimi() : 0;
        final Integer laajuusMin = ms.getLaajuus() != null ? ms.getLaajuus().getMinimi() : 0;
        final Integer laajuusMax = ms.getLaajuus() != null ? ms.getLaajuus().getMaksimi() : 0;
        final RakenneModuuliRooli rooli = rakenne.getRooli();

        if (rooli == RakenneModuuliRooli.NORMAALI) {
            if (laajuusSummaMin < laajuusMin) {
                validointi.ongelmat.add(new Ongelma("Laskettu laajuuksien summan minimi on pienempi kuin ryhmän vaadittu minimi (" + laajuusSummaMin + " < " + laajuusMin + ").", nimi, syvyys));
            }
            else if (laajuusSummaMax > laajuusMax) {
                validointi.ongelmat.add(new Ongelma("Laskettu laajuuksien summan maksimi on suurempi kuin ryhmän vaadittu maksimi (" + laajuusSummaMax + " > " + laajuusMax + ").", nimi, syvyys));
            }
            if (ms.getKoko() != null) {
                if (osat.size() < kokoMin) {
                    validointi.ongelmat.add(new Ongelma("Laskettu koko on pienempi kuin vaadittu minimi (" + osat.size() + " < " + kokoMin + ").", nimi, syvyys));
                }
                else if (osat.size() > kokoMax) {
                    validointi.ongelmat.add(new Ongelma("Laskettu koko on suurempi ryhmän vaadittu maksimi (" + osat.size() + " > " + kokoMax + ").", nimi, syvyys));
                }
            }
            if (uniikit.size() + ryhmienMäärä != osat.size()) {
                validointi.ongelmat.add(new Ongelma("Ryhmässä on samoja tutkinnon osia (" + uniikit.size() + " uniikkia).", nimi, syvyys));
            }
        }

        validointi.laskettuLaajuus = laajuusSummaMax;
        return validointi;
    }
}