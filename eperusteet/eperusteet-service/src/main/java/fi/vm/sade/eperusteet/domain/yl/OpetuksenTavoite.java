package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.AbstractReferenceableEntity;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "yl_opetuksen_tavoite")
@Audited
public class OpetuksenTavoite extends AbstractReferenceableEntity {

    @NotNull
    @Column(updatable = false)
    @Getter
    private UUID tunniste = UUID.randomUUID();

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    private TekstiPalanen tavoite;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    private TekstiPalanen tavoitteistaJohdetutOppimisenTavoitteet;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    private TekstiPalanen arvioinninOtsikko;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    private TekstiPalanen arvioinninKuvaus;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    private TekstiPalanen vapaaTeksti;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    private TekstiPalanen arvioinninOsaamisenKuvaus;

    @Getter
    @Setter
    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name="yl_opetuksen_tavoite_yl_keskeinen_sisaltoalue",
            joinColumns = @JoinColumn(name = "yl_opetuksen_tavoite_id"),
            inverseJoinColumns = @JoinColumn(name = "sisaltoalueet_id"))
    private Set<KeskeinenSisaltoalue> sisaltoalueet = new HashSet<>();

    @Getter
    @Setter
    @ManyToMany
    @JoinTable(name="yl_opetuksen_tavoite_yl_laajaalainen_osaaminen",
            joinColumns = @JoinColumn(name = "yl_opetuksen_tavoite_id"),
            inverseJoinColumns = @JoinColumn(name = "laajattavoitteet_id"))
    private Set<LaajaalainenOsaaminen> laajattavoitteet = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name="yl_opetuksen_tavoite_yl_tavoitteen_arviointi",
            joinColumns = @JoinColumn(name = "yl_opetuksen_tavoite_id"),
            inverseJoinColumns = @JoinColumn(name = "arvioinninkohteet_id"))
    private Set<TavoitteenArviointi> arvioinninkohteet = new HashSet<>();

    @Getter
    @Setter
    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name="yl_opetuksen_tavoite_yl_kohdealue",
            joinColumns = @JoinColumn(name = "yl_opetuksen_tavoite_id"),
            inverseJoinColumns = @JoinColumn(name = "kohdealueet_id"))
    private Set<OpetuksenKohdealue> kohdealueet = new HashSet<>();

    @Getter
    @RelatesToPeruste
    @NotAudited
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_aipe_oppiaineen_yl_opetuksen_tavoite",
            joinColumns = @JoinColumn(name = "tavoitteet_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "oppiaine_id", nullable = false))
    private Set<AIPEOppiaine> aipeOppiaineet = new HashSet<>();

    @Getter
    @RelatesToPeruste
    @NotAudited
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "yl_oppiaineen_vlkok_yl_opetuksen_tavoite",
            joinColumns = @JoinColumn(name = "tavoitteet_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "yl_oppiaineen_vlkok_id", nullable = false))
    private Set<OppiaineenVuosiluokkaKokonaisuus> oppiaineenVuosiluokkaKokonaisuudet = new HashSet<>();

    @Getter
    @OrderColumn
    @NotAudited
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "yl_oppiaineen_tavoite_yl_opetuksen_tavoite",
            joinColumns = @JoinColumn(name = "tavoitteet_id"),
            inverseJoinColumns = @JoinColumn(name = "yl_oppiaineen_tavoitteen_opetuksen_tavoite_id"))
    private List<OppiaineenTavoitteenOpetuksenTavoite> oppiaineenTavoitteenOpetuksenTavoitteet = new ArrayList<>();

    public void setOppiaineenTavoitteenOpetuksenTavoitteet(List<OppiaineenTavoitteenOpetuksenTavoite> oppiaineenTavoitteenOpetuksenTavoitteet) {
        this.oppiaineenTavoitteenOpetuksenTavoitteet.clear();
        if (oppiaineenTavoitteenOpetuksenTavoitteet != null) {
            this.oppiaineenTavoitteenOpetuksenTavoitteet.addAll(oppiaineenTavoitteenOpetuksenTavoitteet);
        }
    }

    public Set<TavoitteenArviointi> getArvioinninkohteet() {
        return new HashSet<>(arvioinninkohteet);
    }

    public void setArvioinninkohteet(Set<TavoitteenArviointi> kohteet) {
        this.arvioinninkohteet.clear();
        if (kohteet != null) {
            this.arvioinninkohteet.addAll(kohteet);
        }
    }

    public OpetuksenTavoite kloonaa(
            Map<KeskeinenSisaltoalue, KeskeinenSisaltoalue> keskeinenSisaltoalueMapper,
            Map<LaajaalainenOsaaminen, LaajaalainenOsaaminen> laajainenOsaaminenMapper,
            Map<OpetuksenKohdealue, OpetuksenKohdealue> kohdealueMapper) {
        OpetuksenTavoite klooni = new OpetuksenTavoite();
        klooni.setArvioinninKuvaus(arvioinninKuvaus);
        klooni.setArvioinninOsaamisenKuvaus(arvioinninOsaamisenKuvaus);
        klooni.setArvioinninOtsikko(arvioinninOtsikko);
        klooni.setTavoite(tavoite);

        for (KeskeinenSisaltoalue sisalto : sisaltoalueet) {
            klooni.getSisaltoalueet().add(keskeinenSisaltoalueMapper.get(sisalto));
        }

        for (LaajaalainenOsaaminen laaja : laajattavoitteet) {
            klooni.getLaajattavoitteet().add(laajainenOsaaminenMapper.get(laaja));
        }

        for (OpetuksenKohdealue kohdealue : kohdealueet) {
            klooni.getKohdealueet().add(kohdealueMapper.get(kohdealue));
        }

        Set<TavoitteenArviointi> kohteet = new HashSet<>();
        for (TavoitteenArviointi kohde : arvioinninkohteet) {
            kohteet.add(kohde.kloonaa());
        }
        klooni.setArvioinninkohteet(kohteet);
        klooni.setVapaaTeksti(vapaaTeksti);

        return klooni;
    }

}
