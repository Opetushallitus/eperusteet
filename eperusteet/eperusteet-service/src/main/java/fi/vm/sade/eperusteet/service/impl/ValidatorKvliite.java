package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteJulkinenDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.HashSet;
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
    public TilaUpdateStatus validate(Long perusteprojektiId) {
        Perusteprojekti projekti = perusteprojektiRepository.findOne(perusteprojektiId);
        KVLiiteJulkinenDto julkinenKVLiite = perusteService.getJulkinenKVLiite(projekti.getPeruste().getId());
        Set<Kieli> vaaditutKielet = new HashSet<Kieli>() {{
            add(Kieli.FI);
            add(Kieli.SV);
            add(Kieli.EN);
        }};
        TilaUpdateStatus result = new TilaUpdateStatus();
        tarkistaKvliite(julkinenKVLiite, vaaditutKielet, result);
        return result;
    }

    @Override
    public boolean applicableKoulutustyyppi(KoulutusTyyppi tyyppi) {
        return tyyppi != null && tyyppi.isAmmatillinen();
    }

    @Override
    public boolean applicableTila(ProjektiTila tila) {
        return tila.isOneOf(ProjektiTila.JULKAISTU, ProjektiTila.VIIMEISTELY);
    }

    @Override
    public boolean applicablePerustetyyppi(PerusteTyyppi tyyppi) {
        return tyyppi.isOneOf(PerusteTyyppi.NORMAALI);
    }

    private void tarkistaKvliite(KVLiiteJulkinenDto julkinenKVLiite, Set<Kieli> vaaditutKielet, TilaUpdateStatus updateStatus) {

        Map<String, Set<Kieli>> virheellisetKielet = new HashMap<>();
        tarkistaLokalisoituTekstiDto("kvliite-validointi-suorittaneen-osaaminen",
                julkinenKVLiite.getSuorittaneenOsaaminen(), vaaditutKielet, virheellisetKielet);
        tarkistaLokalisoituTekstiDto("kvliite-validointi-tyotehtavat-joissa-voi-toimia",
                julkinenKVLiite.getTyotehtavatJoissaVoiToimia(), vaaditutKielet, virheellisetKielet);
        if (julkinenKVLiite.getArvosanaAsteikko() == null) {
            updateStatus.setVaihtoOk(false);
            updateStatus.addStatus("kvliite-validointi-arvosana-asteikko");
        }
        tarkistaLokalisoituTekstiDto("kvliite-validointi-jatkoopinto-kelpoisuus",
                julkinenKVLiite.getJatkoopintoKelpoisuus(), vaaditutKielet, virheellisetKielet);
        tarkistaLokalisoituTekstiDto("kvliite-validointi-kansainvaliset-sopimukset",
                julkinenKVLiite.getKansainvalisetSopimukset(), vaaditutKielet, virheellisetKielet);
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
            updateStatus.setVaihtoOk(false);
            updateStatus.addStatus("kvliite-validointi-tasot");
        }
        Map<Suoritustapakoodi, LokalisoituTekstiDto> muodostumisenKuvaus = julkinenKVLiite.getMuodostumisenKuvaus();
        if (!ObjectUtils.isEmpty(muodostumisenKuvaus)) {
            muodostumisenKuvaus.forEach((st, kuvaus)
                    -> tarkistaLokalisoituTekstiDto("kvliite-validointi-muodostumisen-kuvaus-" + st,
                    kuvaus, vaaditutKielet, virheellisetKielet));
        }

        virheellisetKielet.forEach((viesti, kielet) -> {
            updateStatus.setVaihtoOk(false);
            updateStatus.addStatus(viesti, kielet);
        });
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
