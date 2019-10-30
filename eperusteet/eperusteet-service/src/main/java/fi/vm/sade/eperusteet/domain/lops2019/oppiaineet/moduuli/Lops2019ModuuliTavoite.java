package fi.vm.sade.eperusteet.domain.lops2019.oppiaineet.moduuli;

import fi.vm.sade.eperusteet.domain.Copyable;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Getter
@Setter
@Entity
@Audited
@Table(name = "yl_lops2019_oppiaine_moduuli_tavoite")
public class Lops2019ModuuliTavoite implements Copyable<Lops2019ModuuliTavoite> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Getter
    @Setter
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kohde;

    @Getter
    @Setter
    @OrderColumn
    @BatchSize(size = 25)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "yl_lops2019_oppiaine_moduuli_tavoite_tekstipalanen",
            joinColumns = @JoinColumn(name = "tavoite_id"),
            inverseJoinColumns = @JoinColumn(name = "tekstipalanen_id"))
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private List<TekstiPalanen> tavoitteet = new ArrayList<>();

    @Override
    public Lops2019ModuuliTavoite copy(boolean deep) {
        Lops2019ModuuliTavoite result = new Lops2019ModuuliTavoite();
        result.setKohde(TekstiPalanen.of(this.getKohde()));
        result.tavoitteet.addAll(this.getTavoitteet().stream()
            .map(TekstiPalanen::of)
            .collect(Collectors.toList()));
        return result;
    }

    public boolean structureEquals(Lops2019ModuuliTavoite other) {
        boolean result = true;
        result &= refXnor(this.getTavoitteet(), other.getTavoitteet());

        if (this.getTavoitteet() != null && other.getTavoitteet() != null) {
            result &= this.getTavoitteet().size() == other.getTavoitteet().size();
        }

        return result;
    }
}
