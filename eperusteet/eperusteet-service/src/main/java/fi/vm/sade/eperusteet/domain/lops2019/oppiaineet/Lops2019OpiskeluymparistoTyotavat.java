package fi.vm.sade.eperusteet.domain.lops2019.oppiaineet;

import fi.vm.sade.eperusteet.domain.Copyable;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Getter
@Setter
@Entity
@Audited
@Table(name = "yl_lops2019_oppiaine_opiskeluymparisto_tyotavat")
public class Lops2019OpiskeluymparistoTyotavat implements Copyable<Lops2019OpiskeluymparistoTyotavat> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    public boolean structureEquals(Lops2019OpiskeluymparistoTyotavat other) {
        return Objects.equals(this.getId(), other.getId());
    }

    @Override
    public Lops2019OpiskeluymparistoTyotavat copy(boolean deep) {
        Lops2019OpiskeluymparistoTyotavat opiskeluymparistoTyotavat = new Lops2019OpiskeluymparistoTyotavat();
        opiskeluymparistoTyotavat.setKuvaus(TekstiPalanen.of(this.getKuvaus()));
        return opiskeluymparistoTyotavat;
    }
}
