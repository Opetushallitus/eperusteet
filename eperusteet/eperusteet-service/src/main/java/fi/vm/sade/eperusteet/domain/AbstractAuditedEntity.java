package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Kantaluokka entiteeteille joista ylläpidetään luotu/muokattu -tietoja.
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
    @Setter
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
    protected void preupdate() {
        muokattu = new Date();
        muokkaaja = SecurityUtil.getAuthenticatedPrincipal().getName();
    }

}
