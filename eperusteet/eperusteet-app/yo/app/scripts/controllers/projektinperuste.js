'use strict';
/*global _*/

angular.module('eperusteApp')
  .controller('ProjektinPerusteCtrl', function($scope, PerusteProjektiService,
      YleinenData, Koodisto) {
    PerusteProjektiService.watcher($scope, 'projekti');

    $scope.koodistoHaku = function(koodisto) {

      $scope.projekti.peruste.nimi = koodisto.nimi;
      $scope.projekti.peruste.koodi = koodisto.koodi;
      $scope.projekti.peruste.koulutukset[0].koulutuskoodi = koodisto.koodi;
      
      Koodisto.haeAlarelaatiot($scope.projekti.peruste.koodi, function (relaatiot) {
        $scope.projekti.peruste.koulutukset[0].opintoalakoodi = {};
        $scope.projekti.peruste.koulutukset[0].koulutusalakoodi = {};
        
        _.forEach(relaatiot, function(rel) {
          switch (rel.koodisto.koodistoUri) {
            case 'koulutusalaoph2002':
              $scope.projekti.peruste.koulutukset[0].koulutusalakoodi = rel.koodi;
              break;
            case 'opintoalaoph2002':
              $scope.projekti.peruste.koulutukset[0].opintoalakoodi = rel.koodi;
              break;
          }
        });
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

  });
