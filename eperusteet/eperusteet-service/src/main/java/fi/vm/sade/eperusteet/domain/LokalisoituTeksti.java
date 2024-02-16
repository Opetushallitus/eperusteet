package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

@Embeddable
public class LokalisoituTeksti implements Serializable {

    public LokalisoituTeksti() {
        //NOP
    }

    public LokalisoituTeksti(Kieli kieli, String teksti) {
        this.kieli = kieli;
        this.teksti = teksti;
    }

    @Column(insertable = true, updatable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private Kieli kieli;

    @NotNull
    @Column(columnDefinition = "TEXT", insertable = true, updatable = false)
    private String teksti;

    public Kieli getKieli() {
        return kieli;
    }

    public String getTeksti() {
        return teksti;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.kieli);
        hash = 47 * hash + Objects.hashCode(this.teksti);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LokalisoituTeksti) {
            final LokalisoituTeksti other = (LokalisoituTeksti) obj;
            return this.kieli == other.kieli && Objects.equals(this.teksti, other.teksti);
        }
        return false;
    }

}
