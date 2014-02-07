/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 * 
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
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
    
    @ManyToOne
    private TekstiPalanen ammattitaitovaatimukset;
    
    @ManyToOne
    private TekstiPalanen ammattitaidonOsoittamistavat;

    @ManyToOne(fetch = FetchType.LAZY)
    private Arviointi arviointi;

    public TekstiPalanen getTavoitteet() {
        return tavoitteet;
    }

    public void setTavoitteet(TekstiPalanen tavoitteet) {
        this.tavoitteet = tavoitteet;
    }

    public TekstiPalanen getAmmattitaitovaatimukset() {
        return ammattitaitovaatimukset;
    }

    public void setAmmattitaitovaatimukset(TekstiPalanen ammattitaitovaatimukset) {
        this.ammattitaitovaatimukset = ammattitaitovaatimukset;
    }

    public TekstiPalanen getAmmattitaidonOsoittamistavat() {
        return ammattitaidonOsoittamistavat;
    }

    public void setAmmattitaidonOsoittamistavat(TekstiPalanen ammattitaidonOsoittamistavat) {
        this.ammattitaidonOsoittamistavat = ammattitaidonOsoittamistavat;
    }

    public Arviointi getArviointi() {
        return arviointi;
    }

    public void setArviointi(Arviointi arviointi) {
        this.arviointi = arviointi;
    }

}
