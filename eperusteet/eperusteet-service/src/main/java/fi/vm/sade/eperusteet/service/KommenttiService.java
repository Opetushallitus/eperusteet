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

import fi.vm.sade.eperusteet.dto.KommenttiDto;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 *
 * @author nkala
 */
public interface KommenttiService {

    @PreAuthorize("isAuthenticated()")
    List<KommenttiDto> getAllByPerusteenOsa(Long id, Long perusteeonOsaId);

    @PreAuthorize("isAuthenticated()")
    List<KommenttiDto> getAllByPerusteenOsa(Long perusteenOsaId);

    @PreAuthorize("isAuthenticated()")
    List<KommenttiDto> getAllBySuoritustapa(Long id, String suoritustapa);

    @PreAuthorize("isAuthenticated()")
    List<KommenttiDto> getAllByPerusteprojekti(Long id);

    @PreAuthorize("isAuthenticated()")
    List<KommenttiDto> getAllByParent(Long id);

    @PreAuthorize("isAuthenticated()")
    List<KommenttiDto> getAllByYlin(Long id);

    @PreAuthorize("isAuthenticated()")
    KommenttiDto get(Long kommenttiId);

    @PreAuthorize("hasPermission(#k.perusteprojektiId, 'perusteProjekti', 'LUKU')")
    KommenttiDto add(@P("k") final KommenttiDto kommenttidto);

    @PreAuthorize("isAuthenticated()")
    KommenttiDto update(Long kommenttiId, final KommenttiDto kommenttidto);

    @PreAuthorize("isAuthenticated()")
    void delete(Long kommenttiId);

    @PreAuthorize("isAuthenticated()")
    void deleteReally(Long kommenttiId);
}
