package fi.vm.sade.eperusteet.dto.yl;

import fi.vm.sade.eperusteet.dto.Reference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OppiaineenVuosiluokkaKokonaisuusSuppeaDto {
    private Long id;
    private Reference vuosiluokkaKokonaisuus;
}
