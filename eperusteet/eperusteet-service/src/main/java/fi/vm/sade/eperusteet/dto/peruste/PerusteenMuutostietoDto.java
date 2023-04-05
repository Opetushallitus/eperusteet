package fi.vm.sade.eperusteet.dto.peruste;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PerusteenMuutostietoDto {
    private NavigationType kohde;
    private List<MuutostapahtumaDto> tapahtumat;
}
