package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.service.impl.AuditRevisionListener;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

@Entity
@Table(name = "revinfo")
@RevisionEntity(AuditRevisionListener.class)
@AttributeOverrides({
    @AttributeOverride(name = "id", column = @Column(name = "rev")),
    @AttributeOverride(name = "timestamp", column = @Column(name = "revtstmp"))
})
@Getter
@Setter
public class RevisionInfo extends DefaultRevisionEntity {

    private static final int MAX_LEN = 1000;

    @Column
    private String muokkaajaOid;

    @Column(length = MAX_LEN)
    private String kommentti;

    public void addKommentti(String kommentti) {
        if (kommentti == null) {
            return;
        }
        if (this.kommentti == null) {
            this.kommentti = kommentti;
        } else if (this.kommentti.length() < (MAX_LEN - 2)) {
            this.kommentti = this.kommentti + ("; " + kommentti).substring(0, Math.min(MAX_LEN - this.kommentti.length() - 2, kommentti.length()));
        }
    }
}
