package fi.vm.sade.eperusteet.dto.peruste;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Navigable {

    @JsonIgnore
    NavigationType getNavigationType();
}
