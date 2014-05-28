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

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.PerusteenOsaDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import java.util.List;

/**
 *
 * @author jhyoty
 */
public interface PerusteenOsaService {

    <T extends PerusteenOsaDto, D extends PerusteenOsa> T update(T perusteenOsaDto, Class<T> dtoClass, Class<D> entityClass);

    <T extends PerusteenOsaDto, D extends PerusteenOsa> T add(T perusteenOsaDto, Class<T> dtoClass, Class<D> destinationClass);

    void delete(final Long id);

    PerusteenOsaDto get(final Long id);

    List<PerusteenOsaDto> getAllByKoodiUri(final String koodiUri);

    List<PerusteenOsaDto> getAll();

    List<PerusteenOsaDto> getAllWithName(final String name);

    List<Revision> getRevisions(Long id);

    PerusteenOsaDto getRevision(final Long id, final Integer revisionId);

    LukkoDto lock(final Long id);

    void unlock(final Long id);

    LukkoDto getLock(final Long id);
}
