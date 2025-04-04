package fi.vm.sade.eperusteet.domain.yl;

import fi.vm.sade.eperusteet.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.domain.HistoriaTapahtuma;
import fi.vm.sade.eperusteet.domain.KevytTekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.Tunnistettava;
import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.util.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Audited
@Table(name = "yl_aipe_vaihe")
public class AIPEVaihe extends AbstractAuditedReferenceableEntity implements Kloonattava<AIPEVaihe>, AIPEJarjestettava, Tunnistettava, HistoriaTapahtuma {

    @NotNull
    @Column(updatable = false)
    @Getter
    private UUID tunniste;

    @Getter
    @Setter
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    private TekstiPalanen nimi;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL)
    private TekstiOsa siirtymaEdellisesta;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa tehtava;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL)
    private TekstiOsa siirtymaSeuraavaan;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TekstiOsa paikallisestiPaatettavatAsiat;

    @Getter
    @Setter
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinTable(name = "aipevaihe_vapaateksti",
            joinColumns = @JoinColumn(name = "aipevaihe_id"),
            inverseJoinColumns = @JoinColumn(name = "kevyttekstikappale_id"))
    @OrderColumn(name = "kevyttekstikappaleet_order")
    private List<KevytTekstiKappale> vapaatTekstit;

    @Getter
    @Setter
    private Integer jarjestys;

    @Getter
    @Audited
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "aipevaihe_kohdealue",
            joinColumns = @JoinColumn(name = "vaihe_id", unique = true),
            inverseJoinColumns = @JoinColumn(name = "kohdealue_id", unique = true))
    @OrderColumn(name = "kohdealue_order")
    private List<OpetuksenKohdealue> opetuksenKohdealueet = new ArrayList<>();

    @Getter
    @Audited
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(name = "aipevaihe_aipeoppiaine",
               joinColumns = {
                   @JoinColumn(name = "vaihe_id")},
               inverseJoinColumns = {
                   @JoinColumn(name = "oppiaine_id")})
    @OrderBy("jarjestys, id")
    private List<AIPEOppiaine> oppiaineet = new ArrayList<>(0);

    public List<OpetuksenKohdealue> getOpetuksenKohdealueet() {
        return new ArrayList<>(opetuksenKohdealueet);
    }

    public void setOpetuksenKohdealueet(List<OpetuksenKohdealue> opetuksenKohdealueet) {
        this.opetuksenKohdealueet.clear();
        if (opetuksenKohdealueet != null) {
            this.opetuksenKohdealueet.addAll(opetuksenKohdealueet);
        }
    }

    public void setTunniste(UUID tunniste) {
        if (this.tunniste == null) {
            this.tunniste = tunniste;
        }
    }

    @PrePersist
    public void prePersist() {
        if (this.tunniste == null) {
            this.tunniste = UUID.randomUUID();
        }
    }

    public AIPEOppiaine getOppiaine(Long oppiaineId) {
        Optional<AIPEOppiaine> result = oppiaineet.stream()
                .filter(oppiaine -> Objects.equals(oppiaine.getId(), oppiaineId))
                .findFirst();

        if (!result.isPresent()) {
            result = oppiaineet.stream()
                .map(AIPEOppiaine::getOppimaarat)
                .flatMap(Collection::stream)
                .filter(oppiaine -> Objects.equals(oppiaine.getId(), oppiaineId))
                .findFirst();
        }

        return result.orElse(null);
    }

    @Override
    public AIPEVaihe kloonaa() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    static public void validateChange(AIPEVaihe a, AIPEVaihe b, boolean checkTextChanges) throws BusinessRuleViolationException {
        if (b == null) {
            throw new BusinessRuleViolationException("rakennetta-ei-voi-muuttaa");
        }

        TekstiOsa.validateChange(a.getSiirtymaEdellisesta(), b.getSiirtymaEdellisesta());
        TekstiOsa.validateChange(a.getTehtava(), b.getSiirtymaEdellisesta());
        TekstiOsa.validateChange(a.getSiirtymaSeuraavaan(), b.getSiirtymaEdellisesta());
        TekstiOsa.validateChange(a.getPaikallisestiPaatettavatAsiat(), b.getPaikallisestiPaatettavatAsiat());

        if (!Objects.equals(a.getTunniste(), b.getTunniste())) {
            throw new BusinessRuleViolationException("tunnistetta-ei-voi-muuttaa");
        }

        if (!Objects.equals(a.getJarjestys(), b.getJarjestys())) {
            throw new BusinessRuleViolationException("rakennetta-ei-voi-muuttaa");
        }

        if (!Objects.equals(a.getJarjestys(), b.getJarjestys())) {
            throw new BusinessRuleViolationException("rakennetta-ei-voi-muuttaa");
        }

        if (a.getOppiaineet() != null) {
            if (!Objects.equals(a.getOppiaineet().size(), b.getOppiaineet().size())) {
                throw new BusinessRuleViolationException("rakennetta-ei-voi-muuttaa");
            }

            for (Integer idx = 0; idx < a.getOppiaineet().size(); ++idx) {
                AIPEOppiaine.validateChange(a.getOppiaineet().get(idx), b.getOppiaineet().get(idx));
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
        return NavigationType.aipevaihe;
    }
}
