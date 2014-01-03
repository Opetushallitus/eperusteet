/*
 * Copyright Gofore Oy. 
 * http://www.gofore.com/ 
 */
package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author jhyoty
 */
@Entity
@Table(name = "tutkinnonosa")
@JsonTypeName("tutkinnonosa")
public class TutkinnonOsa extends PerusteenOsa implements Serializable {

    @ManyToOne
    private TekstiPalanen tavoitteet;

    public TekstiPalanen getTavoitteet() {
        return tavoitteet;
    }

    public void setTavoitteet(TekstiPalanen tavoitteet) {
        this.tavoitteet = tavoitteet;
    }

}
