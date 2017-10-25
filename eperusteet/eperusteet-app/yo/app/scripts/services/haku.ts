/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

import * as angular from "angular";
import * as _ from "lodash";

angular.module("eperusteApp").service("Haku", function Haku(YleinenData) {
    var DEFAULTS = {
        "root.selaus.ammatillinenperuskoulutus": {
            nimi: "",
            koulutusala: "",
            tyyppi: "koulutustyyppi_1",
            kieli: YleinenData.kieli,
            opintoala: "",
            siirtyma: false,
            sivu: 0,
            sivukoko: 20,
            suoritustapa: "ops",
            perusteTyyppi: "normaali",
            tila: "valmis"
        },
        "root.selaus.ammatillinenaikuiskoulutus": {
            nimi: "",
            koulutusala: "",
            tyyppi: "",
            kieli: YleinenData.kieli,
            opintoala: "",
            siirtyma: false,
            sivu: 0,
            sivukoko: 20,
            suoritustapa: "naytto",
            perusteTyyppi: "normaali",
            tila: "valmis"
        }
    };

    this.hakuparametrit = _.clone(DEFAULTS);

    this.getHakuparametrit = function(stateName) {
        return _.clone(this.hakuparametrit[stateName]);
    };

    this.setHakuparametrit = function(stateName, hakuparametrit) {
        this.hakuparametrit[stateName] = _.merge(hakuparametrit);
    };

    this.resetHakuparametrit = function(stateName) {
        this.hakuparametrit[stateName] = _.clone(DEFAULTS[stateName]);
        return this.hakuparametrit[stateName];
    };
});
