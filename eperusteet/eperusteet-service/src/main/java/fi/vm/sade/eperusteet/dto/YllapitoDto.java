package fi.vm.sade.eperusteet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YllapitoDto {

    private Long id;
    private String ominaisuus;
    private Boolean sallittu;
    private String url;

}
