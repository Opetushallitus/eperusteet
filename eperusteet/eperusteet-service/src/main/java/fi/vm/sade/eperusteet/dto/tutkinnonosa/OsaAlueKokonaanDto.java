package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.Arviointi2020Dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OsaAlueKokonaanDto extends OsaAlueDto {

    private Arviointi2020Dto arviointi;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Osaamistavoite2020Dto pakollisetOsaamistavoitteet;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Osaamistavoite2020Dto valinnaisetOsaamistavoitteet;

    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<OsaamistavoiteLaajaDto> osaamistavoitteet = new ArrayList<>();
}
