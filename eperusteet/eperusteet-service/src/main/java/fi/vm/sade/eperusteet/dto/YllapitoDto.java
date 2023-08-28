package fi.vm.sade.eperusteet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YllapitoDto {

    private Long id;
    private String kuvaus;
    private String key;
    private String value;
}
