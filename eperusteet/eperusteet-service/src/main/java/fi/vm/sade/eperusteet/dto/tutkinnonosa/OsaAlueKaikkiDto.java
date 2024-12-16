package fi.vm.sade.eperusteet.dto.tutkinnonosa;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.GeneerinenArviointiasteikkoKaikkiDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OsaAlueKaikkiDto extends OsaAlueDto {

    @Schema(description = "OSAALUE2020-mukainen arviointi")
    private GeneerinenArviointiasteikkoKaikkiDto arviointi;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "OSAALUE2020-mukainen pakolliset osaamistavoitteet")
    private Osaamistavoite2020Dto pakollisetOsaamistavoitteet;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "OSAALUE2020-mukainen valinnaiset osaamistavoittet")
    private Osaamistavoite2020Dto valinnaisetOsaamistavoitteet;

    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "Vanhan malliset osaamistavoitteet (OSAALUE2014)")
    private List<OsaamistavoiteLaajaDto> osaamistavoitteet = new ArrayList<>();
}
