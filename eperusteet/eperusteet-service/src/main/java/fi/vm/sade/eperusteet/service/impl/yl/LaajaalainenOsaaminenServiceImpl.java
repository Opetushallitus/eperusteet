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
package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.yl.LaajaalainenOsaaminen;
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.repository.LaajaalainenOsaaminenRepository;
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.yl.LaajaalainenOsaaminenContext;
import fi.vm.sade.eperusteet.service.yl.LaajaalainenOsaaminenService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jhyoty
 */
@Service
@Transactional
public class LaajaalainenOsaaminenServiceImpl implements LaajaalainenOsaaminenService {

    @Autowired
    private PerusopetuksenPerusteenSisaltoRepository sisaltoRepository;
    @Autowired
    private LaajaalainenOsaaminenRepository osaaminenRepository;

    @Autowired
    @LockCtx(LaajaalainenOsaaminenContext.class)
    private LockService<LaajaalainenOsaaminenContext> lockService;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Override
    public LaajaalainenOsaaminenDto addLaajaalainenOsaaminen(Long perusteId, LaajaalainenOsaaminenDto dto) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        notNull(sisalto, "Päivitettävää tietoa ei ole olemassa");
        LaajaalainenOsaaminen tmp = mapper.map(dto, LaajaalainenOsaaminen.class);
        tmp = osaaminenRepository.save(tmp);
        sisalto.addLaajaalainenOsaaminen(tmp);
        return mapper.map(tmp, LaajaalainenOsaaminenDto.class);
    }

    @Override
    public LaajaalainenOsaaminenDto updateLaajaalainenOsaaminen(Long perusteId, LaajaalainenOsaaminenDto dto) {

        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        notNull(sisalto, "Päivitettävää tietoa ei ole olemassa");
        LaajaalainenOsaaminen current = sisalto.getLaajaalainenOsaaminen(dto.getId());
        notNull(current, "Päivitettävää tietoa ei ole olemassa");
        lockService.assertLock(LaajaalainenOsaaminenContext.of(perusteId, dto.getId()));
        sisaltoRepository.lock(sisalto);
        mapper.map(dto, current);
        sisaltoRepository.save(sisalto);
        return mapper.map(current, LaajaalainenOsaaminenDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public LaajaalainenOsaaminenDto getLaajaalainenOsaaminen(Long perusteId, Long id) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        notNull(sisalto, "Perustetta ei ole olemassa");
        return mapper.map(sisalto.getLaajaalainenOsaaminen(id), LaajaalainenOsaaminenDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public LaajaalainenOsaaminenDto getLaajaalainenOsaaminen(Long perusteId, Long id, int revisio) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findRevision(id, revisio);
        notNull(sisalto, "Perustetta ei ole olemassa");
        return mapper.map(sisalto.getLaajaalainenOsaaminen(id), LaajaalainenOsaaminenDto.class);
    }

    @Override
    public List<Revision> getLaajaalainenOsaaminenVersiot(Long perusteId, Long id) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        notNull(notNull(sisalto, "Perustetta ei ole olemassa").getLaajaalainenOsaaminen(id), "Laaja-alaista osaamista ei ole olemassa");
        return osaaminenRepository.getRevisions(id);
    }

    @Override
    public void deleteLaajaalainenOsaaminen(Long perusteId, Long id) {
        PerusopetuksenPerusteenSisalto sisalto = sisaltoRepository.findByPerusteId(perusteId);
        LaajaalainenOsaaminen lo = notNull(sisalto, "Perustetta ei ole olemassa").getLaajaalainenOsaaminen(id);
        notNull(lo, "Laaja-alaista osaamista ei ole olemassa");
        sisaltoRepository.lock(sisalto);
        final LaajaalainenOsaaminenContext ctx = LaajaalainenOsaaminenContext.of(perusteId, id);
        lockService.assertLock(LaajaalainenOsaaminenContext.of(perusteId, id));
        try {
            sisalto.removeLaajaalainenOsaaminen(lo);
            // Poista laaja-alainen osaamisen jos siihen ei ole enää viittauksia
        } finally {
            lockService.unlock(ctx);
        }
        osaaminenRepository.delete(lo);
    }

    private static <T> T notNull(T o, String msg) {
        if (o == null) {
            throw new BusinessRuleViolationException(msg);
        }
        return o;
    }
}
