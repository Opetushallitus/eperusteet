package fi.vm.sade.eperusteet.dto.yl;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AIPEVaiheLaajaDto extends AIPEVaiheDto {
    private List<AIPEOppiaineLaajaDto> oppiaineet;
}
