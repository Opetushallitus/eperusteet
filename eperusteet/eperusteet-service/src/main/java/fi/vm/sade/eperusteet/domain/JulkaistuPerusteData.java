package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fi.vm.sade.eperusteet.repository.dialect.JsonBType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Immutable
@Table(name = "julkaistu_peruste_data")
@TypeDef(name = "jsonb", defaultForType = JsonBType.class, typeClass = JsonBType.class)
public class JulkaistuPerusteData {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @NotNull
    @Getter
    private int hash;

    @NotNull
    @Getter
    @Setter
    @Type(type = "jsonb")
    @Column(name = "data")
    private ObjectNode data;

    @PrePersist
    void prepersist() {
        hash = data.hashCode();
    }

    public JulkaistuPerusteData() {
    }

    public JulkaistuPerusteData(ObjectNode data) {
        this.data = data;
    }
}
