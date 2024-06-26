package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.digi.Osaamiskokonaisuus;
import fi.vm.sade.eperusteet.domain.digi.OsaamiskokonaisuusKasitteisto;
import fi.vm.sade.eperusteet.domain.digi.OsaamiskokonaisuusPaaAlue;
import fi.vm.sade.eperusteet.dto.ValidointiKategoria;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.util.NavigableLokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.Validator;
import fi.vm.sade.eperusteet.service.util.Validointi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fi.vm.sade.eperusteet.domain.TekstiPalanen.tarkistaTekstipalanen;

@Component
@Transactional
@Slf4j
public class ValidatorDigitaalinenOsaaminen implements Validator {

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private PerusteRepository perusteRepository;

    @Override
    public List<Validointi> validate(Long perusteprojektiId, ProjektiTila targetTila) {
        List<Validointi> validoinnit = new ArrayList<>();

        Perusteprojekti projekti = perusteprojektiRepository.findById(perusteprojektiId).orElse(null);

        Validointi perusteValidointi = new Validointi(ValidointiKategoria.PERUSTE);
        Validointi sisaltoValidointi = new Validointi(ValidointiKategoria.KIELISISALTO);


        boolean hasNimiKaikillaKielilla = projekti.getPeruste().getKielet().stream()
                .allMatch(kieli -> projekti.getPeruste().getNimi().getTeksti() != null
                        && projekti.getPeruste().getNimi().getTeksti().containsKey(kieli));
        if (!hasNimiKaikillaKielilla) {
            perusteValidointi.virhe("perusteen-nimea-ei-ole-kaikilla-kielilla", NavigationNodeDto.of(NavigationType.tiedot));
        }
        if (projekti.getPeruste().getVoimassaoloAlkaa() == null) {
            perusteValidointi.virhe("peruste-ei-voimassaolon-alkamisaikaa", NavigationNodeDto.of(NavigationType.tiedot));
        }

        List<Peruste> julkaistutDigitaaliset = perusteRepository.findJulkaistutVoimassaolevatPerusteetByTyyppi(PerusteTyyppi.DIGITAALINEN_OSAAMINEN);
        if (julkaistutDigitaaliset.size() > 0 && julkaistutDigitaaliset.stream().noneMatch(julkaistu -> julkaistu.getId().equals(projekti.getPeruste().getId()))) {
            perusteValidointi.virhe("digitaalinen-osaaminen-jo-julkaistu", NavigationNodeDto.of(NavigationType.tiedot));
        }

        tarkistaPerusteenSisaltoTekstipalaset(projekti.getPeruste(), sisaltoValidointi);
        return validoinnit;
    }

    @Transactional(readOnly = true)
    public void tarkistaPerusteenSisaltoTekstipalaset(Peruste peruste, Validointi validointi) {
        Set<Kieli> vaaditutKielet = peruste.getKielet();

        if (peruste.getDigitaalinenOsaaminenSisalto() != null) {
            for (PerusteenOsaViite lapsi : peruste.getDigitaalinenOsaaminenSisalto().getSisalto().getLapset()) {
                tarkistaSisalto(lapsi, vaaditutKielet, validointi);
            }
        }
    }

    @Transactional(readOnly = true)
    public void tarkistaSisalto(final PerusteenOsaViite viite, final Set<Kieli> pakolliset, Validointi validointi) {
        PerusteenOsa perusteenOsa = viite.getPerusteenOsa();
        if (perusteenOsa instanceof TekstiKappale && (perusteenOsa.getTunniste() == PerusteenOsaTunniste.NORMAALI || perusteenOsa.getTunniste() == null)) {
            TekstiKappale tekstikappale = (TekstiKappale) perusteenOsa;
            Map<String, String> virheellisetKielet = new HashMap<>();
            tarkistaTekstipalanen("peruste-validointi-tekstikappale-nimi", tekstikappale.getNimi(), pakolliset, virheellisetKielet, true);
            tarkistaTekstipalanen("peruste-validointi-tekstikappale-teksti", tekstikappale.getTeksti(), pakolliset, virheellisetKielet, true);

            for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
                validointi.virhe(entry.getKey(), new NavigableLokalisoituTekstiDto(tekstikappale).getNavigationNode());
            }
        }

