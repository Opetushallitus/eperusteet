package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OsaamistasonKriteerit2020Dto {
    private OsaamistasoDto osaamistaso;
    private List<LokalisoituTekstiDto> kriteerit = new ArrayList<>();
}
