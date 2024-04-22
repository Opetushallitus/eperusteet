package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import java.util.List;

import fi.vm.sade.eperusteet.dto.Arviointi2020Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OsaAlueLaajaDto extends OsaAlueDto {
    private Arviointi2020Dto arviointi;
    private Osaamistavoite2020Dto pakollisetOsaamistavoitteet;
    private Osaamistavoite2020Dto valinnaisetOsaamistavoitteet;

    @Deprecated
    private List<OsaamistavoiteDto> osaamistavoitteet;
}
