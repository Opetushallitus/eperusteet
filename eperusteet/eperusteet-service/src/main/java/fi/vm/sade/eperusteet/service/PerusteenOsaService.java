package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TutkinnonOsaQueryDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektinPerusteenosaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueKokonaanDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaAlueLaajaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.OsaamistavoiteLaajaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaKaikkiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteKontekstiDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Set;

public interface PerusteenOsaService {

    //yleiset haut sallittu kaikille -- palauttaa vain julkaistuja osia
    @PreAuthorize("permitAll()")
    List<PerusteenOsaDto.Laaja> getAllByKoodiUri(final String koodiUri);

    @PreAuthorize("permitAll()")
    void onkoTutkinnonOsanKoodiKaytossa(final String koodiUri);

    @PreAuthorize("permitAll()")
    Map<String, Boolean> onkoTutkinnonOsanKoodiKaytossa(final List<String> koodiUri);

    @PreAuthorize("permitAll()")
    List<PerusteenOsaDto.Suppea> getAll();

    @PreAuthorize("permitAll()")
    List<PerusteenOsaDto.Suppea> getAllWithName(final String name);

    @PreAuthorize("hasPermission(#po.dto.id, 'perusteenosa', 'MUOKKAUS') or hasPermission(#po.dto.id, 'perusteenosa', 'KORJAUS')")
    <T extends PerusteenOsaDto.Laaja> T update(Long perusteId, final Long viiteId, @P("po") UpdateDto<T> perusteenOsaDto);

    @PreAuthorize("hasPermission(#po.dto.id, 'perusteenosa', 'MUOKKAUS') or hasPermission(#po.dto.id, 'perusteenosa', 'KORJAUS')")
    <T extends PerusteenOsaDto.Laaja> T update(@P("po") UpdateDto<T> perusteenOsaDto);

    @PreAuthorize("hasPermission(#po.id, 'perusteenosa', 'MUOKKAUS') or hasPermission(#po.id, 'perusteenosa', 'KORJAUS')")
    <T extends PerusteenOsaDto.Laaja> T update(@P("po") T perusteenOsaDto);

    @PreAuthorize("isAuthenticated()")
    @PostAuthorize("hasPermission(returnObject.id, 'perusteenosa', 'MUOKKAUS') or hasPermission(returnObject.id, 'perusteenosa', 'KORJAUS')")
    <T extends PerusteenOsaDto.Laaja> T add(PerusteenOsaViite viite, T perusteenOsaDto);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'POISTO')")
    void delete(@P("id") final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'POISTO') or hasPermission(#perusteId,'peruste','MUOKKAUS')")
    void delete(@P("id") final Long id, @P("perusteId") final Long perusteId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    Integer getLatestRevision(@P("id") final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    PerusteenOsaDto.Laaja revertToVersio(@P("id") Long id, Integer versioId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    PerusteenOsaDto.Laaja get(@P("id") final Long id);

    @PostAuthorize("hasPermission(returnObject.id, 'perusteenosa', 'LUKU')")
    PerusteenOsaDto.Laaja getByViite(final Long viiteId);

    @PostAuthorize("hasPermission(returnObject.perusteenOsa.id, 'perusteenosa', 'LUKU')")
    PerusteenOsaViiteDto.Laaja getByViiteDeep(final Long viiteId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    List<Revision> getVersiot(@P("id") Long id);

    //TODO: versiotietojen lukuoikeus?
    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    PerusteenOsaDto.Laaja getVersio(@P("id") final Long id, final Integer versioId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS') or hasPermission(#id, 'perusteenosa', 'KORJAUS')")
    LukkoDto lock(@P("id") final Long id);

    @PreAuthorize("isAuthenticated()")
    void unlock(final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    LukkoDto getLock(@P("id") final Long id);

    @PreAuthorize("hasPermission(null, 'perusteprojekti', 'LUONTI')")
    <T extends PerusteenOsaDto.Laaja> T addJulkaistuun(PerusteenOsaViite viite, T perusteenOsaDto);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    OsaAlueLaajaDto addTutkinnonOsaOsaAlue(@P("id") final Long id, OsaAlueLaajaDto osaAlueDto);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'MUOKKAUS') or hasPermission(#viiteId, 'tutkinnonosaviite', 'KORJAUS')")
    OsaAlueKokonaanDto updateTutkinnonOsaOsaAlue(@P("viiteId") final Long viiteId, final Long osaAlueId, OsaAlueKokonaanDto osaAlue);

    @PreAuthorize("hasPermission(#viiteId, 'tutkinnonosaviite', 'LUKU')")
    OsaAlueKokonaanDto getTutkinnonOsaOsaAlue(@P("viiteId") final Long viiteId, final Long osaAlueId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    List<OsaAlueKokonaanDto> getTutkinnonOsaOsaAlueet(@P("id") final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    List<OsaAlueKokonaanDto> getTutkinnonOsaOsaAlueetVersio(@P("id") Long id, Integer versioId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    OsaamistavoiteLaajaDto addOsaamistavoite(@P("id") final Long id, final Long osaAlueId, OsaamistavoiteLaajaDto osaamistavoiteDto);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS') or hasPermission(#id, 'perusteenosa', 'KORJAUS')")
    OsaamistavoiteLaajaDto updateOsaamistavoite(@P("id") final Long id, final Long osaAlueId, final Long osaamistavoiteId, OsaamistavoiteLaajaDto osaamistavoite);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'LUKU')")
    List<OsaamistavoiteLaajaDto> getOsaamistavoitteet(@P("id") final Long id, final Long osaAlueId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    void removeOsaamistavoite(@P("id") final Long id, final Long osaAlueId, final Long osaamistavoiteId);

    @PreAuthorize("hasPermission(#id, 'perusteenosa', 'MUOKKAUS')")
    void removeOsaAlue(@P("id") final Long id, final Long osaAlueId);

    @PreAuthorize("hasPermission(#id, 'perusteenosaviite', 'LUKU')")
    List<Revision> getVersiotByViite(@P("id") final Long id);

    @PreAuthorize("hasPermission(#id, 'perusteenosaviite', 'LUKU')")
    PerusteenOsaDto getVersioByViite(@P("id") Long id, Integer versioId);

    @PreAuthorize("permitAll()")
    Revision getLastModifiedRevision(final Long id);

    @PreAuthorize("isAuthenticated()")
    Set<PerusteprojektinPerusteenosaDto> getOwningProjektit(Long id);

    @PreAuthorize("permitAll()")
    Page<TutkinnonOsaDto> findTutkinnonOsatBy(TutkinnonOsaQueryDto pquery);

    @PreAuthorize("permitAll()")
    Page<TutkinnonOsaViiteKontekstiDto> findAllTutkinnonOsatBy(TutkinnonOsaQueryDto pquery);

    @PreAuthorize("permitAll()")
    List<TutkinnonOsaViiteKontekstiDto> findTutkinnonOsaViitteetByTutkinnonOsa(Long tutkinnonOsaId);

    @PreAuthorize("permitAll()")
    List<TutkinnonOsaKaikkiDto> getTutkinnonOsaKaikkiDtoByKoodi(String koodiUri);

}
