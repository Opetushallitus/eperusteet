package fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("rakenne")
@NoArgsConstructor
public class LukioOpetussuunnitelmaRakenneSuppeaDto extends PerusteenOsaDto.Suppea {
}
