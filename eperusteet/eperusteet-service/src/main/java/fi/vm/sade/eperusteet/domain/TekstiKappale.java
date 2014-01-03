/*
 * Copyright Gofore Oy. 
 * http://www.gofore.com/ 
 */
package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author jhyoty
 */
@Entity
@Table(name = "tekstikappale")
@JsonTypeName("tekstiosa")
public class TekstiKappale extends PerusteenOsa implements Serializable {

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private TekstiPalanen teksti;

    public TekstiPalanen getTeksti() {
        return teksti;
    }

    public void setTeksti(TekstiPalanen teksti) {
        this.teksti = teksti;
    }
    
}
