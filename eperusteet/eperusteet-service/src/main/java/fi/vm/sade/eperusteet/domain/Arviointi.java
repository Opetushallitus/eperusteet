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
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 *
 * @author teele1
 */
@Entity
@Table(name = "arviointi")
public class Arviointi implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private TekstiPalanen lisatiedot;
    
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "arviointi_arvioinninkohdealue", 
            joinColumns = @JoinColumn(name = "arviointi_id"),  
            inverseJoinColumns = @JoinColumn(name = "arvioinninkohdealue_id"))
    @OrderColumn
    private List<ArvioinninKohdealue> arvioinninKohdealueet;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TekstiPalanen getLisatiedot() {
        return lisatiedot;
    }

    public void setLisatiedot(TekstiPalanen lisatiedot) {
        this.lisatiedot = lisatiedot;
    }

    public List<ArvioinninKohdealue> getArvioinninKohdealueet() {
        return arvioinninKohdealueet;
    }

    public void setArvioinninKohdealueet(List<ArvioinninKohdealue> arvioinninKohdealueet) {
        this.arvioinninKohdealueet = arvioinninKohdealueet;
    }
    
}
