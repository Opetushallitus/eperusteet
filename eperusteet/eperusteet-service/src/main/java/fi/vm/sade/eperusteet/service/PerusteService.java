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

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.PerusteTekstikappaleillaDto;
import fi.vm.sade.eperusteet.dto.peruste.*;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaTilaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.TutkinnonOsaViiteUpdateDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukiokoulutuksenYleisetTavoitteetDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author harrik
 */
public interface PerusteService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void removeTutkinnonOsa(@P("perusteId") Long perusteId, Suoritustapakoodi of, Long osaId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    TutkinnonOsaViiteDto updateTutkinnonOsa(@P("perusteId") Long perusteId, Suoritustapakoodi of, TutkinnonOsaViiteUpdateDto osa);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    TutkinnonOsaViiteDto updateTutkinnonOsa(@P("perusteId") Long perusteId, Suoritustapakoodi of, TutkinnonOsaViiteDto osa);

    @PreAuthorize("isAuthenticated()")
    @PostAuthorize("hasPermission(returnObject.tutkinnonOsaDto.id, 'perusteenosa', 'LUKU')")
    TutkinnonOsaViiteDto getTutkinnonOsaViite(Long perusteId, Suoritustapakoodi suoritustapakoodi, Long viiteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    TutkinnonOsaViiteDto attachTutkinnonOsa(@P("perusteId") Long perusteId, Suoritustapakoodi of, TutkinnonOsaViiteDto osa);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    TutkinnonOsaViiteDto addTutkinnonOsa(@P("perusteId") Long id, Suoritustapakoodi koodi, TutkinnonOsaViiteDto osa);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    PerusteDto get(@P("perusteId") final Long id);

    @PreAuthorize("permitAll()")
    boolean isDiaariValid(String diaarinumero);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    PerusteKaikkiDto getKokoSisalto(@P("perusteId") final Long id);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    PerusteKaikkiDto getKokoSisalto(@P("perusteId") final Long id, Integer rev);

    @PreAuthorize("hasPermission(#perusteId, 'perusteenmetatiedot', 'MUOKKAUS')")
    PerusteDto update(@P("perusteId") Long perusteId, PerusteDto perusteDto);

    @PreAuthorize("hasPermission(#perusteId, 'perusteenmetatiedot', 'MUOKKAUS')")
    PerusteDto updateFull(@P("perusteId") Long perusteId, PerusteDto perusteDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    PerusteDto getByIdAndSuoritustapa(@P("perusteId") final Long id, Suoritustapakoodi suoritustapakoodi);

    @PreAuthorize("permitAll()")
    Page<PerusteHakuDto> getAll(PageRequest page, String kieli);

    @PreAuthorize("permitAll()")
    List<PerusteKoosteDto> getKooste();

    @PreAuthorize("permitAll()")
    List<PerusteDto> getUusimmat(Set<Kieli> kielet);

    @PreAuthorize("permitAll()")
    List<PerusteInfoDto> getAllInfo();

    @PreAuthorize("permitAll()")
    Page<PerusteHakuDto> findJulkinenBy(PageRequest page, PerusteQuery pquery);

    @PreAuthorize("isAuthenticated()")
    Page<PerusteHakuInternalDto> findByInternal(PageRequest page, PerusteQuery pquery);

    @PreAuthorize("permitAll()")
    List<PerusteInfoDto> getAllPerusopetusInfo();

    PerusteInfoDto getMeta(final Long id);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    Map<Suoritustapakoodi, Map<String, List<TekstiKappaleDto>>> getOsaamisalaKuvaukset(@P("perusteId") final Long perusteId);

    @Deprecated
    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    PerusteenOsaViiteDto.Matala addSisalto(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapakoodi, PerusteenOsaViiteDto.Matala viite);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    PerusteenOsaViiteDto.Matala addSisaltoUUSI(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapakoodi, PerusteenOsaViiteDto.Matala viite);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    PerusteenOsaViiteDto.Matala addSisaltoLapsi(@P("perusteId") final Long perusteId, final Long perusteenosaViiteId, PerusteenOsaViiteDto.Matala viite);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    PerusteenOsaViiteDto.Laaja getSuoritustapaSisalto(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapakoodi);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    <T extends PerusteenOsaViiteDto.Puu<?, ?>> T getSuoritustapaSisalto(@P("perusteId") Long perusteId, Suoritustapakoodi suoritustapakoodi, Class<T> view);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    SuoritustapaDto getSuoritustapa(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapakoodi);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    RakenneModuuliDto getTutkinnonRakenne(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapa, Integer eTag);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    RakenneModuuliDto updateTutkinnonRakenne(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapa, final UpdateDto<RakenneModuuliDto> rakenne);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    RakenneModuuliDto updateTutkinnonRakenne(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapa, final RakenneModuuliDto rakenne);

    // Käytetään vain sisäisistä palveluista ja ainoastaan synkronoi tutkinnon osien järjestysnumerot muodostumispuuta vastaavaksi
    @PreAuthorize("isAuthenticated()")
    void updateAllTutkinnonOsaJarjestys(@P("perusteId") final Long perusteId, RakenneModuuliDto rakenne);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    RakenneModuuliDto revertRakenneVersio(@P("perusteId") Long id, Suoritustapakoodi suoritustapakoodi, Integer versioId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<Revision> getRakenneVersiot(@P("perusteId") Long id, Suoritustapakoodi suoritustapakoodi);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    RakenneModuuliDto getRakenneVersio(@P("perusteId") Long id, Suoritustapakoodi suoritustapakoodi, Integer versioId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<TutkinnonOsaViiteDto> getTutkinnonOsat(@P("perusteId") Long perusteid, Suoritustapakoodi suoritustapakoodi);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<TutkinnonOsaViiteDto> getTutkinnonOsat(@P("perusteId") Long perusteid, Suoritustapakoodi suoritustapakoodi, Integer revisio);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<TutkinnonOsaTilaDto> getTutkinnonOsienTilat(@P("perusteId") Long perusteid, Suoritustapakoodi suoritustapakoodi);

    @PreAuthorize("isAuthenticated()")
    Peruste luoPerusteRunko(KoulutusTyyppi koulutustyyppi, KoulutustyyppiToteutus toteutus, LaajuusYksikko yksikko,
                            PerusteTyyppi tyyppi);

    @PreAuthorize("isAuthenticated()")
    Peruste luoPerusteRunko(KoulutusTyyppi koulutustyyppi, KoulutustyyppiToteutus toteutus, LaajuusYksikko yksikko,
                            PerusteTyyppi tyyppi, boolean isReforminMukainen);

    @PreAuthorize("isAuthenticated()")
    Peruste luoPerusteRunkoToisestaPerusteesta(PerusteprojektiLuontiDto luontiDto, PerusteTyyppi tyyppi);

    @PreAuthorize("permitAll()")
    Page<PerusteInfoDto> findByInfo(PageRequest page, PerusteQuery pquery);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<TutkintonimikeKoodiDto> getTutkintonimikeKoodit(@P("perusteId") Long perusteId);

    @PreAuthorize("permitAll()")
    List<TutkintonimikeKoodiDto> doGetTutkintonimikeKoodit(@P("perusteId") Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    TutkintonimikeKoodiDto addTutkintonimikeKoodi(@P("perusteId") Long perusteId, TutkintonimikeKoodiDto dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void removeTutkintonimikeKoodi(@P("perusteId") Long perusteId, Long tutkintonimikeKoodiId);

    @PostAuthorize("returnObject == null or hasPermission(returnObject.id, 'peruste', 'LUKU')")
    PerusteInfoDto getByDiaari(Diaarinumero diaarinumero);

    @PreAuthorize("permitAll()")
    PerusteKaikkiDto getAmosaaYhteinenPohja();

    @PreAuthorize("permitAll()")
    List<PerusteHakuDto> getAmosaaOpsit();

    @PreAuthorize("permitAll()")
    PerusteVersionDto getPerusteVersion(long id);

    @PreAuthorize("permitAll()")
    Revision getLastModifiedRevision(final Long id);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    LukiokoulutuksenYleisetTavoitteetDto getYleisetTavoitteet(@P("perusteId") long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    LukiokoulutuksenYleisetTavoitteetDto getYleisetTavoitteetByVersion(@P("perusteId") long perusteId, int revision);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void tallennaYleisetTavoitteet(@P("perusteId") Long perusteId, LukiokoulutuksenYleisetTavoitteetDto lukiokoulutuksenYleisetTavoitteetDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<Revision> getYleisetTavoitteetVersiot(@P("perusteId") Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    LukiokoulutuksenYleisetTavoitteetDto palautaYleisetTavoitteet(@P("perusteId") long perusteId, int revisio);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    TutkinnonOsaViiteDto getTutkinnonOsaViiteByKoodiUri(@P("perusteId") Long perusteId, Suoritustapakoodi suoritustapakoodi, String koodiUri);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    KVLiiteJulkinenDto getJulkinenKVLiite(@P("perusteId") long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'perusteenmetatiedot', 'MUOKKAUS')")
    PerusteDto updateKvLiite(@P("perusteId") Long perusteId, KVLiiteJulkinenDto kvliiteDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<KVLiiteTasoDto> haeTasot(@P("perusteId") Long perusteId, Peruste peruste);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    void exportPeruste(@P("perusteId") Long perusteId, ZipOutputStream zipOutputStream) throws IOException;

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    void importPeruste(MultipartHttpServletRequest request) throws IOException;

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    NavigationNodeDto buildNavigationWithDate(@P("perusteId") Long perusteId, Date pvm, String kieli);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    NavigationNodeDto buildNavigation(@P("perusteId") Long perusteId, String kieli);

    @PreAuthorize("isAuthenticated()")
    List<PerusteTekstikappaleillaDto> findByTekstikappaleKoodi(String koodi);

    @PreAuthorize("isAuthenticated()")
    List<PerusteKevytDto> getAllOppaidenPerusteet();
}
