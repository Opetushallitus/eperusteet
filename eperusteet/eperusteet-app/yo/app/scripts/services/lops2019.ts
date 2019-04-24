import * as angular from "angular";

angular
    .module("eperusteApp")
    .service("Lops2019Service", function(
        Api,
    ) {
        let tiedot = null;
        this.setTiedot = value => {
            tiedot = value;
        };

        this.getSisalto = async () => {
            return await Api
                .one("perusteet", tiedot.getPeruste().id)
                .one("suoritustavat", "lukiokoulutus2019")
                .customGET("sisalto");
        };

        // TODO
        this.getOsat = async tyyppi => {
            return;
        };

    });
