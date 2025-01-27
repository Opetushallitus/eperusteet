package fi.vm.sade.eperusteet.domain.lops2019.oppiaineet;

import fi.vm.sade.eperusteet.domain.Copyable;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.*;
import java.util.Objects;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Getter
@Setter
@Entity
@Audited
@Table(name = "yl_lops2019_oppiaine_laaja_alainen_osaaminen")
public class Lops2019OppiaineLaajaAlainenOsaaminen implements Copyable<Lops2019OppiaineLaajaAlainenOsaaminen> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    public boolean structureEquals(Lops2019OppiaineLaajaAlainenOsaaminen other) {
        boolean result = Objects.equals(this.getId(), other.getId());

        result &= refXnor(this.getKuvaus(), other.getKuvaus());

        return result;
    }

    @Override
    public Lops2019OppiaineLaajaAlainenOsaaminen copy(boolean deep) {
        Lops2019OppiaineLaajaAlainenOsaaminen lao = new Lops2019OppiaineLaajaAlainenOsaaminen();
        lao.setKuvaus(TekstiPalanen.of(this.getKuvaus()));
        return lao;
    }
}
