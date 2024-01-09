package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.ValidointiKategoria;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteJulkinenDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.Validator;
import fi.vm.sade.eperusteet.service.util.Validointi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.tarkistaLokalisoituTekstiDto;

@Component
public class ValidatorKvliite implements Validator {

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private PerusteService perusteService;

    @Override
    public List<Validointi> validate(Long perusteprojektiId, ProjektiTila tila) {
        Perusteprojekti projekti = perusteprojektiRepository.findOne(perusteprojektiId);
        KVLiiteJulkinenDto julkinenKVLiite = perusteService.getJulkinenKVLiite(projekti.getPeruste().getId());
        Set<Kieli> vaaditutKielet = new HashSet<Kieli>() {{
            add(Kieli.FI);
            add(Kieli.SV);
            add(Kieli.EN);
        }};
        Validointi validointi = new Validointi(ValidointiKategoria.MAARITTELEMATON);
        tarkistaKvliite(julkinenKVLiite, vaaditutKielet, validointi);
        return Arrays.asList(validointi);
    }

    @Override
    public boolean applicableToteutus(KoulutustyyppiToteutus toteutus) {
        return true;
    }

    @Override
    public boolean applicableKoulutustyyppi(KoulutusTyyppi tyyppi) {
        return tyyppi != null && tyyppi.isAmmatillinen();
    }

    @Override
    public boolean applicablePerustetyyppi(PerusteTyyppi tyyppi) {
        return tyyppi.isOneOf(PerusteTyyppi.NORMAALI);
    }

    private void tarkistaKvliite(KVLiiteJulkinenDto julkinenKVLiite, Set<Kieli> vaaditutKielet, Validointi validointi) {

        Map<String, Set<Kieli>> virheellisetKielet = new HashMap<>();
        tarkistaLokalisoituTekstiDto("kvliite-validointi-suorittaneen-osaaminen",
                julkinenKVLiite.getSuorittaneenOsaaminen(), vaaditutKielet, virheellisetKielet);
        tarkistaLokalisoituTekstiDto("kvliite-validointi-tyotehtavat-joissa-voi-toimia",
                julkinenKVLiite.getTyotehtavatJoissaVoiToimia(), vaaditutKielet, virheellisetKielet);
        if (julkinenKVLiite.getArvosanaAsteikko() == null) {
            validointi.huomautukset("kvliite-validointi-arvosana-asteikko", NavigationNodeDto.of(NavigationType.kvliite));
        }
        tarkistaLokalisoituTekstiDto("kvliite-validointi-jatkoopinto-kelpoisuus",
                julkinenKVLiite.getJatkoopintoKelpoisuus(), vaaditutKielet, virheellisetKielet);
        tarkistaLokalisoituTekstiDto("kvliite-validointi-saados-perusta",
                julkinenKVLiite.getSaadosPerusta(), vaaditutKielet, virheellisetKielet);
        tarkistaLokalisoituTekstiDto("kvliite-validointi-pohjakoulutusvaatimukset",
                julkinenKVLiite.getPohjakoulutusvaatimukset(), vaaditutKielet, virheellisetKielet);
        tarkistaLokalisoituTekstiDto("kvliite-validointi-lisatietoja",
                julkinenKVLiite.getLisatietoja(), vaaditutKielet, virheellisetKielet);
        tarkistaLokalisoituTekstiDto("kvliite-validointi-tutkintotodistuksen-saaminen",
                julkinenKVLiite.getTutkintotodistuksenSaaminen(), vaaditutKielet, virheellisetKielet);
        tarkistaLokalisoituTekstiDto("kvliite-validointi-tutkinnosta-paattava-viranomainen",
                julkinenKVLiite.getTutkinnostaPaattavaViranomainen(), vaaditutKielet, virheellisetKielet);
        tarkistaLokalisoituTekstiDto("kvliite-validointi-nimi",
                julkinenKVLiite.getNimi(), vaaditutKielet, virheellisetKielet);
        if (ObjectUtils.isEmpty(julkinenKVLiite.getTasot())) {
            validointi.huomautukset("kvliite-validointi-tasot", NavigationNodeDto.of(NavigationType.kvliite));
        }
        Map<Suoritustapakoodi, LokalisoituTekstiDto> muodostumisenKuvaus = julkinenKVLiite.getMuodostumisenKuvaus();
        if (!ObjectUtils.isEmpty(muodostumisenKuvaus)) {
            muodostumisenKuvaus.forEach((st, kuvaus)
                    -> tarkistaLokalisoituTekstiDto("kvliite-validointi-muodostumisen-kuvaus-" + st,
                    kuvaus, vaaditutKielet, virheellisetKielet));
        }

        virheellisetKielet.forEach((viesti, kielet) -> {
            validointi.huomautukset(viesti, NavigationNodeDto.of(NavigationType.kvliite));
        });
    }
}
