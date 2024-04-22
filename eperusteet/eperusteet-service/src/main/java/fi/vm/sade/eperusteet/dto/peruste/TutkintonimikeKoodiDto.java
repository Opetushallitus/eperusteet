package fi.vm.sade.eperusteet.dto.peruste;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FIXME: tutkintonimike, osaamisala ja tutkinnon osa käyttämään KoodiDto:ta
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutkintonimikeKoodiDto {
    private Long id;
    private Reference peruste;
    private String tutkinnonOsaUri;
    private String tutkinnonOsaArvo;
    private String osaamisalaUri;
    private String osaamisalaArvo;
    private String tutkintonimikeUri;
    private String tutkintonimikeArvo;
    private LokalisoituTekstiDto nimi;

    @Deprecated
    public TutkintonimikeKoodiDto(Reference peruste, String tutkinnonOsaArvo, String osaamisalaArvo, String tutkintonimikeArvo) {
        this.peruste = peruste;
        this.tutkinnonOsaArvo = tutkinnonOsaArvo;
        this.tutkinnonOsaUri = "tutkinnonosat_" + tutkinnonOsaArvo;
        this.osaamisalaArvo = osaamisalaArvo;
        this.osaamisalaUri = "osaamisala_" + osaamisalaArvo;
        this.tutkintonimikeArvo = tutkintonimikeArvo;
        this.tutkintonimikeUri = "tutkintonimikkeet_" + tutkintonimikeArvo;
    }

    @JsonIgnore
    public boolean isTutkintonimikeTemporary() {
        return tutkintonimikeUri.contains("temporary");
    }

}
