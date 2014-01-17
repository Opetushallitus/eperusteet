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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 *
 * @author teele1
 */
@Entity
@Table(name = "osaamistasonkriteeri")
public class OsaamistasonKriteeri implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Osaamistaso Osaamistaso;
    
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @OrderColumn
    @JoinTable(name = "osaamistasonkriteeri_tekstipalanen", 
            joinColumns = @JoinColumn(name = "osaamistasonkriteeri_id"), 
            inverseJoinColumns = @JoinColumn(name = "tekstipalanen_id"))
    private List<TekstiPalanen> kriteerit;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Osaamistaso getOsaamistaso() {
        return Osaamistaso;
    }

    public void setOsaamistaso(Osaamistaso Osaamistaso) {
        this.Osaamistaso = Osaamistaso;
    }

    public List<TekstiPalanen> getKriteerit() {
        return kriteerit;
    }

    public void setKriteerit(List<TekstiPalanen> kriteerit) {
        this.kriteerit = kriteerit;
    }

}
