package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "koodi")
@EqualsAndHashCode(of = {"uri", "versio"})
public class Koodi implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @NotEmpty
    private String uri; // Uniikki koodistosta minkä sisällöstä ei voi päätellä mitään

    @NotNull
    @NotEmpty
    private String koodisto;

    private Long versio; // Oletuksena null milloin käytetään uusinta koodiston versiota

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private TekstiPalanen nimi;

    public Koodi() {
    }

    public Koodi(final String uri) {
        this.uri = uri;
    }

    public Koodi(final String uri, final String koodisto) {
        this.uri = uri;
        this.koodisto = koodisto;
        this.versio = null;
    }

    public boolean isTemporary() {
        return uri != null && uri.startsWith("temporary_");
    }

    public String getKoodisto() {
        if (isTemporary()) {
            return getUri().split("_")[1];
        } else {
            return this.koodisto;
        }
    }

    public static void validateChange(final Koodi a, final Koodi b) {
        if (a != null && !Objects.equals(a, b)) {
            throw new BusinessRuleViolationException("koodia-ei-voi-muuttaa");
        }
    }

    @PrePersist
    public void onPrePersist() {
        final String[] osat = this.getUri().split("_");
        if (osat.length < 2) {
            throw new BusinessRuleViolationException("virheellinen-koodi-uri: " + this.getUri());
        }

        final String uriKoodisto = osat[0];
        if (ObjectUtils.isEmpty(this.koodisto)) {
            this.koodisto = uriKoodisto;
        } else if (!Objects.equals(this.getKoodisto(), uriKoodisto)) {
            throw new BusinessRuleViolationException("uri: " + this.getUri() + " ei vastaa koodistoa: " + this.getKoodisto());
        }
    }


    static public Koodi of(String koodisto, String arvo) {
        Koodi result = new Koodi();
        result.setUri(koodisto + "_" + arvo);
        result.setKoodisto(koodisto);
        return result;
    }

}
