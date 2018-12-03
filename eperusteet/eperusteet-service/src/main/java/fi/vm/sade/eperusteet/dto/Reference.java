package fi.vm.sade.eperusteet.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Kuvaa viitettä toiseen entiteettiin.
 *
 * @author jhyoty
 */
@EqualsAndHashCode
public class Reference {

    private final String id;

    @JsonCreator
    public Reference(Long id) {
        if (id != null) {
            this.id = id.toString();
        }
        else {
            this.id = null;
        }
    }

    @JsonCreator
    public Reference(String id) {
        this.id = id;
    }

    @JsonValue
    public String getId() {
        return id;
    }

    @JsonIgnore
    public Long getIdLong() {
        return id == null ? null : Long.valueOf(id);
    }

    public static Reference of(ReferenceableEntity e) {
        return (e == null || e.getId() == null) ? null : new Reference(e.getId().toString());
    }

    public static Reference of(ReferenceableDto d) {
        return (d == null || d.getId() == null) ? null : new Reference(d.getId().toString());

    }

    public static Reference of(Long id) {
        return id == null ? null : new Reference(id.toString());
    }

    public static Reference of(UUID id) {
        return id == null ? null : new Reference(id.toString());
    }

    @Override
    public String toString() {
        return id;
    }



}