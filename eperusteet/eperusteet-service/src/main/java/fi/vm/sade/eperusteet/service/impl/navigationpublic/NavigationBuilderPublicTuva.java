package fi.vm.sade.eperusteet.service.impl.navigationpublic;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tuva.KoulutuksenOsaDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.PerusteService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@Transactional
public class NavigationBuilderPublicTuva extends NavigationBuilderPublicDefault {

    public NavigationBuilderPublicTuva(PerusteService perusteService) {
        super(perusteService);
    }

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
