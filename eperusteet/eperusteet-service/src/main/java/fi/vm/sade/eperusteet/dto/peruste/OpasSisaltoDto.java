package fi.vm.sade.eperusteet.dto.peruste;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpasSisaltoDto implements PerusteenSisaltoDto {

    private Long id;
    private PerusteenOsaViiteDto.Laaja sisalto;
    private List<OppaanKiinnitettyKoodiDto> oppaanKiinnitetytKoodit;
}
