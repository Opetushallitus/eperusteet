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
    private Set<Long> koodit;
    private String kieli = "fi";
    private boolean voimassa = false;
    private boolean tuleva = false;
    private boolean poistunut = false;
}
