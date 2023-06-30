package fi.vm.sade.eperusteet.dto.kayttaja;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessioKayttaja implements Serializable {
    private String redirectUrl;
}
