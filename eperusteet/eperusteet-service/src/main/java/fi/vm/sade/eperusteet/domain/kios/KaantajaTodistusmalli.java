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
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import static fi.vm.sade.eperusteet.service.util.Util.refXnor;

@Entity
@Table(name = "kaantaja_todistusmalli")
@Audited
@Getter
@Setter
public class KaantajaTodistusmalli extends PerusteenOsa implements Liitteellinen {

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private KaantajaTodistusmalliTaitotasokuvaus ylintaso;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private KaantajaTodistusmalliTaitotasokuvaus keskitaso;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private KaantajaTodistusmalliTaitotasokuvaus perustaso;

    @Getter
    @Setter
    @NotNull
    private boolean liite = false;

    public KaantajaTodistusmalli() {
    }

    public KaantajaTodistusmalli(KaantajaTodistusmalli other) {
        super(other);
        copyState(other);
    }

    @Override
    public KaantajaTodistusmalli copy() {
        return new KaantajaTodistusmalli(this);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.kaantajatodistusmalli;
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof KaantajaTodistusmalli) {
            KaantajaTodistusmalli other = (KaantajaTodistusmalli) perusteenOsa;
            setNimi(other.getNimi());
            setKuvaus(other.getKuvaus());
            setLiite(other.isLiite());

            if (other.getYlintaso() != null) {
                this.ylintaso = new KaantajaTodistusmalliTaitotasokuvaus(other.getYlintaso());
            } else {
                this.ylintaso = null;
            }

            if (other.getKeskitaso() != null) {
                this.keskitaso = new KaantajaTodistusmalliTaitotasokuvaus(other.getKeskitaso());
            } else {
                this.keskitaso = null;
            }

            if (other.getPerustaso() != null) {
                this.perustaso = new KaantajaTodistusmalliTaitotasokuvaus(other.getPerustaso());
            } else {
                this.perustaso = null;
            }
        }
    }

    @Override
    public boolean structureEquals(PerusteenOsa updated) {
        boolean result = false;
        if (updated instanceof KaantajaTodistusmalli) {
            KaantajaTodistusmalli that = (KaantajaTodistusmalli) updated;
            result = super.structureEquals(that);
            result &= getKuvaus() == null || refXnor(getKuvaus(), that.getKuvaus());
            result &= getYlintaso() == null || refXnor(getYlintaso(), that.getYlintaso());
            result &= getKeskitaso() == null || refXnor(getKeskitaso(), that.getKeskitaso());
            result &= getPerustaso() == null || refXnor(getPerustaso(), that.getPerustaso());
        }
        return result;
    }

    private void copyState(KaantajaTodistusmalli other) {
        if (other == null) {
            return;
        }

        this.kuvaus = other.getKuvaus();
        this.ylintaso = other.getYlintaso() != null ? new KaantajaTodistusmalliTaitotasokuvaus(other.getYlintaso()) : null;
        this.keskitaso = other.getKeskitaso() != null ? new KaantajaTodistusmalliTaitotasokuvaus(other.getKeskitaso()) : null;
        this.perustaso = other.getPerustaso() != null ? new KaantajaTodistusmalliTaitotasokuvaus(other.getPerustaso()) : null;
    }
}

