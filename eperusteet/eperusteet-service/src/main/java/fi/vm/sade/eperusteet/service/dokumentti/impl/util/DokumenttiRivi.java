package fi.vm.sade.eperusteet.service.dokumentti.impl.util;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DokumenttiRivi {

    private ArrayList<String> sarakkeet = new ArrayList<>();
    private Integer colspan = 1;
    private DokumenttiRiviTyyppi tyyppi = DokumenttiRiviTyyppi.DEFAULT;

    public void addSarake(String sarake) {
        sarakkeet.add(sarake);
    }

    public String getBackgroundColor() {
        switch (tyyppi) {
            case HEADER:
                return DokumenttiRiviBgColor.HEADER;
            case SUBHEADER:
                return DokumenttiRiviBgColor.SUBHEADER;
        }

        return DokumenttiRiviBgColor.DEFAULT;
    }

    public String getFontColor() {
        switch (tyyppi) {
            case HEADER:
                return DokumenttiRiviFontColor.HEADER;
        }

        return DokumenttiRiviFontColor.DEFAULT;
    }

    public String getElementType() {
        switch (tyyppi) {
            case HEADER:
                return "th";
        }

        return "td";
    }

    public String getElementAlign() {
        return "start";
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        sarakkeet.forEach((sarake) -> {
            builder.append(String.format("<%s colspan=\"%d\" align=\"%s\">", getElementType(), getColspan(), getElementAlign()));
            builder.append(sarake);
            builder.append(String.format("</%s>", getElementType()));
        });
        return builder.toString();
    }
}
