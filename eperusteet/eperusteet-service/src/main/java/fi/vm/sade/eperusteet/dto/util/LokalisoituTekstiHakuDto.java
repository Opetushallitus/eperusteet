package fi.vm.sade.eperusteet.dto.util;

import fi.vm.sade.eperusteet.domain.Kieli;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LokalisoituTekstiHakuDto {
    private long id;
    
    @Setter(AccessLevel.NONE)
    private Kieli kieli;
    
    private String teksti;

    public void setKieli(Object value) {
        if (value == null) {
            this.kieli = null;
        } else if (value instanceof String) {
            this.kieli = Kieli.of((String) value);
        } else if (value instanceof Kieli) {
            this.kieli = (Kieli) value;
        } else {
            throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to Kieli");
        }
    }
}
