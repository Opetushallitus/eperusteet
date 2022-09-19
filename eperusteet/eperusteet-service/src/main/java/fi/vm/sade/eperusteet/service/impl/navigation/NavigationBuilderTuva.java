package fi.vm.sade.eperusteet.service.impl.navigation;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.tuva.KoulutuksenOsa;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class NavigationBuilderTuva extends NavigationBuilderDefault {

    public NavigationBuilderTuva(@Dto DtoMapper mapper, PerusteRepository perusteRepository) {
        super(mapper, perusteRepository);
    }

    @Override
    public Set<KoulutustyyppiToteutus> getTyypit() {
        return Sets.newHashSet(KoulutustyyppiToteutus.TUTKINTOONVALMENTAVA);
    }

    @Override
    public LokalisoituTekstiDto getPerusteenOsaNimi(DtoMapper mapper, PerusteenOsa perusteenOsa) {
        if (perusteenOsa != null) {
            if (perusteenOsa instanceof KoulutuksenOsa) {
                KoulutuksenOsa koulutuoksenOsa = (KoulutuksenOsa) perusteenOsa;
                if (koulutuoksenOsa.getNimiKoodi() != null) {
                    KoodiDto koodiDto = mapper.map(koulutuoksenOsa.getNimiKoodi(), KoodiDto.class);
                    return koodiDto.getNimi();
                }
            }

            return mapper.map(perusteenOsa, PerusteenOsaDto.class).getNimi();
        }

        return null;
    }
}
