package fi.vm.sade.eperusteet.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SkeduloituAjoStatus {

    AJOSSA("ajossa"),
    PYSAYTETTY("pysaytetty"),
    AJOVIRHE("ajovirhe");

    private String status;
}
