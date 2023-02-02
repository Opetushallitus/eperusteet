package fi.vm.sade.eperusteet.domain.digi;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.vst.KotoTaitotaso;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.AuditJoinTable;
import org.hibernate.envers.AuditMappedBy;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "osaamiskokonaisuus")
@Audited
@Getter
@Setter
public class Osaamiskokonaisuus extends PerusteenOsa {

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kuvaus;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen keskeinenKasitteisto;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "osaamiskokonaisuus_kasitteisto_join")
    @OrderColumn
    private List<OsaamiskokonaisuusKasitteisto> kasitteistot = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    private Date muokattu;

    public Osaamiskokonaisuus() {
        kasitteistot = new ArrayList<>(Arrays.asList(
            new OsaamiskokonaisuusKasitteisto(DigitaalinenOsaaminenTaso.VARHAISKASVATUS),
            new OsaamiskokonaisuusKasitteisto(DigitaalinenOsaaminenTaso.ESIOPETUS),
            new OsaamiskokonaisuusKasitteisto(DigitaalinenOsaaminenTaso.VUOSILUOKKA_12),
            new OsaamiskokonaisuusKasitteisto(DigitaalinenOsaaminenTaso.VUOSILUOKKA_3456),
            new OsaamiskokonaisuusKasitteisto(DigitaalinenOsaaminenTaso.VUOSILUOKKA_789)
        ));
    }

    public Osaamiskokonaisuus(Osaamiskokonaisuus other) {
        super(other);
        copyState(other);
    }

    @Override
    public void mergeState(PerusteenOsa perusteenOsa) {
        super.mergeState(perusteenOsa);
        if (perusteenOsa instanceof Osaamiskokonaisuus) {
            Osaamiskokonaisuus other = (Osaamiskokonaisuus) perusteenOsa;
            setKuvaus(other.getKuvaus());
            setKeskeinenKasitteisto(other.getKeskeinenKasitteisto());
            setKasitteistot(other.getKasitteistot());
            preupdate();

        }
    }

    private void copyState(Osaamiskokonaisuus other) {
        if (other == null) {
            return;
        }

        this.kuvaus = other.kuvaus;
        this.keskeinenKasitteisto = other.getKeskeinenKasitteisto();
        setKasitteistot(other.getKasitteistot().stream().map(OsaamiskokonaisuusKasitteisto::new).collect(Collectors.toList()));
    }

    public void setKasitteistot(List<OsaamiskokonaisuusKasitteisto> kasitteistot) {
        this.kasitteistot.clear();
        this.kasitteistot.addAll(kasitteistot);
    }

    @Override
    public PerusteenOsa copy() {
        return new Osaamiskokonaisuus(this);
    }

    @Override
    public Reference getReference() {
        return new Reference(getId());
    }

    @Override
    public NavigationType getNavigationType() {
        return NavigationType.osaamiskokonaisuus;
    }

    @Override
    public boolean structureEquals(PerusteenOsa updated) {
        return true;
    }

    @PreUpdate
    protected void preupdate() {
        muokattu = new Date();
    }

}
