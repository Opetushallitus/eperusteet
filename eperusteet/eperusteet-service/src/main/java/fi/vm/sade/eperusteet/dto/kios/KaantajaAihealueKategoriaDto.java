package fi.vm.sade.eperusteet.dto.kios;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KaantajaAihealueKategoriaDto {

    private Long id;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kuvaus;
    private LokalisoituTekstiDto perustaso;
    private LokalisoituTekstiDto keskitaso;
    private LokalisoituTekstiDto ylintaso;
}

