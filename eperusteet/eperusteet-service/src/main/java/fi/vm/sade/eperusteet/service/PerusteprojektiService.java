package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.OmistajaDto;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaTyoryhmaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteprojektiQueryDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.DiaarinumeroHakuDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiInfoDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiKevytDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiListausDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.TyoryhmaHenkiloDto;
import fi.vm.sade.eperusteet.dto.util.CombinedDto;
import fi.vm.sade.eperusteet.service.util.Validointi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Set;

public interface PerusteprojektiService {

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

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    PerusteprojektiDto savePohja(PerusteprojektiLuontiDto perusteprojektiDto);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'MUOKKAUS')")
    PerusteprojektiDto update(@P("id") final Long id, PerusteprojektiDto perusteprojektiDto);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'LUKU')")
    Set<ProjektiTila> getTilat(@P("id") final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'TILANVAIHTO')")
    TilaUpdateStatus updateTila(@P("id") final Long id, ProjektiTila tila, TiedoteDto tiedoteDto);

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    void updateProjektiTila(Long id, ProjektiTila tila);

    @PreAuthorize("hasPermission(null, 'pohja', 'LUONTI')")
    void avaaPerusteProjekti(Long id);

    @PreAuthorize("isAuthenticated()")
    DiaarinumeroHakuDto onkoDiaarinumeroKaytossa(Diaarinumero diaarinumero);

    @PreAuthorize("isAuthenticated()")
    @PostFilter("hasPermission(filterObject.id,'perusteprojekti','LUKU')")
    List<PerusteprojektiListausDto> getOmatProjektit();

    @PreAuthorize("isAuthenticated()")
    @PostFilter("hasPermission(filterObject.id,'perusteprojekti','LUKU')")
    List<PerusteprojektiListausDto> getOmatJulkaistut();

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

    @PreAuthorize("hasPermission(#id, 'perusteprojekti', 'MUOKKAUS')")
    List<Validointi> validoiProjekti(@P("id") Long id, ProjektiTila tila);
}
