package fi.vm.sade.eperusteet.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
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
