package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
import javax.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@EqualsAndHashCode
public class Diaarinumero implements Serializable {

    public Diaarinumero() {

    }

    public Diaarinumero(String diaarinumero) {
        this.diaarinumero = diaarinumero;
    }

    @Getter
    private String diaarinumero;

    @Override
    public String toString() {
        return diaarinumero;
    }


}
