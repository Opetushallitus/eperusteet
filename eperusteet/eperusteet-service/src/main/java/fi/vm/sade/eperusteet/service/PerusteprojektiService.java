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

import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.KoulutuskoodiStatusDto;
import fi.vm.sade.eperusteet.dto.OmistajaDto;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.validointi.ValidationDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaTyoryhmaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.*;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author harrik
 */
public interface PerusteprojektiService {

    void validoiPerusteetTask();

    void tarkistaKooditTask();

    void lataaMaarayskirjeetTask();

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'LUKU')")
    List<KayttajanTietoDto> getJasenet(@P("id") Long id);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'LUKU')")
    OmistajaDto isOwner(@P("id") Long id, Long perusteenOsaId);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'LUKU')")
    List<CombinedDto<KayttajanTietoDto, KayttajanProjektitiedotDto>> getJasenetTiedot(@P("id") Long id);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'LUKU')")
    PerusteprojektiDto get(@P("id") final Long id);

    @PreAuthorize("isAuthenticated()")
    @PostFilter("hasPermission(filterObject.id,'perusteprojekti','LUKU')")
    List<PerusteprojektiInfoDto> getBasicInfo();

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    Page<PerusteprojektiKevytDto> findBy(PageRequest page, PerusteprojektiQueryDto query);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    List<PerusteprojektiKevytDto> getKevytBasicInfo();

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    PerusteprojektiDto save(PerusteprojektiLuontiDto perusteprojektiDto);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'MUOKKAUS')")
    PerusteprojektiDto update(@P("id") final Long id, PerusteprojektiDto perusteprojektiDto);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'LUKU')")
    Set<ProjektiTila> getTilat(@P("id") final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'TILANVAIHTO')")
    TilaUpdateStatus updateTila(@P("id") final Long id, ProjektiTila tila, TiedoteDto tiedoteDto);

    @PreAuthorize("isAuthenticated()")
    DiaarinumeroHakuDto onkoDiaarinumeroKaytossa(Diaarinumero diaarinumero);

    @PreAuthorize("isAuthenticated()")
    @PostFilter("hasPermission(filterObject.id,'perusteprojekti','LUKU')")
    List<PerusteprojektiListausDto> getOmatProjektit();

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'LUKU')")
    List<TyoryhmaHenkiloDto> getTyoryhmaHenkilot(@P("id") Long perusteProjektiId);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'LUKU')")
    List<TyoryhmaHenkiloDto> getTyoryhmaHenkilot(@P("id") Long perusteProjektiId, String nimi);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'MUOKKAUS')")
    List<TyoryhmaHenkiloDto> saveTyoryhma(@P("id") Long perusteProjektiId, String tyoryhma, List<String> henkilot);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'MUOKKAUS')")
    TyoryhmaHenkiloDto saveTyoryhma(@P("id") Long id, TyoryhmaHenkiloDto tyoryhma);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'MUOKKAUS')")
    void removeTyoryhma(@P("id") Long perusteProjektiId, String nimi);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'MUOKKAUS')")
    List<String> setPerusteenOsaViiteTyoryhmat(@P("id") Long perusteProjektiId, Long perusteenOsaId, List<String> nimet);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'LUKU')")
    List<String> getPerusteenOsaViiteTyoryhmat(@P("id") Long perusteProjektiId, Long perusteenOsaId);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'LUKU')")
    List<PerusteenOsaTyoryhmaDto> getSisallonTyoryhmat(@P("id") Long perusteProjektiId);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    Page<ValidationDto> getVirheelliset(PageRequest p);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    Page<KoulutuskoodiStatusDto> getKoodiongelmat(PageRequest p);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'MUOKKAUS')")
    TilaUpdateStatus validoiProjekti(Long id, ProjektiTila tila);
}
