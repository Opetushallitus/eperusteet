package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PerusteHakuInternalDto extends PerusteHakuDto {
    private PerusteprojektiDto perusteprojekti;
}
