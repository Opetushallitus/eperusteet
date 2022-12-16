package fi.vm.sade.eperusteet.dto.lops2019.oppiaineet.moduuli;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lops2019ModuuliSisaltoDto {
    private LokalisoituTekstiDto kohde;
    private List<LokalisoituTekstiDto> sisallot;
}
