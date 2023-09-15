package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerusteenMuokkaustietoLisaparametritDto {

    private NavigationType kohde;
    private Long kohdeId;
}