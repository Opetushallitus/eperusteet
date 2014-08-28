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


import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonOsa.OsaAlueLaajaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonOsa.OsaamistavoiteLaajaDto;

import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import java.util.List;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author jhyoty
 */
public interface PerusteenOsaService {


    @PreAuthorize("hasPermission(#po.dto.id, 'perusteenosa', 'MUOKKAUS')")
    <T extends PerusteenOsaDto, D extends PerusteenOsa> T update(@P("po") UpdateDto<T> perusteenOsaDto, Class<T> dtoClass);
    @PreAuthorize("hasPermission(#po.id, 'perusteenosa', 'MUOKKAUS')")
    <T extends PerusteenOsaDto, D extends PerusteenOsa> T update(@P("po") T perusteenOsaDto, Class<T> dtoClass);

    <T extends PerusteenOsaDto, D extends PerusteenOsa> T add(T perusteenOsaDto, Class<T> dtoClass, Class<D> destinationClass);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'POISTO')")
    void delete(final Long id);

    Integer getLatestRevision(final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    PerusteenOsaDto revertToVersio(@P("id") Long id, Integer versioId);

    @PostAuthorize("returnObject.tila == T(fi.vm.sade.eperusteet.domain.PerusteTila).VALMIS or hasPermission(#id, 'perusteenosa', 'LUKU')")
    PerusteenOsaDto get(@P("id") final Long id);

    List<PerusteenOsaDto> getAllByKoodiUri(final String koodiUri);

    List<PerusteenOsaDto> getAll();

    List<PerusteenOsaDto> getAllWithName(final String name);

    List<Revision> getVersiot(Long id);

    @PostAuthorize("returnObject.tila == T(fi.vm.sade.eperusteet.domain.PerusteTila).VALMIS or hasPermission(#id, 'perusteenosa', 'LUKU')")
    PerusteenOsaDto getVersio(@P("id") final Long id, final Integer versioId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    LukkoDto lock(@P("id") final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    void unlock(final Long id);

    LukkoDto getLock(final Long id);

    OsaAlueLaajaDto addTutkinnonOsaOsaAlue(final Long id, OsaAlueLaajaDto osaAlueDto);

    OsaAlueLaajaDto updateTutkinnonOsaOsaAlue(final Long id, final Long osaAlueId, OsaAlueLaajaDto osaAlue);

    List<OsaAlueLaajaDto> getTutkinnonOsaOsaAlueet(final Long id);

    public OsaamistavoiteLaajaDto addOsaamistavoite(final Long id, final Long osaAlueId, OsaamistavoiteLaajaDto osaamistavoiteDto);

    public OsaamistavoiteLaajaDto updateOsaamistavoite(final Long id, final Long osaAlueId, final Long osaamistavoiteId, OsaamistavoiteLaajaDto osaamistavoite);

    public List<OsaamistavoiteLaajaDto> getOsaamistavoitteet(final Long id, final Long osaAlueId);

    public void removeOsaamistavoite(final Long id, final Long osaAlueId, final Long osaamistavoiteId);

    public void removeOsaAlue(final Long id, final Long osaAlueId);
}
