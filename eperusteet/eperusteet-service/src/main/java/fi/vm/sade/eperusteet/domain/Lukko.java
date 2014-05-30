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

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Getter;
import org.joda.time.DateTime;

/**
 *
 * @author jhyoty
 */
@Entity
@Table(name = "lukko")
public class Lukko {

    @Id
    @Getter
    private Long id;

    @Column(name = "haltija_oid")
    @Getter
    private String haltijaOid;

    @Temporal(TemporalType.TIMESTAMP)
    private Date luotu;

    protected Lukko() {
        //JPA
    }

    public Lukko(Long id, String haltijaOid) {
        this.id = id;
        this.haltijaOid = haltijaOid;
        this.luotu = new Date();
    }

    public DateTime getLuotu() {
        return new DateTime(luotu.getTime());
    }

    public void refresh() {
        this.luotu = new Date();
    }

}
