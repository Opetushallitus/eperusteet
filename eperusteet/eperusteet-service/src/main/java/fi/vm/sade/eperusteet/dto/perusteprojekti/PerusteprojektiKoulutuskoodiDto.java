package fi.vm.sade.eperusteet.dto.perusteprojekti;

import fi.vm.sade.eperusteet.dto.peruste.PerusteKoulutuskoodiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerusteprojektiKoulutuskoodiDto {
    private Long id;
    private PerusteKoulutuskoodiDto peruste;
}