        if (perusteenOsa instanceof Osaamiskokonaisuus) {
            Osaamiskokonaisuus osaamiskokonaisuus = (Osaamiskokonaisuus) perusteenOsa;
            Map<String, String> virheellisetKielet = new HashMap<>();
            tarkistaTekstipalanen("peruste-validointi-osaamiskokonaisuus-nimi", osaamiskokonaisuus.getNimi(), pakolliset, virheellisetKielet, true);
            tarkistaTekstipalanen("peruste-validointi-osaamiskokonaisuus-kuvaus", osaamiskokonaisuus.getKuvaus(), pakolliset, virheellisetKielet, true);

            for (OsaamiskokonaisuusKasitteisto kasitteisto : osaamiskokonaisuus.getKasitteistot()) {
                tarkistaTekstipalanen("peruste-validointi-osaamiskokonaisuus-kasitteisto-kuvaus", kasitteisto.getKuvaus(), pakolliset, virheellisetKielet, true);
            }

            for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
                validointi.virhe(entry.getKey(), new NavigableLokalisoituTekstiDto(osaamiskokonaisuus).getNavigationNode());
            }
        }

        if (perusteenOsa instanceof OsaamiskokonaisuusPaaAlue) {
            OsaamiskokonaisuusPaaAlue osaamiskokonaisuusPaaAlue = (OsaamiskokonaisuusPaaAlue) perusteenOsa;
            Map<String, String> virheellisetKielet = new HashMap<>();
            tarkistaTekstipalanen("peruste-validointi-osaamiskokonaisuus-paa-alue-nimi", osaamiskokonaisuusPaaAlue.getNimi(), pakolliset, virheellisetKielet, true);

            osaamiskokonaisuusPaaAlue.getOsaAlueet().forEach(osaAlue -> {
                tarkistaTekstipalanen("peruste-validointi-osaamiskokonaisuus-paa-alue-osa-alue-nimi", osaAlue.getNimi(), pakolliset, virheellisetKielet, true);

                osaAlue.getTasokuvaukset().forEach(tasokuvaus -> {
                    tasokuvaus.getEdelleenKehittyvatOsaamiset().forEach(kuvaus -> {
                        tarkistaTekstipalanen("peruste-validointi-osaamiskokonaisuus-paa-alue-osa-alue-kuvaus", osaAlue.getNimi(), pakolliset, virheellisetKielet, true);
                    });

                    tasokuvaus.getOsaamiset().forEach(kuvaus -> {
                        tarkistaTekstipalanen("peruste-validointi-osaamiskokonaisuus-paa-alue-osa-alue-kuvaus", osaAlue.getNimi(), pakolliset, virheellisetKielet, true);
                    });

                    tasokuvaus.getEdistynytOsaaminenKuvaukset().forEach(kuvaus -> {
                        tarkistaTekstipalanen("peruste-validointi-osaamiskokonaisuus-paa-alue-osa-alue-kuvaus", osaAlue.getNimi(), pakolliset, virheellisetKielet, true);
                    });
                });
            });

            for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
                validointi.virhe(entry.getKey(), new NavigableLokalisoituTekstiDto(osaamiskokonaisuusPaaAlue).getNavigationNode());

            }
        }

        for (PerusteenOsaViite lapsi : viite.getLapset()) {
            tarkistaSisalto(lapsi, pakolliset, validointi);
        }
    }

    @Override
    public boolean applicableKoulutustyyppi(KoulutusTyyppi tyyppi) {
        return true;
    }

    @Override
    public boolean applicableToteutus(KoulutustyyppiToteutus toteutus) {
        return true;
    }

    @Override
    public boolean applicablePerustetyyppi(PerusteTyyppi tyyppi) {
        return PerusteTyyppi.DIGITAALINEN_OSAAMINEN.equals(tyyppi);
    }
}
