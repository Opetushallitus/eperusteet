package fi.vm.sade.eperusteet.domain;

public enum SkeduloituAjoStatus {

    AJOSSA("ajossa"),
    PYSAYTETTY("pysaytetty"),
    AJOVIRHE("ajovirhe");

    private String status;

    SkeduloituAjoStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }

}
