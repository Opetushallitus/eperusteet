package fi.vm.sade.eperusteet.domain.kios;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.liite.Liitteellinen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "kaantaja_aihealue")
@Audited
@Getter
@Setter
public class KaantajaAihealue extends PerusteenOsa implements Liitteellinen {

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @OrderColumn
    @NotAudited
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "kaantajaAihealue", orphanRemoval = true)
    private List<KaantajaAihealueKategoria> kategoriat = new ArrayList<>();

    @Getter
    @Setter
    @NotNull
    private boolean liite = false;

    public KaantajaAihealue() {
    }

    public KaantajaAihealue(KaantajaAihealue other) {
        super(other);
        copyState(other);
    }

    @Override
    public KaantajaAihealue copy() {
        return new KaantajaAihealue(this);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.kaantajaaihealue;
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof KaantajaAihealue) {
            KaantajaAihealue other = (KaantajaAihealue) perusteenOsa;
            setNimi(other.getNimi());
            setKuvaus(other.getKuvaus());
            setLiite(other.isLiite());

            this.kategoriat.clear();
            for (KaantajaAihealueKategoria kategoria : other.getKategoriat()) {
                KaantajaAihealueKategoria newKategoria = new KaantajaAihealueKategoria(kategoria);
                newKategoria.setKaantajaAihealue(this);
                this.kategoriat.add(newKategoria);
            }
        }
    }

    @Override
    public boolean structureEquals(PerusteenOsa updated) {
        boolean result = false;
        if (updated instanceof KaantajaAihealue) {
            KaantajaAihealue that = (KaantajaAihealue) updated;
            result = super.structureEquals(that);
            result &= getKuvaus() == null || refXnor(getKuvaus(), that.getKuvaus());

            if (result && getKategoriat() != null) {
                Iterator<KaantajaAihealueKategoria> i = getKategoriat().iterator();
                Iterator<KaantajaAihealueKategoria> j = that.getKategoriat().iterator();
                while (result && i.hasNext() && j.hasNext()) {
                    result &= Objects.equals(i.next(), j.next());
                }
                result &= !i.hasNext();
                result &= !j.hasNext();
            }
        }
        return result;
    }

    private void copyState(KaantajaAihealue other) {
        if (other == null) {
            return;
        }

        this.kuvaus = other.getKuvaus();
        this.kategoriat = other.getKategoriat().stream()
                .map(KaantajaAihealueKategoria::new)
                .peek(kategoria -> kategoria.setKaantajaAihealue(this))
                .collect(Collectors.toList());
    }
}

