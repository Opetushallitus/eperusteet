package fi.vm.sade.eperusteet.dto.util;

import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PerusteenOsaUpdateDto extends UpdateDto<PerusteenOsaDto.Laaja> {
}
