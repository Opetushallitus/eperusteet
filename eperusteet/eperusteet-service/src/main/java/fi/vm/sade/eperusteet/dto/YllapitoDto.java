package fi.vm.sade.eperusteet.dto;

import lombok.Data;

@Data
public class YllapitoDto {

    private Long id;
    private String ominaisuus;
    private Boolean sallittu;
    private String url;

}
