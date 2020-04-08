package fi.vm.sade.eperusteet.dto.peruste;

import fi.vm.sade.eperusteet.domain.AikatauluTapahtuma;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PerusteAikatauluDto {

    private Long id;
    private PerusteKevytDto peruste;
    private LokalisoituTekstiDto tavoite;
    private AikatauluTapahtuma tapahtuma;
    private Date tapahtumapaiva;

}
