'use strict';
/*global _*/

angular.module('eperusteApp')
  .controller('ProjektinPerusteCtrl', function($scope, PerusteProjektiService,
      YleinenData, Koodisto) {
    PerusteProjektiService.watcher($scope, 'projekti');

    //$scope.projekti = PerusteProjektiService.get();
    
    $scope.koodistoHaku = function(koodisto) {
      console.log('koodisto', koodisto);
      
      $scope.projekti.peruste.nimi = koodisto.nimi;
      $scope.projekti.peruste.koodi = koodisto.koodi;
      
      Koodisto.haeAlarelaatiot($scope.projekti.peruste.koodi, function (relaatiot) {
        
        _.forEach(relaatiot, function(rel) {
          switch (rel.koodisto.koodistoUri) {
            case 'koulutusalaoph2002':
              $scope.projekti.peruste.koulutusala = rel.koodi;
              break;
            case 'opintoalaoph2002':
              $scope.projekti.peruste.opintoalat.push(rel.koodi);
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
