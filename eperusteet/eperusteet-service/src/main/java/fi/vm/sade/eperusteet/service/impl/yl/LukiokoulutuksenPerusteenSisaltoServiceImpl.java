/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukioOppiaineOppimaaraNodeDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukioOppiainePuuDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.julkinen.LukiokurssiJulkisetTiedotDto;
import fi.vm.sade.eperusteet.repository.LukiokoulutuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.repository.LukiokurssiRepository;
import fi.vm.sade.eperusteet.repository.OppiaineRepository;
import fi.vm.sade.eperusteet.service.LokalisointiService;
import fi.vm.sade.eperusteet.service.yl.LukiokoulutuksenPerusteenSisaltoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static fi.vm.sade.eperusteet.service.util.OptionalUtil.found;
import static java.util.stream.Collectors.toMap;

/**
 * User: tommiratamaa
 * Date: 21.9.15
 * Time: 15.33
 */
@Service
public class LukiokoulutuksenPerusteenSisaltoServiceImpl
        extends AbstractOppiaineOpetuksenSisaltoService<LukiokoulutuksenPerusteenSisalto>
        implements LukiokoulutuksenPerusteenSisaltoService {
    @Autowired
    private LukiokurssiRepository lukiokurssiRepository;

    @Autowired
    private OppiaineRepository oppiaineRepository;

    @Autowired
    private LokalisointiService lokalisointiService;

    @Autowired
    private LukiokoulutuksenPerusteenSisaltoRepository sisaltoRepository;

    @Override
    protected LukiokoulutuksenPerusteenSisalto getByPerusteId(Long perusteId) {
        LukiokoulutuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        assertExists(sisalto, "Sisaltoä annetulle perusteelle ei ole olemassa");
        return sisalto;
    }

    @Override
    @Transactional(readOnly = true)
    public LukioOppiainePuuDto getOppiaineTreeStructure(long perusteId) {
        found(sisaltoRepository.findLukioperusteenTilaByPerusteId(perusteId),
                t -> t != PerusteTila.POISTETTU);
        LukioOppiainePuuDto tree = new LukioOppiainePuuDto(perusteId);
        List<LukiokurssiJulkisetTiedotDto> kurssit = lukiokurssiRepository.findLukiokurssiJulkinenDtosByPerusteId(perusteId);
        List<LukioOppiaineOppimaaraNodeDto> oppiaineet = oppiaineRepository.findLukioOppiaineetJulkinenDtosByPerusteId(perusteId);
        Map<Long, LukioOppiaineOppimaaraNodeDto> oppiaineById = oppiaineet.stream()
                .collect(toMap(LukioOppiaineOppimaaraNodeDto::getId, n -> n));
        oppiaineet.stream().filter(oa -> oa.getParentId() != null)
                .forEach(oa -> oppiaineById.get(oa.getParentId()).getOppimaarat().add(oa));
        kurssit.stream().forEach(k -> oppiaineById.get(k.getOppiaineId()).getKurssit().add(k));
        oppiaineet.stream().filter(oa -> oa.getParentId() == null).forEach(oa -> tree.getOppiaineet().add(oa));
        return lokalisointiService.lokalisoi(tree);
    }
}
