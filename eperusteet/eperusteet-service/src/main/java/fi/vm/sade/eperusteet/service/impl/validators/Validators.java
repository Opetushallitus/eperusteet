package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.lops2019.Koodillinen;
import fi.vm.sade.eperusteet.domain.yl.Nimetty;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Validators {

    @Autowired
    @Dto
    private DtoMapper mapper;

    <T extends Koodillinen & Nimetty> void validKoodi(T koodillinen, TilaUpdateStatus status, String kohde, String expectedKoodisto) {
        Koodi koodi = koodillinen.getKoodi();
        if (koodi == null) {
            status.addStatus(
                    kohde + "-puuttuva-koodi",
                    Suoritustapakoodi.LUKIOKOULUTUS2019,
                    mapper.map(koodillinen.getNimi(), LokalisoituTekstiDto.class));
            status.setVaihtoOk(false);
        }
        else if (!expectedKoodisto.equals(koodi.getKoodisto())) {
            status.addStatus(kohde + "-virheellinen-koodisto");
            status.setVaihtoOk(false);
        }
    }
}
