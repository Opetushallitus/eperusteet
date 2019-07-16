package fi.vm.sade.eperusteet.dto.perusteprojekti;

import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDokumenttiDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerusteprojektiDokumenttiDto {
    private PerusteDokumenttiDto peruste;
    private ProjektiTila tila;
}
