package fi.vm.sade.eperusteet.dto.osaamismerkki;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Julkaistun osaamismerkin tiedot julkisesta rajapinnasta (/api/external/osaamismerkit).")
public class OsaamismerkkiExternalDto extends OsaamismerkkiBaseDto {
}
