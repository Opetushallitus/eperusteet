package fi.vm.sade.eperusteet.domain;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Immutable
@Table(name = "julkaistu_peruste_data")
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
    @JdbcTypeCode(SqlTypes.JSON)
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
