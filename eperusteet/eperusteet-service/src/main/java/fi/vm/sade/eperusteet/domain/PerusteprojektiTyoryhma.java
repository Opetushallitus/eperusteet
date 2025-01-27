package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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

    @ManyToOne(fetch = FetchType.LAZY)
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
