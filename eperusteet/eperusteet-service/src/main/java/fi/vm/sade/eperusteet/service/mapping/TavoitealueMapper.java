package fi.vm.sade.eperusteet.service.mapping;

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.vst.TavoiteAlue;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.vst.TavoiteAlueDto;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
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
public class TavoitealueMapper extends CustomMapper<TavoiteAlueDto, TavoiteAlue> {

    @Autowired
    private KoodistoClient koodistoClient;

    @Override
    public void mapAtoB(TavoiteAlueDto tavoitealueDto, TavoiteAlue tavoitealue, MappingContext context) {
        super.mapAtoB(tavoitealueDto, tavoitealue, context);

        if (CollectionUtils.isNotEmpty(tavoitealueDto.getTavoitteet())) {
            List<KoodiDto> tallentamattomat = tavoitealueDto.getTavoitteet()
                    .stream().filter(tavoite -> tavoite.getUri() == null)
                    .collect(Collectors.toList());

            Stack<Long> koodiStack = new Stack<>();
            koodiStack.addAll(koodistoClient.nextKoodiId(KoodistoUriArvo.TAVOITTEETLUKUTAIDOT, tallentamattomat.size()));

            List<Koodi> koodit = new ArrayList<>();
            for (KoodiDto tavoite : tavoitealueDto.getTavoitteet()) {

                if (tavoite.getUri() == null) {
                    KoodistoKoodiDto lisattyKoodi = koodistoClient.addKoodiNimella(KoodistoUriArvo.TAVOITTEETLUKUTAIDOT, tavoite.getNimi(), koodiStack.pop());

                    if (lisattyKoodi == null) {
                        log.error("Koodin lisääminen epäonnistui {} {}", tavoite.getNimi(), tavoite.getNimi());
                        throw new BusinessRuleViolationException("tavoitteen-lisaaminen-epaonnistui");
                    }

                    tavoite.setKoodisto(lisattyKoodi.getKoodisto().getKoodistoUri());
                    tavoite.setUri(lisattyKoodi.getKoodiUri());
                    tavoite.setVersio(lisattyKoodi.getVersio() != null ? Long.valueOf(lisattyKoodi.getVersio()) : null);
                }

                Koodi koodi = new Koodi();
                BeanUtils.copyProperties(tavoite, koodi);
                koodit.add(koodi);
            }

            tavoitealue.setTavoitteet(koodit);
        }
    }

    @Override
    public void mapBtoA(TavoiteAlue source, TavoiteAlueDto target, MappingContext context) {
        super.mapBtoA(source, target, context);
    }
}
