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

import java.io.Serializable;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 *
 * @author teele1
 */
@Entity
@Table(name = "arvioinninkohde")
public class ArvioinninKohde implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private TekstiPalanen otsikko;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull
    private Arviointiasteikko Arviointiasteikko;
    
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private Set<OsaamistasonKriteeri> osaamistasonKriteerit;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TekstiPalanen getOtsikko() {
        return otsikko;
    }

    public void setOtsikko(TekstiPalanen otsikko) {
        this.otsikko = otsikko;
    }

    public Arviointiasteikko getArviointiasteikko() {
        return Arviointiasteikko;
    }

    public void setArviointiasteikko(Arviointiasteikko Arviointiasteikko) {
        this.Arviointiasteikko = Arviointiasteikko;
    }

    public Set<OsaamistasonKriteeri> getOsaamistasonKriteerit() {
        return osaamistasonKriteerit;
    }

    public void setOsaamistasonKriteerit(Set<OsaamistasonKriteeri> osaamistasonKriteerit) {
        this.osaamistasonKriteerit = osaamistasonKriteerit;
    }
    
}
