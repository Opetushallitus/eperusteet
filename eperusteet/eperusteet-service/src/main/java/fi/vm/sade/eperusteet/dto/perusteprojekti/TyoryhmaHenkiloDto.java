package fi.vm.sade.eperusteet.dto.perusteprojekti;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TyoryhmaHenkiloDto implements Serializable {
    private Long id;
    private String kayttajaOid;
    private String nimi;

    public TyoryhmaHenkiloDto(String nimi, String kayttajaOid) {
        this.kayttajaOid = kayttajaOid;
        this.nimi = nimi;
    }

}
