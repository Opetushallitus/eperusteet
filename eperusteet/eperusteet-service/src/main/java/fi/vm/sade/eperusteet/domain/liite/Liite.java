package fi.vm.sade.eperusteet.domain.liite;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.annotation.RelatesToPeruste;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Blob;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "liite")
@NoArgsConstructor
public class Liite implements Serializable {

    @Id
    @Getter
    @Setter
    @Column(updatable = false)
    private UUID id;

    @Getter
    @Setter
    @NotNull
    @Enumerated(EnumType.STRING)
    private LiiteTyyppi tyyppi = LiiteTyyppi.TUNTEMATON;

    @Getter
    @NotNull
    @Basic(optional = false)
    private String mime;

    @Getter
    //@NotNull
    @Size(max = 1024)
    private String nimi;

    @Temporal(TemporalType.TIMESTAMP)
    private Date luotu;

    @Getter
    @Basic(fetch = FetchType.LAZY, optional = false)
    @Column(updatable = false, nullable = false)
    @Lob
    @NotNull
    private Blob data;

    @RelatesToPeruste
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "peruste_liite",
            joinColumns = @JoinColumn(name = "liite_id"),
            inverseJoinColumns = @JoinColumn(name = "peruste_id"))
    @Getter
    @Setter
    private Set<Peruste> perusteet;

    @Getter
    @Setter
    private String lisatieto;

    public Liite(UUID uuid, LiiteTyyppi tyyppi, String mime, String nimi, Blob data) {
        this.id = uuid;
        this.luotu = new Date();
        this.nimi = nimi;
        this.tyyppi = tyyppi;
        this.mime = mime;
        this.data = data;
    }

    public Liite(LiiteTyyppi tyyppi, String mime, String nimi, Blob data) {
        this.id = UUID.randomUUID();
        this.luotu = new Date();
        this.nimi = nimi;
        this.tyyppi = tyyppi;
        this.mime = mime;
        this.data = data;
    }

    public Date getLuotu() {
        return luotu == null ? null : new Date(luotu.getTime());
    }

}
