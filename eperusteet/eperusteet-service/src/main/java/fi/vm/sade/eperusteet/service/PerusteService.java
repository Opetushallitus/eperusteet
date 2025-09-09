package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.KoulutustyyppiLukumaara;
import fi.vm.sade.eperusteet.dto.PerusteTekstikappaleillaDto;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteLaajaDto;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteTasoDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuInternalDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteInfoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKevytDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKoosteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.SuoritustapaDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.peruste.TutkintonimikeKoodiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaKaikkiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaTilaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteSuppeaDto;
import fi.vm.sade.eperusteet.dto.util.TutkinnonOsaViiteUpdateDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukiokoulutuksenYleisetTavoitteetDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipOutputStream;

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

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    TutkinnonOsaViiteDto attachTutkinnonOsa(@P("perusteId") Long perusteId, Suoritustapakoodi of, TutkinnonOsaViiteDto osa);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    TutkinnonOsaViiteDto attachTutkinnonOsa(@P("perusteId") Long perusteId, Suoritustapakoodi of, TutkinnonOsaViiteDto osa, PerusteKevytDto alkuperainenPeruste);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    TutkinnonOsaViiteDto addTutkinnonOsa(@P("perusteId") Long id, Suoritustapakoodi koodi, TutkinnonOsaViiteDto osa);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    PerusteDto get(@P("perusteId") final Long id);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    ProjektiTila getPerusteProjektiTila(@P("perusteId") final Long id);

    @PreAuthorize("permitAll()")
    PerusteKaikkiDto getJulkaistuSisalto(@P("perusteId") final Long id);

    @PreAuthorize("permitAll()")
    Object getJulkaistuSisaltoObjectNode(@P("perusteId") final Long id, String query);

    @PreAuthorize("permitAll()")
    Object getJulkaistuSisaltoObjectNode(@P("perusteId") final Long id, List<String> queryList);

    @PreAuthorize("permitAll()")
    PerusteKaikkiDto getJulkaistuSisalto(@P("perusteId") final Long id, boolean useCurrentData);

    @PreAuthorize("permitAll()")
    PerusteKaikkiDto getJulkaistuSisalto(@P("perusteId") final Long id, Integer julkaisuRevisio, boolean useCurrentData);

    @PreAuthorize("permitAll")
    List<TutkinnonOsaKaikkiDto> getJulkaistutTutkinnonOsat(Long perusteId, boolean useCurrentData);

    @PreAuthorize("permitAll")
    Set<TutkinnonOsaViiteSuppeaDto> getJulkaistutTutkinnonOsaViitteet(Long perusteId, boolean useCurrentData);

    @PreAuthorize("permitAll")
    PerusteenOsaDto getJulkaistuPerusteenOsa(Long perusteId, Long perusteOsaId);

    @PreAuthorize("hasPermission(#perusteId, 'perusteenmetatiedot', 'MUOKKAUS') ")
    PerusteDto update(@P("perusteId") Long perusteId, PerusteDto perusteDto);

    @PreAuthorize("hasPermission(#perusteId, 'perusteenmetatiedot', 'MUOKKAUS')")
    PerusteDto updateFull(@P("perusteId") Long perusteId, PerusteDto perusteDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    void updateOsaamisalat(@P("perusteId") Long perusteId, Set<KoodiDto> osaamisalat);

    @Transactional(readOnly = true)
    PerusteKaikkiDto getKaikkiSisalto(final Long id);

//    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
//    PerusteDto getByIdAndSuoritustapa(@P("perusteId") final Long id, Suoritustapakoodi suoritustapakoodi);

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

    @PreAuthorize("permitAll()")
    Page<PerusteBaseDto> getJulkaisuAikatauluPerusteet(Integer sivu, Integer sivukoko, List<String> koulutusTyyppit);

    @PreAuthorize("permitAll()")
    List<KoulutustyyppiLukumaara> getVoimassaolevatJulkaistutPerusteLukumaarat(List<String> koulutusTyyppit);

    @PreAuthorize("isAuthenticated()")
    Page<PerusteHakuInternalDto> findByInternal(PageRequest page, PerusteQuery pquery);

    @PreAuthorize("isAuthenticated()")
    List<PerusteKevytDto> getPohjaperusteet(PerusteTyyppi tyyppi);

    @PreAuthorize("permitAll()")
    List<PerusteInfoDto> getAllPerusopetusInfo();

    PerusteInfoDto getMeta(final Long id);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    Map<Suoritustapakoodi, Map<String, List<TekstiKappaleDto>>> getOsaamisalaKuvaukset(@P("perusteId") final Long perusteId);

    @PreAuthorize("permitAll()")
    Map<Suoritustapakoodi, Map<String, List<TekstiKappaleDto>>> getJulkaistutOsaamisalaKuvaukset(final Long perusteId);

    @Deprecated
    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    PerusteenOsaViiteDto.Matala addSisalto(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapakoodi, PerusteenOsaViiteDto.Matala viite);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    PerusteenOsaViiteDto.Matala addSisaltoUUSI(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapakoodi, PerusteenOsaViiteDto.Matala viite);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    PerusteenOsaViiteDto.Matala addSisaltoLapsi(@P("perusteId") final Long perusteId, final Long perusteenosaViiteId, PerusteenOsaViiteDto.Matala viite);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    PerusteenOsaViiteDto.Laaja getSuoritustapaSisalto(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapakoodi);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    <T extends PerusteenOsaViiteDto.Puu<?, ?>> T getSuoritustapaSisalto(@P("perusteId") Long perusteId, Suoritustapakoodi suoritustapakoodi, Class<T> view);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    SuoritustapaDto getSuoritustapa(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapakoodi);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    RakenneModuuliDto getTutkinnonRakenne(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapa, Integer eTag);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    Integer getTutkinnonLaajuus(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapa);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    RakenneModuuliDto updateTutkinnonRakenne(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapa, final UpdateDto<RakenneModuuliDto> rakenne);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    RakenneModuuliDto updateTutkinnonRakenne(@P("perusteId") final Long perusteId, final Suoritustapakoodi suoritustapa, final RakenneModuuliDto rakenne);

    // Käytetään vain sisäisistä palveluista ja ainoastaan synkronoi tutkinnon osien järjestysnumerot muodostumispuuta vastaavaksi
    @PreAuthorize("isAuthenticated()")
    void updateAllTutkinnonOsaJarjestys(@P("perusteId") final Long perusteId, RakenneModuuliDto rakenne);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
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

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    TutkintonimikeKoodiDto getTutkintonimikeKoodi(@P("perusteId") Long perusteId, String tutkintonimikeKoodiUri);

    @PreAuthorize("permitAll()")
    List<TutkintonimikeKoodiDto> doGetTutkintonimikeKoodit(@P("perusteId") Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    TutkintonimikeKoodiDto addTutkintonimikeKoodi(@P("perusteId") Long perusteId, TutkintonimikeKoodiDto dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    TutkintonimikeKoodiDto updateTutkintonimikeKoodi(@P("perusteId") Long perusteId, TutkintonimikeKoodiDto dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    void updateTutkintonimikkeet(@P("perusteId") Long perusteId, List<TutkintonimikeKoodiDto> dtos);

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

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    void tallennaYleisetTavoitteet(@P("perusteId") Long perusteId, LukiokoulutuksenYleisetTavoitteetDto lukiokoulutuksenYleisetTavoitteetDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<Revision> getYleisetTavoitteetVersiot(@P("perusteId") Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS') or hasPermission(#perusteId, 'peruste', 'KORJAUS')")
    LukiokoulutuksenYleisetTavoitteetDto palautaYleisetTavoitteet(@P("perusteId") long perusteId, int revisio);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    TutkinnonOsaViiteDto getTutkinnonOsaViiteByKoodiUri(@P("perusteId") Long perusteId, Suoritustapakoodi suoritustapakoodi, String koodiUri);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU') or isAuthenticated()")
    KVLiiteLaajaDto getJulkinenKVLiite(@P("perusteId") long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'perusteenmetatiedot', 'MUOKKAUS')")
    PerusteDto updateKvLiite(@P("perusteId") Long perusteId, KVLiiteLaajaDto kvliiteDto);

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

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    NavigationNodeDto buildNavigationPublic(@P("perusteId") Long perusteId, String kieli, boolean esikatselu, Integer julkaisuRevisio);

    @PreAuthorize("isAuthenticated()")
    List<PerusteTekstikappaleillaDto> findByTekstikappaleKoodi(String koodi);

    @PreAuthorize("isAuthenticated()")
    List<PerusteKevytDto> getAllOppaidenPerusteet();

    @PreAuthorize("isAuthenticated()")
    List<PerusteKevytDto> getJulkaistutPerusteet();

    @PreAuthorize("permitAll()")
    List<KoulutustyyppiLukumaara> getJulkaistutKoulutustyyppiLukumaarat(Kieli kieli);

    @PreAuthorize("permitAll()")
    List<KoulutusTyyppi> getJulkaistutKoulutustyyppit(Kieli kieli);

    @PreAuthorize("permitAll()")
    List<PerusteDto> getOpasKiinnitettyKoodi(String koodiUri);

    @PreAuthorize("permitAll()")
    List<PerusteInfoDto> getKorvattavatPerusteet(Long perusteId);

    @PreAuthorize("permitAll()")
    List<PerusteKevytDto> getJulkaistutKoostePerusteet();

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    void updateJulkaistutKoostePerusteet(List<PerusteKevytDto> perusteet);

}
