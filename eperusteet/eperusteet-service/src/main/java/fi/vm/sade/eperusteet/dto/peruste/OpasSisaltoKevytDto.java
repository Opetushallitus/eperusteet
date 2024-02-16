package fi.vm.sade.eperusteet.dto.peruste;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpasSisaltoKevytDto {

    private Long id;
    private List<OppaanKiinnitettyKoodiDto> oppaanKiinnitetytKoodit;

}
