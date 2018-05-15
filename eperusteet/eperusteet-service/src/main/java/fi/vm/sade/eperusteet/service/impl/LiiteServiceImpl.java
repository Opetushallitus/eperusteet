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
package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.liite.Liite;
import fi.vm.sade.eperusteet.dto.liite.LiiteDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.liite.LiiteRepository;
import fi.vm.sade.eperusteet.service.LiiteService;
import fi.vm.sade.eperusteet.service.event.aop.IgnorePerusteUpdateCheck;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jhyoty
 */
@Service
public class LiiteServiceImpl implements LiiteService {

    @Autowired
    private LiiteRepository liitteet;

    @Autowired
    private PerusteRepository perusteet;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    @IgnorePerusteUpdateCheck
    public void export(Long perusteId, UUID id, OutputStream os) {
        Liite liite = liitteet.findOne(id);
        if (liite == null) {
            throw new NotExistsException();
        }

        try (InputStream is = liite.getData().getBinaryStream()) {
            IOUtils.copy(is, os);
        } catch (SQLException | IOException ex) {
            // FIXME
            // throw new ServiceException("Liiteen lataaminen ei onnistu", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @IgnorePerusteUpdateCheck
    public LiiteDto get(Long perusteId, UUID id) {
        Liite liite = liitteet.findOne(id);
        Peruste peruste = perusteet.findOne(perusteId);

        if (liite == null) {
            throw new NotExistsException("liite-ei-loytynyt");
        }

        if (!liite.getPerusteet().contains(peruste)) {
            throw new BusinessRuleViolationException("kuva-ei-kuulu-julkaistuun-perusteeseen");
        }

        return mapper.map(liite, LiiteDto.class);
    }

    @Override
    @Transactional
    public UUID add(Long perusteId, String tyyppi, String nimi, long length, InputStream is) {
        Liite liite = liitteet.add(tyyppi, nimi, length, is);
        Peruste peruste = perusteet.findOne(perusteId);
        peruste.attachLiite(liite);
        return liite.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiiteDto> getAll(Long perusteId) {
        List<Liite> loydetyt = liitteet.findByPerusteId(perusteId);
        return mapper.mapAsList(loydetyt, LiiteDto.class);
    }

    @Override
    @Transactional
    public void delete(Long perusteId, UUID id) {
        Liite liite = liitteet.findOne(perusteId, id);
        if ( liite == null ) {
            throw new NotExistsException();
        }
        perusteet.findOne(perusteId).removeLiite(liite);
    }

}
