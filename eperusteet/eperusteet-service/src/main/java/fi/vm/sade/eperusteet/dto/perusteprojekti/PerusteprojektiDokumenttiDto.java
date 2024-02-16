package fi.vm.sade.eperusteet.dto.perusteprojekti;

import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDokumenttiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerusteprojektiDokumenttiDto {
    private PerusteDokumenttiDto peruste;
    private ProjektiTila tila;
}
