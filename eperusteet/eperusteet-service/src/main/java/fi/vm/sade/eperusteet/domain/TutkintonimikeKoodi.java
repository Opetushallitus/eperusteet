package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.validation.ValidHtml;
import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
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

    @OneToOne(fetch = FetchType.LAZY)
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
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private TekstiPalanen nimi;
}
