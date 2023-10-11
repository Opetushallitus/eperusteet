package fi.vm.sade.eperusteet.dto.osaamismerkki;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OsaamismerkkiQuery {
    private int sivu = 0;
    private int sivukoko = 25;
    private String nimi;
    private long kategoria;
    private List<String> tila;
    private boolean voimassaolo;
    private boolean julkaistu = true;
    private boolean laadinta = true;
}
