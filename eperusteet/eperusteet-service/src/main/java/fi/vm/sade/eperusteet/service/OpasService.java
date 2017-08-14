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

import fi.vm.sade.eperusteet.dto.opas.OpasDto;
import fi.vm.sade.eperusteet.dto.opas.OpasLuontiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author nkala
 */
public interface OpasService {

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'LUKU')")
    OpasDto get(@P("id") final Long id);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    OpasDto save(OpasLuontiDto opasDto);

    @PreAuthorize("permitAll()")
    Page<PerusteHakuDto> findBy(PageRequest page, PerusteQuery pquery);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    Page<PerusteprojektiKevytDto> findProjektiBy(PageRequest p, PerusteprojektiQueryDto pquery);
}
