package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.AbstractReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Audited
@Table(name="yl_vlkok_laaja_osaaminen")
public class VuosiluokkaKokonaisuudenLaajaalainenOsaaminen extends AbstractReferenceableEntity {

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(nullable = false)
    private LaajaalainenOsaaminen laajaalainenOsaaminen;

    @RelatesToPeruste
    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @NotNull
    @JoinColumn(updatable = false, nullable = false)
    private VuosiluokkaKokonaisuus vuosiluokkaKokonaisuus;

    @Getter
    @Setter
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml
    private TekstiPalanen kuvaus;

    public void setVuosiluokkaKokonaisuus(VuosiluokkaKokonaisuus vuosiluokkaKokonaisuus) {
        if ( this.vuosiluokkaKokonaisuus == null || this.vuosiluokkaKokonaisuus.equals(vuosiluokkaKokonaisuus) ) {
            this.vuosiluokkaKokonaisuus = vuosiluokkaKokonaisuus;
        } else {
            throw new IllegalStateException("Vuosiluokkakokonaisuuteen kuulumista ei voi muuttaa");
        }
    }


}
