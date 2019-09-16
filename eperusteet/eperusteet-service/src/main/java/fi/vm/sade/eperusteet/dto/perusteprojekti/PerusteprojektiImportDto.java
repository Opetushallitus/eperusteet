package fi.vm.sade.eperusteet.dto.perusteprojekti;

import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerusteprojektiImportDto {
    private PerusteprojektiLuontiDto projekti;
    private PerusteKaikkiDto peruste;
}
