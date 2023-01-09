package fi.vm.sade.eperusteet.dto.peruste;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitaalisenOsaamisenSisaltoDto implements PerusteenSisaltoDto {
    private Long id;
    private PerusteenOsaViiteDto.Laaja sisalto;
}