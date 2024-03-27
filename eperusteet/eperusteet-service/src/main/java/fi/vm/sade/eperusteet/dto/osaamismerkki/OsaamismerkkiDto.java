package fi.vm.sade.eperusteet.dto.osaamismerkki;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class OsaamismerkkiDto extends OsaamismerkkiBaseDto {
    private Date muokattu;
    private String muokkaaja;
}
