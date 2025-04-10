package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.io.Serializable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "perusteen_tutkintonimikkeet")
public class TutkintonimikeKoodi implements Serializable {

    public TutkintonimikeKoodi() {
    }

    public TutkintonimikeKoodi(TutkintonimikeKoodi other) {
        this.tutkinnonOsaUri = other.tutkinnonOsaUri;
        this.tutkinnonOsaArvo = other.tutkinnonOsaArvo;
        this.osaamisalaUri = other.osaamisalaUri;
        this.osaamisalaArvo = other.osaamisalaArvo;
        this.tutkintonimikeUri = other.tutkintonimikeUri;
        this.tutkintonimikeArvo = other.tutkintonimikeArvo;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "peruste_id")
    private Peruste peruste;

    @Column(name = "tutkinnon_osa_koodi_uri")
    private String tutkinnonOsaUri;

    @Column(name = "tutkinnon_osa_koodi_arvo")
    private String tutkinnonOsaArvo;

    @Column(name = "osaamisala_koodi_uri")
    private String osaamisalaUri;

    @Column(name = "osaamisala_koodi_arvo")
    private String osaamisalaArvo;

    @NotNull
    @Column(name = "tutkintonimike_koodi_uri")
    private String tutkintonimikeUri;

    @Column(name = "tutkintonimike_koodi_arvo")
    private String tutkintonimikeArvo;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private TekstiPalanen nimi;
}
