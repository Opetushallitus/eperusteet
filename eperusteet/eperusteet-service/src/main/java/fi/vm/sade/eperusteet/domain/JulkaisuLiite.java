package fi.vm.sade.eperusteet.domain;

import fi.vm.sade.eperusteet.domain.liite.Liite;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "julkaisu_liite")
public class JulkaisuLiite implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @NotNull
    @JoinColumn(name = "julkaistu_peruste_id", nullable = false, updatable = false)
    private JulkaistuPeruste julkaistuPeruste;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @NotNull
    private Liite liite;

    @NotNull
    private String kieli;

    @NotNull
    private String nimi;
}
