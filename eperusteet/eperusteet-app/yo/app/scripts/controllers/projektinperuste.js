'use strict';
/*global _*/

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('perusteprojekti.editoi.peruste', {
        url: '/peruste',
        templateUrl: 'views/partials/perusteprojektiPeruste.html',
        controller: 'ProjektinPerusteCtrl',
        naviBase: ['perusteprojekti', ':perusteProjektiId'],
        navigaationimiId: 'perusteProjektiId',
        onEnter: ['SivunavigaatioService', function(SivunavigaatioService) {
            SivunavigaatioService.aseta({osiot: false});
          }]
      });
  })
  .controller('ProjektinPerusteCtrl', function($scope, $rootScope, $stateParams, $state,
    Koodisto, Perusteet, PerusteprojektiResource, YleinenData) {

    $scope.hakemassa = false;
    $scope.peruste = {};
    $scope.peruste.nimi = {};
    $scope.projektiId = $stateParams.perusteProjektiId;
    $scope.open = {};


    PerusteprojektiResource.get({id: $stateParams.perusteProjektiId}, function(vastaus) {
      $scope.projekti = vastaus;
      if ($scope.projekti._peruste) {
        Perusteet.get({perusteenId: vastaus._peruste}, function(vastaus) {
          vastaus.nimi = vastaus.nimi || {};
          $scope.peruste = vastaus;

        }, function(virhe) {
          console.log('perusteen haku virhe', virhe);
        });
      }
    }, function(virhe) {
      console.log('virhe', virhe);
    });

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
        $state.go('perusteprojekti.editoi.sisalto', {perusteProjektiId: $scope.projektiId}, {reload: true});
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
