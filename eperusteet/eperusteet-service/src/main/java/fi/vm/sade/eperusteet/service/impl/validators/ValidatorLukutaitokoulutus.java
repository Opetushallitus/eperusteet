package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.lukutaitokoulutus.LukutaitokoulutusTavoitealue;
import fi.vm.sade.eperusteet.domain.lukutaitokoulutus.LukutaitokoulutusTavoitteetJaSisaltoalueet;
import fi.vm.sade.eperusteet.dto.ValidointiKategoria;
import fi.vm.sade.eperusteet.dto.util.NavigableLokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.Validator;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.util.Validointi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fi.vm.sade.eperusteet.domain.TekstiPalanen.tarkistaTekstipalanen;

@Component
@Transactional
@Slf4j
public class ValidatorLukutaitokoulutus implements Validator {

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Override
    public List<Validointi> validate(Long perusteprojektiId, ProjektiTila targetTila) {
        Perusteprojekti projekti = perusteprojektiRepository.findById(perusteprojektiId).orElse(null);

        if (projekti == null) {
            throw new BusinessRuleViolationException("Projektia ei ole olemassa id:llä: " + perusteprojektiId);
        }

        Validointi sisaltoValidointi = new Validointi(ValidointiKategoria.KIELISISALTO);
        tarkistaPerusteenSisaltoTekstipalaset(projekti.getPeruste(), sisaltoValidointi);
        return List.of(sisaltoValidointi);
    }

    private void tarkistaPerusteenSisaltoTekstipalaset(Peruste peruste, Validointi validointi) {
        if (peruste.getLukutaitokoulutusSisalto() == null || peruste.getLukutaitokoulutusSisalto().getSisalto() == null) {
            return;
        }

        Set<Kieli> vaaditutKielet = peruste.getKielet();
        for (PerusteenOsaViite lapsi : peruste.getLukutaitokoulutusSisalto().getSisalto().getLapset()) {
            tarkistaSisalto(lapsi, vaaditutKielet, validointi);
        }
    }

    private void tarkistaSisalto(PerusteenOsaViite viite, Set<Kieli> pakolliset, Validointi validointi) {
        PerusteenOsa perusteenOsa = viite.getPerusteenOsa();

        if (perusteenOsa instanceof LukutaitokoulutusTavoitteetJaSisaltoalueet tavoitteetJaSisaltoalueet) {
            tarkistaTavoitteetJaSisaltoalueet(tavoitteetJaSisaltoalueet, pakolliset, validointi);
        }

        for (PerusteenOsaViite lapsi : viite.getLapset()) {
            tarkistaSisalto(lapsi, pakolliset, validointi);
        }
    }

    private void tarkistaTavoitteetJaSisaltoalueet(LukutaitokoulutusTavoitteetJaSisaltoalueet tavoitteetJaSisaltoalueet,
                                                   Set<Kieli> pakolliset, Validointi validointi) {
        Map<String, String> virheellisetKielet = new HashMap<>();

        tarkistaTekstipalanen("peruste-validointi-lukutaitokoulutus-tavoitteet-ja-sisaltoalueet-nimi",
                tavoitteetJaSisaltoalueet.getNimi(), pakolliset, virheellisetKielet, true);

        for (LukutaitokoulutusTavoitealue tavoitealue : tavoitteetJaSisaltoalueet.getTavoitealueet()) {
            tarkistaTekstipalanen("peruste-validointi-lukutaitokoulutus-tavoitealue-otsikko",
                    tavoitealue.getNimi(), pakolliset, virheellisetKielet, true);

            for (TekstiPalanen tavoite : tavoitealue.getTavoitteet()) {
                tarkistaTekstipalanen("peruste-validointi-lukutaitokoulutus-tavoitealue-tavoitteet",
                        tavoite, pakolliset, virheellisetKielet, true);
            }
        }

        for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
            validointi.virhe(entry.getKey(), new NavigableLokalisoituTekstiDto(tavoitteetJaSisaltoalueet).getNavigationNode());
        }
    }

    @Override
    public boolean applicableKoulutustyyppi(KoulutusTyyppi tyyppi) {
        return KoulutusTyyppi.LUKUTAITOKOULUTUS.equals(tyyppi);
    }

    @Override
    public boolean applicableToteutus(KoulutustyyppiToteutus toteutus) {
        return true;
    }
}
