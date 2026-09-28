package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.vst.Opintokokonaisuus;
import fi.vm.sade.eperusteet.dto.ValidointiKategoria;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.util.NavigableLokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.Validator;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.Validointi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static fi.vm.sade.eperusteet.domain.TekstiPalanen.tarkistaTekstipalanen;

@Component
@Transactional
@Slf4j
public class ValidatorVapaaSivistystyo implements Validator {

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    @Dto
    private DtoMapper mapper;

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
        if (peruste.getVstSisalto() == null || peruste.getVstSisalto().getSisalto() == null) {
            return;
        }

        Set<Kieli> vaaditutKielet = peruste.getKielet();
        for (PerusteenOsaViite lapsi : peruste.getVstSisalto().getSisalto().getLapset()) {
            tarkistaSisalto(lapsi, vaaditutKielet, validointi);
        }
    }

    private void tarkistaSisalto(PerusteenOsaViite viite, Set<Kieli> pakolliset, Validointi validointi) {
        PerusteenOsa perusteenOsa = viite.getPerusteenOsa();

        if (perusteenOsa instanceof Opintokokonaisuus opintokokonaisuus) {
            tarkistaOpintokokonaisuus(opintokokonaisuus, pakolliset, validointi);
        }

        for (PerusteenOsaViite lapsi : viite.getLapset()) {
            tarkistaSisalto(lapsi, pakolliset, validointi);
        }
    }

    private void tarkistaOpintokokonaisuus(Opintokokonaisuus opintokokonaisuus, Set<Kieli> pakolliset, Validointi validointi) {
        Map<String, String> virheellisetKielet = new HashMap<>();

        tarkistaTekstipalanen("peruste-validointi-opintokokonaisuus-nimi",
                opintokokonaisuus.getNimi(), pakolliset, virheellisetKielet, true);
        tarkistaTekstipalanen("peruste-validointi-opintokokonaisuus-kuvaus",
                opintokokonaisuus.getKuvaus(), pakolliset, virheellisetKielet, true);
        tarkistaTekstipalanen("peruste-validointi-opintokokonaisuus-opetuksen-tavoite-otsikko",
                opintokokonaisuus.getOpetuksenTavoiteOtsikko(), pakolliset, virheellisetKielet, true);

        if (opintokokonaisuus.getOpetuksenTavoitteet().isEmpty()) {
            virheellisetKielet.put("peruste-validointi-opintokokonaisuus-opetuksen-tavoitteet", null);
        }
        for (Koodi tavoite : opintokokonaisuus.getOpetuksenTavoitteet()) {
            tarkistaTekstipalanen("peruste-validointi-opintokokonaisuus-opetuksen-tavoitteet",
                    koodinNimi(tavoite), pakolliset, virheellisetKielet, true);
        }

        if (opintokokonaisuus.getArvioinnit().isEmpty()) {
            virheellisetKielet.put("peruste-validointi-opintokokonaisuus-arvioinnit", null);
        }
        for (TekstiPalanen arviointi : opintokokonaisuus.getArvioinnit()) {
            tarkistaTekstipalanen("peruste-validointi-opintokokonaisuus-arvioinnit",
                    arviointi, pakolliset, virheellisetKielet, true);
        }

        for (Map.Entry<String, String> entry : virheellisetKielet.entrySet()) {
            validointi.virhe(entry.getKey(), new NavigableLokalisoituTekstiDto(opintokokonaisuus).getNavigationNode());
        }
    }

    private TekstiPalanen koodinNimi(Koodi koodi) {
        return Optional.ofNullable(mapper.map(koodi, KoodiDto.class))
                .map(KoodiDto::getNimi)
                .map(LokalisoituTekstiDto::getTekstit)
                .map(TekstiPalanen::of)
                .orElse(null);
    }

    @Override
    public boolean applicableKoulutustyyppi(KoulutusTyyppi tyyppi) {
        return KoulutusTyyppi.VAPAASIVISTYSTYO.equals(tyyppi);
    }

    @Override
    public boolean applicableToteutus(KoulutustyyppiToteutus toteutus) {
        return true;
    }
}
