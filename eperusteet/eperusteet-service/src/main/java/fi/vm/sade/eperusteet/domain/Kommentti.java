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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author nkala
 */
@Entity
@Table(name = "kommentti")
public class Kommentti extends AbstractAuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @Column(name="poistettu")
    private Boolean poistettu;

    @Getter
    @Setter
    @Column(name="ylin_id")
    private Long ylinId;

    @Getter
    @Setter
    @Column(name="parent_id")
    private Long parentId;

    @Getter
    @Setter
    @Column(name="perusteprojekti_id")
    private Long perusteprojektiId;

    @Getter
    @Setter
    @Column(name="sisalto")
    private String sisalto;

    @Getter
    @Setter
    @Column(name="viite_suoritustapa")
    private String suoritustapa;

    @Getter
    @Setter
    @Column(name="viite_perusteenosa_id")
    private Long perusteenOsaId;
}