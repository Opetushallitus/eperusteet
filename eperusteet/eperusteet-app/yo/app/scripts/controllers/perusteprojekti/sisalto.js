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
// /*global _*/

angular.module('eperusteApp')
  .controller('PerusteprojektisisaltoCtrl', function($scope, $state, $stateParams,
    SuoritustapaSisalto, PerusteProjektiService, perusteprojektiTiedot, TutkinnonOsaEditMode) {

    $scope.projekti = perusteprojektiTiedot.getProjekti();
    $scope.peruste = perusteprojektiTiedot.getPeruste();
    $scope.peruste.sisalto = perusteprojektiTiedot.getSisalto();

    $scope.valittuSuoritustapa = PerusteProjektiService.getSuoritustapa();

    var haeSisalto = function(suoritustapa) {
      perusteprojektiTiedot.haeSisalto($scope.projekti._peruste, suoritustapa).then(function(vastaus) {
        $scope.peruste.sisalto = vastaus;
        $scope.valittuSuoritustapa = suoritustapa;
        PerusteProjektiService.setSuoritustapa(suoritustapa);
      }, function(virhe) {
        $scope.valittuSuoritustapa = '';
        console.log('suoritustapasisältöä ei löytynyt', virhe);
      });
    };

    $scope.createSisalto = function() {
      SuoritustapaSisalto.save({perusteId: $scope.projekti._peruste, suoritustapa: PerusteProjektiService.getSuoritustapa()}, {}, function(response) {
        // Uusi luotu, siirry suoraan muokkaustilaan
        TutkinnonOsaEditMode.setMode(true);
        $scope.navigoi('perusteprojekti.suoritustapa.perusteenosa', {
          perusteenOsanTyyppi: 'tekstikappale',
          perusteenOsaId: response._perusteenOsa
        });
      }, function(virhe) {
        console.log('Uuden sisällön luontivirhe', virhe);
      });
    };

    $scope.vaihdaSuoritustapa = function(suoritustapakoodi) {
      $scope.valittuSuoritustapa = suoritustapakoodi;
      PerusteProjektiService.setSuoritustapa(suoritustapakoodi);
      $state.go('perusteprojekti.suoritustapa.sisalto', {perusteProjektiId: $stateParams.perusteProjektiId, suoritustapa: suoritustapakoodi});
    };

    $scope.navigoi = function (state, params) {
      $state.go(state, params);
    };
  });
