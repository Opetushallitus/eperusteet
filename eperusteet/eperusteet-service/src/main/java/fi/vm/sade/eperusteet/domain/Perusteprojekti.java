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
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
/**
 *
 * @author harrik
 */
@Entity
@Table(name = "perusteprojekti")
public class Perusteprojekti implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    private Long id;
    
    @Getter
    @Setter
    @NotNull(message="Nimi ei voi olla tyhjä")
    private String nimi;

    @OneToOne(fetch = FetchType.LAZY, cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @Getter
    @Setter
    private Peruste peruste;
    
    @Getter
    @Setter
    @NotNull(message="Diaarinumero ei voi olla tyhjä")
    private String diaarinumero;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    private Date paatosPvm;
    
    @Getter
    @Setter
    private String tehtavaluokka;         
}