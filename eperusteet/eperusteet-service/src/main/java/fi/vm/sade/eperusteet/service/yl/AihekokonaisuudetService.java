package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.domain.yl.lukio.Aihekokonaisuudet;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.dto.yl.lukio.AihekokonaisuudetYleiskuvausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.AihekokonaisuusListausDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukioAihekokonaisuusLuontiDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.LukioAihekokonaisuusMuokkausDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface AihekokonaisuudetService {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<AihekokonaisuusListausDto> getAihekokonaisuudet(Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    AihekokonaisuudetYleiskuvausDto getAihekokonaisuudetYleiskuvaus(Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    LukioAihekokonaisuusMuokkausDto getLukioAihekokobaisuusMuokkausById(long perusteId, long aihekokonaisuusId) throws NotExistsException;

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    long luoAihekokonaisuus(long perusteId, LukioAihekokonaisuusLuontiDto aihekokonaisuusLuontiDto) throws BusinessRuleViolationException;

    @SuppressWarnings({"TransactionalAnnotations", "ServiceMethodEntity"})
    @PreAuthorize("isAuthenticated()")
    Aihekokonaisuudet initAihekokonaisuudet(LukiokoulutuksenPerusteenSisalto sisalto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void muokkaaAihekokonaisuutta(long perusteId, LukioAihekokonaisuusMuokkausDto lukioAihekokonaisuusMuokkausDto) throws NotExistsException;

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void tallennaYleiskuvaus(Long perusteId, AihekokonaisuudetYleiskuvausDto aihekokonaisuudetYleiskuvausDto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void poistaAihekokonaisuus(long perusteId, long aihekokonaisuusId) throws NotExistsException;

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<Revision> getAihekokonaisuudetYleiskuvausVersiot(long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    AihekokonaisuudetYleiskuvausDto palautaAihekokonaisuudetYleiskuvaus(long perusteId, int revisio);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    AihekokonaisuudetYleiskuvausDto getAihekokonaisuudetYleiskuvausByVersion(long perusteId, int revisio);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<Revision> getAihekokonaisuusVersiot(long perusteId, long aihekokonaisuusId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    LukioAihekokonaisuusMuokkausDto palautaAihekokonaisuus(long perusteId, long aihekokonaisuusId, int revisio);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    LukioAihekokonaisuusMuokkausDto getAihekokonaisuusByVersion(long perusteId, long aihekokonaisuusId, int revisio);

}
