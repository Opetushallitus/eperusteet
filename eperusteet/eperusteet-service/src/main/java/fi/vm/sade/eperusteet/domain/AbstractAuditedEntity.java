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

import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Getter;
import org.hibernate.envers.Audited;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kantaluokka entiteeteille joista ylläpidetään luotu/muokattu -tietoja.
 * @author jhyoty
 */
@MappedSuperclass
public abstract class AbstractAuditedEntity implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAuditedEntity.class);

    @Audited
    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date luotu;

    @Audited
    @Getter
    @Column(updatable = false)
    private String luoja;

    @Audited
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date muokattu;

    @Audited
    @Getter
    @Column
    private String muokkaaja;

    public Date getLuotu() {
        return luotu == null ? null : new Date(luotu.getTime());
    }

    public Date getMuokattu() {
        return muokattu == null ? null : new Date(muokattu.getTime());
    }

    public void muokattu() {
        this.muokattu = new Date();
    }

    @PrePersist
    private void prepersist() {
        muokattu = luotu = new Date();
        luoja = muokkaaja = SecurityUtil.getAuthenticatedPrincipal().getName();
    }

    @PreUpdate
    private void preupdate() {
        muokattu = new Date();
        muokkaaja = SecurityUtil.getAuthenticatedPrincipal().getName();
    }

}
