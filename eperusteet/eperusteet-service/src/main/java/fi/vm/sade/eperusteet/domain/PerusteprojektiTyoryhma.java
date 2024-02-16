package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "perusteprojekti_tyoryhma")
public class PerusteprojektiTyoryhma implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    private Perusteprojekti perusteprojekti;

    @Getter
    @Setter
    @NotNull(message = "Käyttäjän oid ei voi olla tyhjä")
    @Column(name = "kayttaja_oid")
    private String kayttajaOid;

    @Getter
    @Setter
    @NotNull(message = "Työryhmän nimi ei voi olla tyhjä")
    @Column(name = "nimi")
    private String nimi;

    public PerusteprojektiTyoryhma(Perusteprojekti perusteprojekti, String kayttajaOid, String nimi) {
        this.perusteprojekti = perusteprojekti;
        this.kayttajaOid = kayttajaOid;
        this.nimi = nimi;
    }

    public PerusteprojektiTyoryhma() {
    }
}
