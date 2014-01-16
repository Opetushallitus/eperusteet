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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 * Perusteen rakenneosanen. Muodostaa puun jonka solmut osoittavat perusteeseen kuuluviin perusteen osiin
 * määritellyssä järjestyksessä ja hierarkiassa.
 * @author jhyoty
 */
@Entity
@Table(name = "perusteenosaviite")
public class PerusteenOsaViite implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    @ManyToOne
    @JsonBackReference
    protected PerusteenOsaViite vanhempi;

    @ManyToOne(fetch = FetchType.EAGER)
    private PerusteenOsa perusteenOsa;

    @OneToMany(mappedBy = "vanhempi", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderColumn
    @JsonManagedReference
    private List<PerusteenOsaViite> lapset;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PerusteenOsaViite getVanhempi() {
        return vanhempi;
    }

    public void setVanhempi(PerusteenOsaViite vanhempi) {
        this.vanhempi = vanhempi;
    }

    public List<PerusteenOsaViite> getLapset() {
        return lapset;
    }

    public void setLapset(List<PerusteenOsaViite> viiteet) {
        this.lapset = viiteet;
    }

    public PerusteenOsa getPerusteenOsa() {
        return perusteenOsa;
    }

    public void setPerusteenOsa(PerusteenOsa perusteenOsa) {
        this.perusteenOsa = perusteenOsa;
    }
    
}
