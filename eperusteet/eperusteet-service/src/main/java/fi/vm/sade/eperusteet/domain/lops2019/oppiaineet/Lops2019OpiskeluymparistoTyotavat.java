package fi.vm.sade.eperusteet.domain.lops2019.oppiaineet;

import fi.vm.sade.eperusteet.domain.Copyable;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.util.Objects;

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
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
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
