package fi.vm.sade.eperusteet.dto.perusteprojekti;

import fi.vm.sade.eperusteet.dto.peruste.PerusteKoulutuskoodiDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerusteprojektiKoulutuskoodiDto {
    private Long id;
    private PerusteKoulutuskoodiDto peruste;
}
