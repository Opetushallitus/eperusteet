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

'use strict';
/* global _ */

angular.module('eperusteApp')
  .controller('TuoTekstikappale', function($scope, $modalInstance, Notifikaatiot, peruste, suoritustapa, PerusteenRakenne, SuoritustapaSisalto) {
    $scope.perusteet = [];
    $scope.sivuja = 0;
    $scope.sivu = 0;
    $scope.valittuPeruste = null;
    $scope.rajaus = '';

    $scope.haku = function(haku) {
      PerusteenRakenne.haePerusteita(haku, function(res) {
        $scope.perusteet = res.data;
        $scope.sivuja = res.sivuja;
        $scope.sivu = res.sivu;
      });
    };
    $scope.haku('');

    // $scope.rajaus = Algoritmit.rajausVertailu($scope.rajaus, osa, 'perusteenOsa', 'nimi');

    $scope.valitse = function(valittuPeruste) {
      $scope.valittuPeruste = valittuPeruste;
      SuoritustapaSisalto.get({
        perusteId: valittuPeruste.id,
        suoritustapa: suoritustapa
      }, function(res) {
        $scope.sisalto = _.reject(res.lapset, function(lapsi) {
          return lapsi.perusteenOsa.tunniste === 'rakenne';
        });
      }, Notifikaatiot.serverCb);
    };

    $scope.takaisin = function() { $scope.valittuPeruste = null; };
    $scope.peru = function() { $modalInstance.dismiss(); };
    $scope.ok = function() {
      $modalInstance.close(_.filter($scope.sisalto, function(s) { return s.$valittu; }));
    };
  });
