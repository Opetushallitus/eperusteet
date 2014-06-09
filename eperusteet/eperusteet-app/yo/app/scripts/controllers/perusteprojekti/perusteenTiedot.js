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
/*global _*/

angular.module('eperusteApp')
  .controller('PerusteenTiedotCtrl', function($scope, $rootScope, $stateParams, $state,
    Koodisto, Perusteet, YleinenData, PerusteProjektiService, perusteprojektiTiedot) {

    $scope.hakemassa = false;
    $scope.peruste = perusteprojektiTiedot.getPeruste();
    $scope.peruste.nimi = $scope.peruste.nimi || {};
    $scope.projektiId = $stateParams.perusteProjektiId;
    $scope.open = {};

    $scope.rajaaKoodit = function(koodi) {
      return koodi.koodi.indexOf('_3') !== -1;
    };

    $scope.koodistoHaku = function(koodisto) {

      angular.forEach(YleinenData.kielet, function(value) {
        if (_.isEmpty($scope.peruste.nimi[value]) && !_.isNull(koodisto.nimi[value])) {
          $scope.peruste.nimi[value] = koodisto.nimi[value];
        }
      });

      $scope.peruste.koulutukset.push({});
      $scope.peruste.koulutukset[$scope.peruste.koulutukset.length - 1].nimi = koodisto.nimi;
      $scope.peruste.koulutukset[$scope.peruste.koulutukset.length - 1].koulutuskoodi = koodisto.koodi;

      $scope.open[koodisto.koodi] = true;

      Koodisto.haeAlarelaatiot(koodisto.koodi, function(relaatiot) {
        _.forEach(relaatiot, function(rel) {
          switch (rel.koodisto.koodistoUri) {
            case 'koulutusalaoph2002':
              $scope.peruste.koulutukset[$scope.peruste.koulutukset.length - 1].koulutusalakoodi = rel.koodi;
              break;
            case 'opintoalaoph2002':
              $scope.peruste.koulutukset[$scope.peruste.koulutukset.length - 1].opintoalakoodi = rel.koodi;
              break;
          }
        });
      }, function(virhe) {
        console.log('koodisto alarelaatio virhe', virhe);
      });
    };

    $scope.tallennaPeruste = function() {
      Perusteet.save({perusteenId: $scope.peruste.id}, $scope.peruste, function(vastaus) {
        $scope.peruste = vastaus;
        $state.go('perusteprojekti.suoritustapa.sisalto', {perusteProjektiId: $scope.projektiId, suoritustapa: PerusteProjektiService.getSuoritustapa()}, {reload: true});
      }, function(virhe) {
        console.log('perusteen tallennus virhe', virhe);
      });
    };

    $scope.avaaKoodistoModaali = function() {
      Koodisto.modaali(function(koodi) {
        $scope.koodistoHaku(koodi);
      },
        {tyyppi: function() {
            return 'koulutus';
          }, ylarelaatioTyyppi: function() {
            return $scope.peruste.tutkintokoodi;
          }},
      function() {
      }, null)();
    };

    $scope.poistaKoulutus = function (koulutuskoodi) {
      $scope.peruste.koulutukset = _.remove($scope.peruste.koulutukset, function(koulutus) {
            return koulutus.koulutuskoodi !== koulutuskoodi;
      });
    };

    $scope.koulutusalaNimi = function(koodi) {
      return $scope.Koulutusalat.haeKoulutusalaNimi(koodi);
    };

    $scope.opintoalaNimi = function(koodi) {
      return $scope.Opintoalat.haeOpintoalaNimi(koodi);
    };

    $rootScope.$on('event:spinner_on', function() {
      $scope.hakemassa = true;
    });

    $rootScope.$on('event:spinner_off', function() {
      $scope.hakemassa = false;
    });

  });
