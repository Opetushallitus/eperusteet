package fi.vm.sade.eperusteet.dto.julkinen;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TietoaPalvelustaDto {

    private Long id;
    private LokalisoituTekstiDto tietoapalvelustaKuvaus;

}
