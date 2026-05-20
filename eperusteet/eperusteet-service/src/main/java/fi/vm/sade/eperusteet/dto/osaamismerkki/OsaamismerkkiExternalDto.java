package fi.vm.sade.eperusteet.dto.osaamismerkki;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Julkaistun osaamismerkin tiedot julkisesta rajapinnasta (/api/external/osaamismerkit).")
public class OsaamismerkkiExternalDto extends OsaamismerkkiBaseDto {

    @Schema(description = "Osaamismerkin voimassaolon alkamispäivä.")
    private Date voimassaoloAlkaa;

    @Schema(description = "Osaamismerkin voimassaolon päättymispäivä.")
    private Date voimassaoloLoppuu;
}
