package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.lops2019.laajaalainenosaaminen.Lops2019LaajaAlainenOsaaminenKokonaisuusDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.Lops2019OppiaineDto;
import fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli.Lops2019ModuuliDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface Lops2019Service {

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    PerusteenOsaViiteDto.Matala addSisalto(@P("perusteId") Long perusteId, Long viiteId, PerusteenOsaViiteDto.Matala dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void removeSisalto(@P("perusteId") Long perusteId, Long viiteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    Lops2019LaajaAlainenOsaaminenKokonaisuusDto getLaajaAlainenOsaaminenKokonaisuus(@P("perusteId") Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    Lops2019LaajaAlainenOsaaminenKokonaisuusDto updateLaajaAlainenOsaaminenKokonaisuus(
            @P("perusteId") Long perusteId,
            Lops2019LaajaAlainenOsaaminenKokonaisuusDto dto
    );

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    Lops2019LaajaAlainenOsaaminenDto addLaajaAlainenOsaaminen(@P("perusteId") Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<Lops2019OppiaineDto> getOppiaineet(@P("perusteId") Long perusteId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    List<Lops2019OppiaineDto> sortOppiaineet(@P("perusteId") Long perusteId, List<Lops2019OppiaineDto> oppiaineet);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    Lops2019OppiaineDto addOppiaine(@P("perusteId") Long perusteId, Lops2019OppiaineDto dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    Lops2019OppiaineDto getOppiaine(@P("perusteId") Long perusteId, Long oppiaineId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    Lops2019OppiaineDto updateOppiaine(@P("perusteId") Long perusteId, Lops2019OppiaineDto dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void removeOppiaine(@P("perusteId") Long perusteId, Long id);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'LUKU')")
    Lops2019ModuuliDto getModuuli(@P("perusteId") Long perusteId, Long oppiaineId, Long moduuliId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    Lops2019ModuuliDto updateModuuli(@P("perusteId") Long perusteId, Lops2019ModuuliDto dto);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void removeModuuli(@P("perusteId") Long perusteId, Long oppiaineId, Long moduuliId);

    @PreAuthorize("hasPermission(#perusteId, 'peruste', 'MUOKKAUS')")
    void palautaOppiaineenModuulit(@P("perusteId") Long perusteId, Long id);
}
