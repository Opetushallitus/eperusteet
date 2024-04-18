package fi.vm.sade.eperusteet.config;

import java.beans.PropertyEditorSupport;

public class EnumToUpperCaseEditor<E extends Enum<E>> extends PropertyEditorSupport {

    private final Class<E> anEnum;

    public EnumToUpperCaseEditor(Class<E> anEnum) {
        this.anEnum = anEnum;
    }


    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(Enum.valueOf(anEnum, text.toUpperCase()));
    }

}
