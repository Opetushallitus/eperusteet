package fi.vm.sade.eperusteet.dto.kayttaja;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuosikkiDto implements Serializable {
    private Long id;
    private String nimi;
    private String sisalto;
    private Date lisatty;
}
