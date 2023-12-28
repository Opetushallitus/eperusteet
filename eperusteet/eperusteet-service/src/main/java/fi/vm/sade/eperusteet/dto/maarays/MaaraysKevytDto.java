package fi.vm.sade.eperusteet.dto.maarays;

import fi.vm.sade.eperusteet.domain.maarays.MaaraysTila;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaaraysKevytDto {
    private Long id;
    private LokalisoituTekstiDto nimi;
    private String diaarinumero;
    private MaaraysTila tila;
    private Date voimassaoloAlkaa;
}
