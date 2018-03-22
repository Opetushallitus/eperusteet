/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuliRooli;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneOsaDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;

@Transactional
@DirtiesContext
public class PerusteenRakenneIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    @LockCtx(TutkinnonRakenneLockContext.class)
    private LockService<TutkinnonRakenneLockContext> lockService;

    @Test
    public void testValidoiTutkinnossaMaariteltavatRyhmat() {

        PerusteprojektiLuontiDto ppldto = new PerusteprojektiLuontiDto(KoulutusTyyppi.PERUSTUTKINTO.toString(),
                LaajuusYksikko.OSAAMISPISTE, null, null, PerusteTyyppi.NORMAALI, "1.2.246.562.28.11287634288");
        ppldto.setNimi(TestUtils.uniikkiString());
        ppldto.setDiaarinumero(TestUtils.uniikkiString());
        ppldto.setReforminMukainen(true);
        PerusteprojektiDto perusteprojektiDto = perusteprojektiService.save(ppldto);

        Perusteprojekti pp = perusteprojektiRepository.findOne(perusteprojektiDto.getId());
        Peruste peruste = pp.getPeruste();

        final TutkinnonRakenneLockContext ctx = TutkinnonRakenneLockContext.of(peruste.getId(), Suoritustapakoodi.REFORMI);
        lockService.lock(ctx);

        perusteService.addSisalto(pp.getPeruste().getId(), Suoritustapakoodi.REFORMI, null);


        peruste.getSuoritustavat().forEach(suoritustapa -> {
            assertThatThrownBy(() -> {
                RakenneModuuliDto dto = mapper.map(suoritustapa.getRakenne(), RakenneModuuliDto.class);
                dto.setRooli(RakenneModuuliRooli.VIRTUAALINEN);
                RakenneOsaDto rakenneOsaDto = new RakenneOsaDto();
                ArrayList<AbstractRakenneOsaDto> osat = new ArrayList<>();
                osat.add(rakenneOsaDto);
                dto.setOsat(osat);

                perusteService.updateTutkinnonRakenne(peruste.getId(), suoritustapa.getSuoritustapakoodi(), dto);
            }).isInstanceOf(BusinessRuleViolationException.class)
            .hasMessage("Rakennehierarkia ei saa sisältää tutkinnossa määriteltäviä ryhmiä, joihin liitetty osia");
        });

        lockService.unlock(ctx);
    }
}
