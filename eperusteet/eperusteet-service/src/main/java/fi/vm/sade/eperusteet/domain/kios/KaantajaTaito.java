package fi.vm.sade.eperusteet.domain.kios;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Entity
@Table(name = "kaantaja_taito")
@Audited
@Getter
@Setter
public class KaantajaTaito extends PerusteenOsa {

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen valiotsikko;

    @OrderColumn
    @NotAudited
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "kaantajaTaito", orphanRemoval = true)
    private List<KaantajaTaitoKohdealue> kohdealueet = new ArrayList<>();

    public KaantajaTaito() {
    }

    public KaantajaTaito(KaantajaTaito other) {
        super(other);
        copyState(other);
    }

    @Override
    public KaantajaTaito copy() {
        return new KaantajaTaito(this);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.kaantajataito;
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof KaantajaTaito) {
            KaantajaTaito other = (KaantajaTaito) perusteenOsa;
            setNimi(other.getNimi());
            setKuvaus(other.getKuvaus());
            setValiotsikko(other.getValiotsikko());

            this.kohdealueet.clear();
            for (KaantajaTaitoKohdealue kohdealue : other.getKohdealueet()) {
                KaantajaTaitoKohdealue newKohdealue = new KaantajaTaitoKohdealue(kohdealue);
                newKohdealue.setKaantajaTaito(this);
                this.kohdealueet.add(newKohdealue);
            }
        }
    }

    @Override
    public boolean structureEquals(PerusteenOsa updated) {
        boolean result = false;
        if (updated instanceof KaantajaTaito) {
            KaantajaTaito that = (KaantajaTaito) updated;
            result = super.structureEquals(that);
            result &= getKuvaus() == null || refXnor(getKuvaus(), that.getKuvaus());
            result &= getValiotsikko() == null || refXnor(getValiotsikko(), that.getValiotsikko());

            if (result && getKohdealueet() != null) {
                Iterator<KaantajaTaitoKohdealue> i = getKohdealueet().iterator();
                Iterator<KaantajaTaitoKohdealue> j = that.getKohdealueet().iterator();
                while (result && i.hasNext() && j.hasNext()) {
                    result &= Objects.equals(i.next(), j.next());
                }
                result &= !i.hasNext();
                result &= !j.hasNext();
            }
        }
        return result;
    }

    private void copyState(KaantajaTaito other) {
        if (other == null) {
            return;
        }

        this.kuvaus = other.getKuvaus();
        this.valiotsikko = other.getValiotsikko();
        this.kohdealueet = other.getKohdealueet().stream()
                .map(KaantajaTaitoKohdealue::new)
                .peek(kohdealue -> kohdealue.setKaantajaTaito(this))
                .collect(Collectors.toList());
    }
}

