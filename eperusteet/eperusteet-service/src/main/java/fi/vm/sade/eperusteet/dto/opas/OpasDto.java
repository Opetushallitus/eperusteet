package fi.vm.sade.eperusteet.dto.opas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.Reference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class OpasDto {
    private Long id;
    private String nimi;
    private Reference peruste;
    private ProjektiTila tila;
    private String ryhmaOid;
}
