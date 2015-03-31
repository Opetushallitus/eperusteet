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

import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueKokonaanDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueLaajaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaamistavoiteLaajaDto;
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

    //yleiset haut sallittu kaikille -- palauttaa vain julkaistuja osia
    @PreAuthorize("permitAll()")
    List<PerusteenOsaDto.Laaja> getAllByKoodiUri(final String koodiUri);

    @PreAuthorize("permitAll()")
    void onkoTutkinnonOsanKoodiKaytossa(final String koodiUri);

    @PreAuthorize("permitAll()")
    List<PerusteenOsaDto.Suppea> getAll();

    @PreAuthorize("permitAll()")
    List<PerusteenOsaDto.Suppea> getAllWithName(final String name);

    @PreAuthorize("hasPermission(#po.dto.id, 'perusteenosa', 'MUOKKAUS') or hasPermission(#po.dto.id, 'perusteenosa', 'KORJAUS')")
    <T extends PerusteenOsaDto.Laaja> T update(@P("po") UpdateDto<T> perusteenOsaDto);

    @PreAuthorize("hasPermission(#po.id, 'perusteenosa', 'MUOKKAUS') or hasPermission(#po.id, 'perusteenosa', 'KORJAUS')")
    <T extends PerusteenOsaDto.Laaja> T update(@P("po") T perusteenOsaDto);

    @PreAuthorize("isAuthenticated()")
    @PostAuthorize("hasPermission(returnObject.id, 'perusteenosa', 'MUOKKAUS')")
    <T extends PerusteenOsaDto.Laaja> T add(PerusteenOsaViite viite, T perusteenOsaDto);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'POISTO')")
    void delete(@P("id") final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    Integer getLatestRevision(@P("id") final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    PerusteenOsaDto.Laaja revertToVersio(@P("id") Long id, Integer versioId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    PerusteenOsaDto.Laaja get(@P("id") final Long id);

    @PostAuthorize("hasPermission(returnObject.id, 'perusteenosa', 'LUKU')")
    public PerusteenOsaDto.Laaja getByViite(final Long viiteId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    List<Revision> getVersiot(@P("id") Long id);

    //TODO: versiotietojen lukuoikeus?
    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    PerusteenOsaDto.Laaja getVersio(@P("id") final Long id, final Integer versioId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS') or hasPermission(#id, 'perusteenosa', 'KORJAUS')")
    LukkoDto lock(@P("id") final Long id);

    @PreAuthorize("isAuthenticated()")
    void unlock(@P("id") final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    LukkoDto getLock(@P("id") final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    OsaAlueLaajaDto addTutkinnonOsaOsaAlue(@P("id") final Long id, OsaAlueLaajaDto osaAlueDto);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS') or hasPermission(#viiteId, 'tutkinnonosaviite', 'KORJAUS')")
    OsaAlueKokonaanDto updateTutkinnonOsaOsaAlue(@P("viiteId") final Long viiteId, final Long osaAlueId, OsaAlueKokonaanDto osaAlue);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'LUKU')")
    OsaAlueKokonaanDto getTutkinnonOsaOsaAlue(@P("viiteId") final Long viiteId, final Long osaAlueId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    List<OsaAlueKokonaanDto> getTutkinnonOsaOsaAlueet(@P("id") final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    List<OsaAlueKokonaanDto> getTutkinnonOsaOsaAlueetVersio(Long id, Integer versioId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    public OsaamistavoiteLaajaDto addOsaamistavoite(@P("id") final Long id, final Long osaAlueId, OsaamistavoiteLaajaDto osaamistavoiteDto);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS') or hasPermission(#id, 'perusteenosa', 'KORJAUS')")
    public OsaamistavoiteLaajaDto updateOsaamistavoite(@P("id") final Long id, final Long osaAlueId, final Long osaamistavoiteId, OsaamistavoiteLaajaDto osaamistavoite);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    public List<OsaamistavoiteLaajaDto> getOsaamistavoitteet(final Long id, final Long osaAlueId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    public void removeOsaamistavoite(@P("id") final Long id, final Long osaAlueId, final Long osaamistavoiteId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    public void removeOsaAlue(@P("id") final Long id, final Long osaAlueId);

    @PreAuthorize("hasPermission(#id, 'perusteenosaviite', 'LUKU')")
    public List<Revision> getVersiotByViite(final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosaviite', 'LUKU')")
    public PerusteenOsaDto getVersioByViite(Long id, Integer versioId);

    @PreAuthorize("permitAll()")
    public Revision getLastModifiedRevision(final Long id);

}
