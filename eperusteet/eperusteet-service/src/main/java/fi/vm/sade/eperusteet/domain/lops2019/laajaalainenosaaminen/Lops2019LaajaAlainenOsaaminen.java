package fi.vm.sade.eperusteet.domain.lops2019.laajaalainenosaaminen;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Getter
@Setter
@Entity
@Audited
@Table(name = "yl_lops2019_laaja_alainen_osaaminen")
public class Lops2019LaajaAlainenOsaaminen extends AbstractAuditedReferenceableEntity {

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen nimi;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen opinnot;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "yl_lops2019_laaja_alainen_osaaminen_tavoite",
            joinColumns = @JoinColumn(name = "laaja_alainen_osaaminen_id"),
            inverseJoinColumns = @JoinColumn(name = "tavoite_id"))
    @OrderBy("jarjestys, id")
    private List<Lops2019Tavoite> tavoitteet = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "yl_lops2019_laaja_alainen_osaaminen_painopiste",
            joinColumns = @JoinColumn(name = "laaja_alainen_osaaminen_id"),
            inverseJoinColumns = @JoinColumn(name = "painopiste_id"))
    @OrderBy("jarjestys, id")
    private List<Lops2019Painopiste> painopisteet = new ArrayList<>();

    private Integer jarjestys;

    public boolean structureEquals(Lops2019LaajaAlainenOsaaminen other) {
        boolean result = Objects.equals(this.getId(), other.getId());

        result &= refXnor(this.getNimi(), other.getNimi());

        // tavoitteet
        result &= refXnor(this.getTavoitteet(), other.getTavoitteet());

        if (this.getTavoitteet() != null && other.getTavoitteet() != null) {
            result &= this.getTavoitteet().size() == other.getTavoitteet().size();

            for (Lops2019Tavoite t : this.getTavoitteet()) {
                if (!result) {
                    break;
                }

                for (Lops2019Tavoite ot :other.getTavoitteet()) {
                    if (Objects.equals(t.getId(), ot.getId())) {
                        result &= t.structureEquals(ot);
                    }
                }
            }
        }


        // painopisteet
        result &= refXnor(this.getPainopisteet(), other.getPainopisteet());

        if (this.getPainopisteet() != null && other.getPainopisteet() != null) {
            result &= this.getPainopisteet().size() == other.getPainopisteet().size();

            for (Lops2019Painopiste p : this.getPainopisteet()) {
                if (!result) {
                    break;
                }

                for (Lops2019Painopiste op :other.getPainopisteet()) {
                    if (Objects.equals(p.getId(), op.getId())) {
                        result &= p.structureEquals(op);
                    }
                }
            }
        }

        return result;
    }
}
