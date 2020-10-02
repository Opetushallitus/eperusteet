package fi.vm.sade.eperusteet.service.mapping;

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.vst.Opintokokonaisuus;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.vst.OpintokokonaisuusDto;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OpintokokonaisuusMapper extends CustomMapper<OpintokokonaisuusDto, Opintokokonaisuus>{

    @Autowired
    private KoodistoClient koodistoClient;

    @Override
    public void mapAtoB(OpintokokonaisuusDto opintokokonaisuusDto, Opintokokonaisuus opintokokonaisuus, MappingContext context) {
        super.mapAtoB(opintokokonaisuusDto, opintokokonaisuus, context);

        List<KoodiDto> tallentamattomat = opintokokonaisuusDto.getOpetuksenTavoitteet()
                .stream().filter(tavoite -> tavoite.getUri() == null)
                .collect(Collectors.toList());

        Stack<Long> koodiStack = new Stack<>();
        koodiStack.addAll(koodistoClient.nextKoodiId(KoodistoUriArvo.OPINTOKOKONAISUUSTAVOITTEET, tallentamattomat.size()));

        List<Koodi> koodit = new ArrayList<>();
        for (KoodiDto tavoite : opintokokonaisuusDto.getOpetuksenTavoitteet()) {

            if (tavoite.getUri() == null) {
                LokalisoituTekstiDto lokalisoituTekstiDto = new LokalisoituTekstiDto(tavoite.getNimi());
                KoodistoKoodiDto lisattyKoodi = koodistoClient.addKoodiNimella(KoodistoUriArvo.OPINTOKOKONAISUUSTAVOITTEET, lokalisoituTekstiDto, koodiStack.pop());

                if (lisattyKoodi == null) {
                    log.error("Koodin lisääminen epäonnistui {} {}", lokalisoituTekstiDto, lisattyKoodi);
                    continue;
                }

                tavoite.setKoodisto(lisattyKoodi.getKoodisto().getKoodistoUri());
                tavoite.setUri(lisattyKoodi.getKoodiUri());
                tavoite.setVersio(lisattyKoodi.getVersio() != null ? Long.valueOf(lisattyKoodi.getVersio()) : null);
            }

            Koodi koodi = new Koodi();
            BeanUtils.copyProperties(tavoite, koodi);
            koodit.add(koodi);
        }

        opintokokonaisuus.setOpetuksenTavoitteet(koodit);
    }

    @Override
    public void mapBtoA(Opintokokonaisuus source, OpintokokonaisuusDto target, MappingContext context) {
        super.mapBtoA(source, target, context);
    }
}
