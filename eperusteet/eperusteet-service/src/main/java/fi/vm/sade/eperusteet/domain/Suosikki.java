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
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author harrik
 */
@Entity
@Table(name = "suosikki")
public class Suosikki implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    private Long id;
    
    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "kayttajaprofiili_id")
    private Kayttajaprofiili kayttajaprofiili;
      
    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "peruste_id")
    private Peruste peruste;
    
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private Suoritustapakoodi suoritustapakoodi;
    
}
