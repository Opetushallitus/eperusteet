package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import lombok.Getter;
import org.joda.time.DateTime;

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

    @Transient
    private DateTime vanhentuu;

    protected Lukko() {
        //JPA
    }

    public Lukko(Long id, String haltijaOid, int vanhenemisAika) {
        this.id = id;
        this.haltijaOid = haltijaOid;
        this.luotu = new Date();
        this.vanhentuu = new DateTime(luotu.getTime()).plusSeconds(vanhenemisAika);
    }

    public DateTime getLuotu() {
        return new DateTime(luotu.getTime());
    }

    public void refresh() {
        this.luotu = new Date();
    }

    public DateTime getVanhentuu() {
        return vanhentuu;
    }

    public boolean isOma() {
        return haltijaOid.equals(SecurityUtil.getAuthenticatedPrincipal().getName());
    }

    public void setVanhentumisAika(int maxLockTime) {
        DateTime t = getLuotu().plusSeconds(maxLockTime);
        if (this.vanhentuu == null || t.equals(this.vanhentuu)) {
            this.vanhentuu = t;
        } else {
            throw new IllegalStateException("vanhenemisaikaa ei voi muuttaa");
        }
    }

}
