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
@Table(name = "yl_lops2019_moduuli_sisalto")
public class Lops2019ModuuliSisalto implements Copyable<Lops2019ModuuliSisalto> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kohde;

    @OrderColumn
    @BatchSize(size = 25)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "yl_lops2019_oppiaine_moduuli_sisalto_tekstipalanen",
            joinColumns = @JoinColumn(name = "sisalto_id"),
            inverseJoinColumns = @JoinColumn(name = "tekstipalanen_id"))
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private List<TekstiPalanen> sisallot = new ArrayList<>();

    @Override
    public Lops2019ModuuliSisalto copy(boolean deep) {
        Lops2019ModuuliSisalto result = new Lops2019ModuuliSisalto();
        result.setKohde(TekstiPalanen.of(this.getKohde()));
        result.sisallot.addAll(this.getSisallot().stream()
                .map(TekstiPalanen::of)
                .collect(Collectors.toList()));
        return result;
    }

    public boolean structureEquals(Lops2019ModuuliSisalto other) {
        boolean result = Objects.equals(this.getId(), other.getId());
        result &= refXnor(this.getKohde(), other.getKohde());
        result &= refXnor(this.getSisallot(), other.getSisallot());

        if (this.getSisallot() != null && other.getSisallot() != null) {
            result &= this.getSisallot().size() == other.getSisallot().size();
        }

        return result;
    }
}
