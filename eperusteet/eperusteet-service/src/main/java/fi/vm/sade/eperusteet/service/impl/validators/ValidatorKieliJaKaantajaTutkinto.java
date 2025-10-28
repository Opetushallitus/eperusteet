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

import java.util.*;

import static fi.vm.sade.eperusteet.domain.TekstiPalanen.tarkistaTekstipalanen;

@Component
@Transactional
@Slf4j
public class ValidatorKieliJaKaantajaTutkinto implements Validator {

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private PerusteRepository perusteRepository;

    @Override
    public List<Validointi> validate(Long perusteprojektiId, ProjektiTila targetTila) {
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

        tarkistaPerusteenSisaltoTekstipalaset(projekti.getPeruste(), sisaltoValidointi);
        return Arrays.asList(perusteValidointi, sisaltoValidointi);
    }

    @Transactional(readOnly = true)
    public void tarkistaPerusteenSisaltoTekstipalaset(Peruste peruste, Validointi validointi) {
        Set<Kieli> vaaditutKielet = peruste.getKielet();

        if (peruste.getKieliJaKaantajaTutkintoPerusteenSisalto() != null) {
            for (PerusteenOsaViite lapsi : peruste.getKieliJaKaantajaTutkintoPerusteenSisalto().getSisalto().getLapset()) {
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
        return PerusteTyyppi.KIELI_KAANTAJA_TUTKINTO.equals(tyyppi);
    }
}

