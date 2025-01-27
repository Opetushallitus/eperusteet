package fi.vm.sade.eperusteet.domain;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "julkaistu_peruste_tila")
public class JulkaisuPerusteTila implements Serializable {

    @Id
    @Column(name = "peruste_id")
    private Long perusteId;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "julkaisu_tila")
    private JulkaisuTila julkaisutila;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date muokattu;

    @PreUpdate
    protected void preupdate() {
        muokattu = new Date();
    }

    @PrePersist
    private void prepersist() {
        muokattu = new Date();
    }
}
