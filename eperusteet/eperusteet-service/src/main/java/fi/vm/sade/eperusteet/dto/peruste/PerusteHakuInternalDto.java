package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PerusteHakuInternalDto extends PerusteHakuDto {
    private PerusteprojektiDto perusteprojekti;
}
