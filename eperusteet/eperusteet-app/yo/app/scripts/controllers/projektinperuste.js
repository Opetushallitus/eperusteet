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
        onEnter: function (SivunavigaatioService) {
          SivunavigaatioService.aseta({osiot: false});
        }
      });
  })
  .controller('ProjektinPerusteCtrl', function($scope, $rootScope, $stateParams, $state,
    YleinenData, Koodisto, Perusteet, PerusteprojektiResource) {

    $scope.hakemassa = false;
    $scope.peruste = {};
    $scope.projektiId = $stateParams.perusteProjektiId;

    PerusteprojektiResource.get({id: $stateParams.perusteProjektiId}, function(vastaus) {
      $scope.projekti = vastaus;
      if ($scope.projekti._peruste) {
        Perusteet.get({perusteenId: vastaus._peruste}, function(vastaus) {
          console.log('peruste', vastaus);
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

      $scope.hakemassa = true;
      $scope.peruste.nimi = koodisto.nimi;
      $scope.peruste.koodi = koodisto.koodi;
      $scope.peruste.koulutukset = [];
      $scope.peruste.koulutukset.push({});
      $scope.peruste.koulutukset[0].koulutuskoodi = koodisto.koodi;

      Koodisto.haeAlarelaatiot($scope.peruste.koodi, function(relaatiot) {
        _.forEach(relaatiot, function(rel) {
          switch (rel.koodisto.koodistoUri) {
            case 'koulutusalaoph2002':
              $scope.peruste.koulutukset[0].koulutusalakoodi = rel.koodi;
              break;
            case 'opintoalaoph2002':
              $scope.peruste.koulutukset[0].opintoalakoodi = rel.koodi;
              break;
          }
        });
      }, function(virhe) {
        console.log('koodisto alarelaatio virhe', virhe);
      });
    };
    
    $scope.tallennaPeruste = function() {
      Perusteet.save({perusteenId: $scope.peruste.id}, $scope.peruste, function(vastaus) {
          console.log('tallennettu peruste', vastaus);
          $scope.peruste = vastaus;
          $state.go('perusteprojekti.editoi.sisalto', {perusteProjektiId: $scope.projektiId}, {reload: true});
        }, function(virhe) {
          console.log('perusteen tallennus virhe', virhe);
        });
    };

    $scope.valitseKieli = function(teksti) {
      return YleinenData.valitseKieli(teksti);
    };

    $scope.koulutusalaNimi = function(koodi) {
      return $scope.Koulutusalat.haeKoulutusalaNimi(koodi);
    };

    $scope.opintoalaNimi = function(koodi) {
      return $scope.Opintoalat.haeOpintoalaNimi(koodi);
    };
    
    $rootScope.$on('event:spinner_on', function () {
      $scope.hakemassa = true;
    });
    
    $rootScope.$on('event:spinner_off', function () {
      $scope.hakemassa = false;
    });

  });
