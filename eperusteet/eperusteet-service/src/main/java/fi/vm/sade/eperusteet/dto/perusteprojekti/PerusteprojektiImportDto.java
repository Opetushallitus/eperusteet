package fi.vm.sade.eperusteet.dto.perusteprojekti;

import fi.vm.sade.eperusteet.dto.liite.LiiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.TermiDto;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerusteprojektiImportDto {
    private PerusteprojektiLuontiDto projekti;
    private PerusteKaikkiDto peruste;
    List<TermiDto> termit;
    HashMap<UUID, byte[]> liitetiedostot;
    List<LiiteDto> liitteet;
}
