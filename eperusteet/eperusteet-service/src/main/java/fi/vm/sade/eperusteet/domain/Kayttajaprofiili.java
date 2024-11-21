package fi.vm.sade.eperusteet.domain;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
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
