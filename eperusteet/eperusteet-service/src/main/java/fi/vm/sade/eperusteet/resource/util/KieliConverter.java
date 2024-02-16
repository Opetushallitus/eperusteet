package fi.vm.sade.eperusteet.resource.util;

import fi.vm.sade.eperusteet.domain.Kieli;

import java.beans.PropertyEditorSupport;

public class KieliConverter extends PropertyEditorSupport {
    public void setAsText(String kieli) throws IllegalArgumentException {
        setValue(Kieli.of(kieli));
    }
}
