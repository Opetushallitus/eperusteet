package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.MuodostumisSaanto;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuliRooli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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

        Ongelma() {

        }

        Ongelma(String ongelma, TekstiPalanen ryhma, Integer syvyys) {
            this.ongelma = ongelma;
            this.ryhma = ryhma;
            this.syvyys = syvyys;
        }
    }

    @Getter
    @Setter
    static public class Context {
        Set<Koodi> osaamisalat = new HashSet<>();
        List<TutkintonimikeKoodiDto> tutkintonimikkeet = new ArrayList<>();

        public Context(Set<Koodi> osaamisalat, List<TutkintonimikeKoodiDto> tutkintonimikkeet) {
            if (osaamisalat != null) {
                this.osaamisalat = osaamisalat;
            }
            if (tutkintonimikkeet != null) {
                this.tutkintonimikkeet = tutkintonimikkeet;
            }
        }

        public Set<String> getOsaamisalanTutkintonimikekoodit(String osaamisalaUri) {
            return this.tutkintonimikkeet.stream()
                    .filter(koodi -> Objects.equals(koodi.getOsaamisalaUri(), osaamisalaUri))
                    .map(TutkintonimikeKoodiDto::getTutkintonimikeUri)
                    .collect(Collectors.toSet());
        }
    }

    @Getter
    @Setter
    static private class ValidointiState {
        Context context;
        RakenneModuuli osaamisalaryhma; // Kaikkien löydettyjen osaamisalojen tätyyy löytyä tästä

        ValidointiState(Context ctx) {
            this.context = ctx;
        }
    }

    static public Validointi validoiRyhma(Context ctx, RakenneModuuli rakenne) {
        return validoiRyhma(ctx, rakenne, 0, false);
    }

    static public Validointi validoiRyhma(Context ctx, RakenneModuuli rakenne, boolean useMax) {
        return validoiRyhma(ctx, rakenne, 0, useMax);
    }

    static private Validointi validoiRyhma(Context ctx, RakenneModuuli rakenne, final int syvyys, boolean useMax) {
        if (ctx == null) {
            ctx = new Context(null, null);
        }
        if (ctx.getOsaamisalat() == null) {
            ctx.setOsaamisalat(new HashSet<>());
        }
        if (ctx.getOsaamisalat() == null) {
            ctx.setOsaamisalat(new HashSet<>());
        }
        return validoiRyhma(new ValidointiState(ctx), rakenne, null, syvyys, useMax);
    }

    static private Validointi validoiRyhma(ValidointiState ctx, RakenneModuuli rakenne, RakenneModuuli parent, final int syvyys, boolean useMax) {
        final TekstiPalanen nimi = rakenne.getNimi();
        final RakenneModuuliRooli rooli = rakenne.getRooli();
        List<AbstractRakenneOsa> osat = rakenne.getOsat();
        if (osat == null) {
            osat = new ArrayList<>();
        }

        MuodostumisSaanto ms = rakenne.getMuodostumisSaanto();

        Validointi validointi = new Validointi();

        // Juurisolmulle pitää aina määrittää muodostumissääntö
        if (syvyys == 0) {
            MuodostumisSaanto rootms = rakenne.getMuodostumisSaanto();
            if (rootms == null || rootms.getLaajuus() == null || rootms.getLaajuus().getMinimi() == null) {
                validointi.ongelmat.add(new Ongelma("tutkinnolle-ei-maaritetty-kokonaislaajuutta", rakenne.getNimi(), 0));
            }
        }

        BigDecimal laajuusSummaMin = new BigDecimal(0);
        BigDecimal laajuusSummaMax = new BigDecimal(0);
        Integer ryhmienMaara = 0;
        Set<Long> uniikit = new HashSet<>();

        BigDecimal osaamisalojenLaajuus = null;
        BigDecimal tutkintonimikkeidenLaajuus = null;

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
            }
            else if (x instanceof RakenneModuuli) {
                RakenneModuuli rm = (RakenneModuuli) x;
                ++ryhmienMaara;
                Validointi validoitu = validoiRyhma(ctx, rm, rakenne, syvyys + 1, useMax);
                if (rm.getRooli() == RakenneModuuliRooli.OSAAMISALA) {
                    if (osaamisalojenLaajuus == null || validoitu.getLaskettuLaajuus().compareTo(osaamisalojenLaajuus) < 0) {
                        osaamisalojenLaajuus = validoitu.getLaskettuLaajuus();
                    }
                }
                else if (rm.getRooli() == RakenneModuuliRooli.TUTKINTONIMIKE) {
                    if (tutkintonimikkeidenLaajuus == null || validoitu.getLaskettuLaajuus().compareTo(tutkintonimikkeidenLaajuus) < 0) {
                        tutkintonimikkeidenLaajuus = validoitu.getLaskettuLaajuus();
                    }
                }
                else {
                    validointi.laskettuLaajuus = validointi.laskettuLaajuus.add(validoitu.laskettuLaajuus);
                    laajuusSummaMin = laajuusSummaMin.add(validoitu.laskettuLaajuus);
                    laajuusSummaMax = laajuusSummaMax.add(validoitu.laskettuLaajuus);
                }
                validointi.ongelmat.addAll(validoitu.ongelmat);
                validointi.sisakkaisiaOsaamisalaryhmia = validoitu.sisakkaisiaOsaamisalaryhmia;
            }
        }

        if (tutkintonimikkeidenLaajuus != null) {
            validointi.laskettuLaajuus = validointi.laskettuLaajuus.add(tutkintonimikkeidenLaajuus);
            laajuusSummaMin = laajuusSummaMin.add(tutkintonimikkeidenLaajuus);
            laajuusSummaMax = laajuusSummaMax.add(tutkintonimikkeidenLaajuus);
        }

        if (osaamisalojenLaajuus != null) {
            validointi.laskettuLaajuus = validointi.laskettuLaajuus.add(osaamisalojenLaajuus);
            laajuusSummaMin = laajuusSummaMin.add(osaamisalojenLaajuus);
            laajuusSummaMax = laajuusSummaMax.add(osaamisalojenLaajuus);
        }

        if (rooli == RakenneModuuliRooli.OSAAMISALA) {
            validoiOsaamisala(ctx, rakenne, syvyys, nimi, validointi);
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
            else if (parent != null && !Objects.equals(parent.getTunniste(), ctx.osaamisalaryhma.getTunniste())) {
                validointi.ongelmat.add(new Ongelma("rakenteessa-osaamisaloja-useassa-ryhmassa", nimi, syvyys));
            }
        }

        if (rooli == RakenneModuuliRooli.VIRTUAALINEN) {
            if (osat.size() > 0) {
                validointi.ongelmat.add(new Ongelma("rakennehierarkia-ei-saa-sisaltaa-tutkinnossa-maariteltavia-ryhmia-joihin-liitetty-osia", nimi, syvyys));
            }
        }

        if (validointi.sisakkaisiaOsaamisalaryhmia > 1) {
            validointi.sisakkaisiaOsaamisalaryhmia = 1;
            validointi.ongelmat.add(new Ongelma("rakenteessa-sisakkaisia-osaamisalaryhmia", nimi, syvyys));
        }

        if (rooli == RakenneModuuliRooli.NORMAALI && uniikit.size() + ryhmienMaara != osat.size()) {
            validointi.ongelmat.add(new Ongelma("ryhmassa-on-samoja-tutkinnon-osia", nimi, syvyys));
        }

        // Tutkintonimike- ja osaamisalaryhmillä täytyy olla sisältöä
        if (rooli == RakenneModuuliRooli.OSAAMISALA || rooli == RakenneModuuliRooli.TUTKINTONIMIKE) {
            if (rakenne.getOsat().isEmpty()) {
                validointi.ongelmat.add(new Ongelma("tutkintonimikkeelta-tai-osaamisalalta-puuttuu-sisalto", nimi, syvyys));
            }
        }

        if (ms != null) {
            final Integer kokoMin = ms.kokoMinimi();
            final Integer kokoMax = ms.kokoMaksimi();
            final BigDecimal laajuusMin = new BigDecimal(ms.laajuusMinimi());
            final BigDecimal laajuusMax = new BigDecimal(ms.laajuusMaksimi());

            if (rooli == RakenneModuuliRooli.NORMAALI) {
                if (laajuusSummaMin.compareTo(laajuusMin) < 0) {
                    validointi.ongelmat.add(new Ongelma("laskettu-laajuuksien-summan-minimi-on-pienempi-kuin-ryhman-vaadittu-minimi", nimi, syvyys));
                } else if (laajuusSummaMax.compareTo(laajuusMax) < 0) {
                    validointi.ongelmat.add(new Ongelma("laskettu-laajuuksien-summan-maksimi-on-pienempi-kuin-ryhman-vaadittu-maksimi", nimi, syvyys));
                }

                if (osat.size() < kokoMin) {
                    validointi.ongelmat.add(new Ongelma("laskettu-koko-on-pienempi-kuin-vaadittu-minimi", nimi, syvyys));
                } else if (osat.size() < kokoMax) {
                    validointi.ongelmat.add(new Ongelma("laskettu-koko-on-pienempi-kuin-ryhman-vaadittu-maksimi", nimi, syvyys));
                }
                validointi.laskettuLaajuus = ms.laajuusMaksimi() != null && ms.laajuusMaksimi() > 0 ? laajuusMax : laajuusSummaMax;
            } else if (rooli == RakenneModuuliRooli.VIRTUAALINEN) {
                if (laajuusMax.compareTo(BigDecimal.valueOf(Integer.MIN_VALUE)) != 0) {
                    validointi.laskettuLaajuus = laajuusMax;
                } else if (laajuusMin.compareTo(BigDecimal.valueOf(Integer.MIN_VALUE)) != 0) {
                    validointi.laskettuLaajuus = laajuusMin;
                } else {
                    validointi.laskettuLaajuus = laajuusMax;
                }
            } else {
                if (laajuusMax.compareTo(BigDecimal.valueOf(Integer.MIN_VALUE)) != 0) {
                    validointi.laskettuLaajuus = laajuusMax;
                } else {
                    validointi.laskettuLaajuus = laajuusMin;
                }
            }
        }
        return validointi;
    }

    private static void validoiOsaamisala(ValidointiState ctx, RakenneModuuli rakenne, int syvyys, TekstiPalanen nimi, Validointi validointi) {
        validointi.sisakkaisiaOsaamisalaryhmia = validointi.sisakkaisiaOsaamisalaryhmia + 1;
        // Tarkista löytyykö perusteesta valittua osaamisalaa
        Koodi roa = rakenne.getOsaamisala();
        if (roa != null) {
            boolean osaamisalaaEiPerusteella = true;
            for (Koodi oa : ctx.getContext().getOsaamisalat()) {
                if (roa != null && roa.getUri() != null && oa != null && roa.getUri().equals(oa.getUri())) {
                    osaamisalaaEiPerusteella = false;
                    break;
                }
            }

            if (osaamisalaaEiPerusteella) {
                validointi.ongelmat.add(new Ongelma("ryhman-osaamisalaa-ei-perusteella", nimi, syvyys));
            }
        }
    }
}
