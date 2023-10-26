package fi.vm.sade.eperusteet.dto.osaamismerkki;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OsaamismerkkiQuery {
    private int sivu = 0;
    private int sivukoko = 25;
    private String nimi;
    private Long kategoria;
    private Set<String> tila;
    private boolean voimassa = false;
    private boolean tuleva = false;
    private boolean poistunut = false;
}
