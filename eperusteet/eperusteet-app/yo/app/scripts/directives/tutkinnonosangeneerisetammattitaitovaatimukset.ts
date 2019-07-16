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
import _ from "lodash";

angular
    .module("eperusteApp")
    .controller("tutkinnonosangeneerisetammattitaitovaatimuksetCtrl", function(ArviointiasteikkoHelper, Api, $scope, YleinenData, $timeout, Utils, Varmistusdialogi) {
        $scope.state = {};
        let geneeriset: any[] = [];

        async function init() {
            geneeriset = (await Api.all("geneerinenarviointi").getList()).plain();
            $scope.arviointiasteikot = await ArviointiasteikkoHelper.getMappedArviointiasteikot();
            $scope.geneeriset = _.filter(geneeriset, "julkaistu");
            const asteikkoId = _.parseInt($scope.geneerinen);
            $scope.valittu = _(geneeriset)
                .filter({ id: asteikkoId })
                .first();
            $scope.state.valittu = asteikkoId;
        }

        $scope.changeGeneerinen = (id) => {
            $scope.geneerinen = id;
            $scope.valittu = _(geneeriset)
                .filter({ id: _.parseInt(id) })
                .first();
        };
        init();
    })
    .directive("tutkinnonosangeneerisetammattitaitovaatimukset", function(YleinenData, $timeout) {
        return {
            template: require("views/partials/geneerisetammattitaitovaatimukset.pug"),
            restrict: "E",
            scope: {
                editAllowed: "@?editointiSallittu",
                editEnabled: "=",
                geneerinen: "=",
                type: "@"
            },
            controller: "tutkinnonosangeneerisetammattitaitovaatimuksetCtrl",
            link(scope: any) {
            },
        };
    });
