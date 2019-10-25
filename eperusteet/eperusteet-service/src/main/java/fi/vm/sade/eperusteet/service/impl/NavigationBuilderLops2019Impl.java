package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.service.NavigationBuilder;
import fi.vm.sade.eperusteet.service.PerusteprojektiQualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@PerusteprojektiQualifier(KoulutustyyppiToteutus.LOPS2019)
public class NavigationBuilderLops2019Impl implements NavigationBuilder {


    @Override
    public NavigationNodeDto buildNavigation(Peruste peruste) {
        return null;
    }
}
