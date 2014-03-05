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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author harrik
 */
@Entity
@Table(name = "kayttajaprofiili")
public class Kayttajaprofiili implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    private Long id;
    
    @Getter
    @Setter
    private String oid;

    @ManyToMany
    @OrderColumn(name = "suosikki_order")
    @JoinTable(name = "kayttajaprofiili_peruste", 
            joinColumns = @JoinColumn(name = "kayttajaprofiili_id"), 
            inverseJoinColumns = @JoinColumn(name = "peruste_id"))
    @Getter
    @Setter
    private List<Peruste> suosikit;
    
    @ManyToMany
    @OrderColumn(name = "projekti_order")
    @JoinTable(name = "kayttajaprofiili_perusteprojekti", 
            joinColumns = @JoinColumn(name = "kayttajaprofiili_id"), 
            inverseJoinColumns = @JoinColumn(name = "perusteprojekti_id"))
    @Getter
    @Setter
    private List<Perusteprojekti> perusteprojektit;

    public Kayttajaprofiili() {
    }

    public Kayttajaprofiili(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "fi.vm.sade.eperusteet.domain.Kayttajaprofiili[ id=" + id + " ]";
    }

}
