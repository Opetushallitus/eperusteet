package fi.vm.sade.eperusteet.service.impl.navigationpublic;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tuva.KoulutuksenOsaDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.NavigationBuilderPublic;
import fi.vm.sade.eperusteet.service.PerusteDispatcher;
import fi.vm.sade.eperusteet.service.PerusteService;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class NavigationBuilderPublicTuva extends NavigationBuilderPublicDefault {

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.TUTKINTOONVALMENTAVA);
    }

    @Override
    public LokalisoituTekstiDto getPerusteenOsaNimi(PerusteenOsaDto perusteenOsaDto) {
        if(perusteenOsaDto != null) {
            if (perusteenOsaDto instanceof KoulutuksenOsaDto) {
                KoulutuksenOsaDto koulutuksenOsaDto = (KoulutuksenOsaDto) perusteenOsaDto;
                if (koulutuksenOsaDto.getNimiKoodi() != null) {
                    return koulutuksenOsaDto.getNimiKoodi().getNimi();
                }
            }

            return perusteenOsaDto.getNimi();
        }

        return null;
    }
}
