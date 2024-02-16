package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "kayttajaprofiili")
public class Kayttajaprofiili implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String oid;

    @OrderColumn(name = "suosikki_order")
    @OneToMany(mappedBy = "kayttajaprofiili", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    @Setter
    private List<Suosikki> suosikit;

    @OneToMany(mappedBy = "kayttajaprofiili", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    @Setter
    private List<KayttajaprofiiliPreferenssi> preferenssit;

    public Kayttajaprofiili() {
    }

    public Kayttajaprofiili(Long id) {
        this.id = id;
    }

}
