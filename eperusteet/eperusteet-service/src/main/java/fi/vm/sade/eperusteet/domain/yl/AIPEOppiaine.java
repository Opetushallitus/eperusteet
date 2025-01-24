package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.HistoriaTapahtuma;
import fi.vm.sade.eperusteet.domain.KevytTekstiKappale;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.Tunnistettava;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml.WhitelistType;
import java.util.*;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Audited
@Table(name = "yl_aipe_oppiaine")
public class AIPEOppiaine extends AbstractAuditedReferenceableEntity implements Kloonattava<AIPEOppiaine>, AIPEJarjestettava, Tunnistettava, HistoriaTapahtuma {

    @NotNull
    @Column(updatable = false)
    @Getter
    private UUID tunniste = UUID.randomUUID();

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    @NotNull(groups = Oppiaine.Strict.class)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Koodi koodi;

    @Getter
    @Setter
    private Integer jarjestys;

    @Getter
    @Setter
    @Valid
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa tehtava;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa tyotavat;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa ohjaus;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa arviointi;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa sisaltoalueinfo;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "yl_aipe_oppiaine_yl_opetuksen_tavoite",
            joinColumns = { @JoinColumn(name = "yl_aipe_oppiaine_id") },
            inverseJoinColumns = { @JoinColumn(name = "tavoitteet_id") })
    @OrderColumn
    private List<OpetuksenTavoite> tavoitteet = new ArrayList<>();

    @Getter
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "yl_aipe_oppiaine_yl_keskeinen_sisaltoalue",
            joinColumns = { @JoinColumn(name = "yl_aipe_oppiaine_id") },
            inverseJoinColumns = { @JoinColumn(name = "sisaltoalueet_id") })
    @OrderColumn
    private List<KeskeinenSisaltoalue> sisaltoalueet = new ArrayList<>();

    @Getter
    @Setter
    @ValidHtml
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private TekstiPalanen pakollinenKurssiKuvaus;

    @Getter
    @Setter
    @ValidHtml
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "syventava_kurssi_kuvaus")
    private TekstiPalanen syventavaKurssiKuvaus;

    @Getter
    @Setter
    @ValidHtml
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "soveltava_kurssi_kuvaus")
    private TekstiPalanen soveltavaKurssiKuvaus;

    @Getter
    @Audited
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "aipeoppiaine_aipekurssi",
               joinColumns = { @JoinColumn(name = "oppiaine_id") },
               inverseJoinColumns = { @JoinColumn(name = "kurssi_id") })
    @OrderBy("jarjestys, id")
    private List<AIPEKurssi> kurssit = new ArrayList<>();

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    private TekstiPalanen vapaaTeksti;

    @Getter
    @Setter
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "aipeoppiaine_vapaateksti",
            joinColumns = @JoinColumn(name = "aipeoppiaine_id"),
            inverseJoinColumns = @JoinColumn(name = "kevyttekstikappale_id"))
    @OrderColumn(name = "kevyttekstikappaleet_order")
    private List<KevytTekstiKappale> vapaatTekstit;

    @Getter
    @Setter
    private boolean koosteinen = false;

    @Getter
    @Setter
    private boolean abstrakti = false;

    @Getter
    @Audited
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JoinTable(name = "aipeoppiaine_aipeoppiaine",
               joinColumns = {@JoinColumn(name = "oppiaine_id")},
               inverseJoinColumns = {@JoinColumn(name = "oppimaara_id")})
    @OrderBy("jarjestys, id")
    private List<AIPEOppiaine> oppimaarat = new ArrayList<>();

    @ValidHtml(whitelist = WhitelistType.NORMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TekstiPalanen kielikasvatus;

    @Getter
    @Setter
    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(name = "aipeoppiaine_aipeoppiaine",
               joinColumns = {@JoinColumn(name = "oppimaara_id")},
               inverseJoinColumns = {@JoinColumn(name = "oppiaine_id")})
    private AIPEOppiaine oppiaine;

    @Override
    public AIPEOppiaine kloonaa() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<OpetuksenTavoite> getTavoitteet() {
        return new ArrayList<>(tavoitteet);
    }

    public List<KeskeinenSisaltoalue> getSisaltoalueet() {
        return new ArrayList<>(sisaltoalueet);
    }

    public void addOpetuksenTavoite(OpetuksenTavoite tavoite) {
        tavoitteet.add(tavoite);
    }

    public void setTavoitteet(List<OpetuksenTavoite> tavoitteet) {
        this.tavoitteet.clear();
        if (tavoitteet != null) {
            this.tavoitteet.addAll(tavoitteet);
        }
    }

    public void addKurssit(List<AIPEKurssi> kurssit) {
        this.kurssit.clear();
        if (kurssit != null) {
            this.kurssit.addAll(kurssit);
        }
    }

    public Optional<AIPEKurssi> getKurssi(Long kurssiId) {
        return kurssit.stream()
                .filter(kurssi -> Objects.equals(kurssi.getId(), kurssiId))
                .findFirst();
    }

    public static void validateChange(AIPEOppiaine a, AIPEOppiaine b) {
        if (a.getNimi() != null && b.getNimi() == null) {
            throw new BusinessRuleViolationException("nimea-ei-voi-poistaa");
        }

        if (a.getKoodi() != null && b.getKoodi() == null) {
            throw new BusinessRuleViolationException("koodia-ei-voi-muuttaa");
        }

        if (!Objects.equals(a.getTunniste(), b.getTunniste())) {
            throw new BusinessRuleViolationException("tunnistetta-ei-voi-muuttaa");
        }

        if (a.pakollinenKurssiKuvaus != null && b.pakollinenKurssiKuvaus == null) {
            throw new BusinessRuleViolationException("nimea-ei-voi-poistaa");
        }

        if (a.syventavaKurssiKuvaus != null && b.syventavaKurssiKuvaus == null) {
            throw new BusinessRuleViolationException("nimea-ei-voi-poistaa");
        }

        if (a.soveltavaKurssiKuvaus != null && b.soveltavaKurssiKuvaus == null) {
            throw new BusinessRuleViolationException("nimea-ei-voi-poistaa");
        }

        if (a.kielikasvatus != null && b.kielikasvatus == null) {
            throw new BusinessRuleViolationException("nimea-ei-voi-poistaa");
        }

        if (a.koosteinen != b.koosteinen) {
            throw new BusinessRuleViolationException("oppiaineen-ominaisuutta-ei-voi-muuttaa");
        }

        if (a.abstrakti != b.abstrakti) {
            throw new BusinessRuleViolationException("oppiaineen-ominaisuutta-ei-voi-muuttaa");
        }

        if ((a.oppiaine != null && b.oppiaine == null || (a.oppiaine != null && !Objects.equals(a.oppiaine.getId(), b.oppiaine.getId())))) {
            throw new BusinessRuleViolationException("omistavaa-oppiainetta-ei-voi-muuttaa");
        }

        TekstiOsa.validateChange(a.tehtava, b.tehtava);
        TekstiOsa.validateChange(a.tyotavat, b.tyotavat);
        TekstiOsa.validateChange(a.ohjaus, b.tehtava);
        TekstiOsa.validateChange(a.arviointi, b.arviointi);
        TekstiOsa.validateChange(a.sisaltoalueinfo, b.sisaltoalueinfo);

        if (a.getKurssit() != null) {
            if (!Objects.equals(a.getKurssit().size(), b.getKurssit().size())) {
                throw new BusinessRuleViolationException("rakennetta-ei-voi-muuttaa");
            }

            for (Integer idx = 0; idx < a.getKurssit().size(); ++idx) {
                AIPEKurssi.validateChange(a.getKurssit().get(idx), b.getKurssit().get(idx));
            }
        }
    }

    public void setVapaatTekstit(List<KevytTekstiKappale> vapaatTekstit) {
        this.vapaatTekstit = new ArrayList<>();
        if (vapaatTekstit != null) {
            for (KevytTekstiKappale vapaaTeksti : vapaatTekstit) {
                this.vapaatTekstit.add(new KevytTekstiKappale(vapaaTeksti));
            }
        }
    }


    @Override
    public NavigationType getNavigationType() {
        return NavigationType.aipeoppiaine;
    }
}
