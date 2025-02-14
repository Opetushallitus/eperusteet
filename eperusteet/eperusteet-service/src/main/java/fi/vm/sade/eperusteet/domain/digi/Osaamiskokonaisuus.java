package fi.vm.sade.eperusteet.domain.digi;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
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
