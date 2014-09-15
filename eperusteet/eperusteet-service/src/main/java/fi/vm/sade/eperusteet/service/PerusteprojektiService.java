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

import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiInfoDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.TyoryhmaHenkiloDto;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import java.util.List;
import java.util.Set;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author harrik
 */
public interface PerusteprojektiService {

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'LUKU')")
    List<KayttajanTietoDto> getJasenet(Long id);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'LUKU')")
    List<CombinedDto<KayttajanTietoDto, KayttajanProjektitiedotDto>> getJasenetTiedot(Long id);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'LUKU')")
    PerusteprojektiDto get(final Long id);

    @PreAuthorize("isAuthenticated()")
    //@PostFilter("hasPermission(filterObject.id,'perusteprojekti','LUKU')")
    List<PerusteprojektiInfoDto> getBasicInfo();

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    PerusteprojektiDto save(PerusteprojektiLuontiDto perusteprojektiDto);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'MUOKKAUS')")
    PerusteprojektiDto update(final Long id, PerusteprojektiDto perusteprojektiDto);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'LUKU')")
    Set<ProjektiTila> getTilat(final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'TILANVAIHTO')")
    TilaUpdateStatus updateTila(final Long id, ProjektiTila tila);

    @PreAuthorize("isAuthenticated()")
    void onkoDiaarinumeroKaytossa(String diaarinumero);

    @PreAuthorize("isAuthenticated()")
    @PostFilter("hasPermission(filterObject.id,'perusteprojekti','LUKU')")
    List<PerusteprojektiInfoDto> getOmatProjektit();

    List<TyoryhmaDto> getTyoryhmat(Long perusteProjektiId);

    List<TyoryhmaDto> getTyoryhmat(Long perusteProjektiId, String tyoryhmaOid);

    TyoryhmaDto saveTyoryhma(Long id, TyoryhmaDto tyoryhma);

    List<TyoryhmaHenkiloDto> getTyoryhmaHenkilot(Long perusteProjektiId);

    List<TyoryhmaHenkiloDto> getTyoryhmaHenkilot(Long perusteProjektiId, String nimi);

    List<TyoryhmaHenkiloDto> saveTyoryhma(Long perusteProjektiId, String tyoryhma, List<TyoryhmaHenkiloDto> henkilot);

    TyoryhmaHenkiloDto saveTyoryhma(Long id, TyoryhmaHenkiloDto tyoryhma);

    void removeTyoryhma(Long trId);

    void removeTyoryhma(Long perusteProjektiId, String nimi);
}
