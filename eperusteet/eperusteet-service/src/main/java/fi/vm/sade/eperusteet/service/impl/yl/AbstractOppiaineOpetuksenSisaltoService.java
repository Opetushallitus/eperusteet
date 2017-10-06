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

import fi.vm.sade.eperusteet.domain.yl.AbstractOppiaineOpetuksenSisalto;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineBaseDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.PerusteenOsaViiteService;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.OppiainePerusteenSisaltoService;

import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * User: tommiratamaa
 * Date: 21.9.15
 * Time: 15.37
 */
public abstract class AbstractOppiaineOpetuksenSisaltoService<EntityType extends AbstractOppiaineOpetuksenSisalto>
        implements OppiainePerusteenSisaltoService {

    @Autowired
    protected PerusteenOsaViiteService viiteService;

    @Autowired
    protected PerusteRepository perusteet;

    @Autowired
    @Dto
    protected DtoMapper mapper;

    protected abstract EntityType getByPerusteId(Long perusteId);

    @Override
    @Transactional(readOnly = true)
    public <T extends PerusteenOsaViiteDto<?>> T getSisalto(Long perusteId, Long sisaltoId, Class<T> view) {
        AbstractOppiaineOpetuksenSisalto sisalto = getByPerusteId(perusteId);
        return viiteService.getSisalto(perusteId, sisaltoId == null ? sisalto.getSisalto().getId() : sisaltoId, view);
    }

    @Override
    @Transactional
    public PerusteenOsaViiteDto.Matala addSisalto(Long perusteId, Long viiteId, PerusteenOsaViiteDto.Matala dto) {
        AbstractOppiaineOpetuksenSisalto sisalto = getByPerusteId(perusteId);
        assertExists(sisalto, "Pyydettyä perustetta ei ole olemassa");
        if (viiteId == null) {
            return viiteService.addSisalto(perusteId, sisalto.getSisalto().getId(), dto);
        } else {
            return viiteService.addSisalto(perusteId, viiteId, dto);
        }
    }

    @Override
    @Transactional
    public void removeSisalto(Long perusteId, Long viiteId) {
        getByPerusteId(perusteId);
        viiteService.removeSisalto(perusteId, viiteId);
    }

    @Override
    @Transactional(readOnly = true)
    public <T extends OppiaineBaseDto> List<T> getOppiaineet(Long perusteId, Class<T> view) {
        return listOppiaineet(getByPerusteId(perusteId).getOppiaineet().stream(), view);
    }

    protected <T extends OppiaineBaseDto> List<T> listOppiaineet(Stream<Oppiaine> oppiaineetStream, Class<T> view) {
        List<Oppiaine> oppiaineet = oppiaineetStream.filter(oa -> oa.getOppiaine() == null)
                .sorted(comparing(Oppiaine::getJnro, (a, b) -> {
                    if (a == null) {
                        return 1;
                    }
                    if (b == null) {
                        return -1;
                    }
                    return a.compareTo(b);
                }))
                .collect(toList());
        return mapper.mapAsList(oppiaineet, view);
    }

    protected static void assertExists(Object o, String msg) {
        if (o == null) {
            throw new NotExistsException(msg);
        }
    }
}
