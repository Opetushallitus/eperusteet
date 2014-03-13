'use strict';
/*global _*/

angular.module('eperusteApp')
  .controller('ProjektinPerusteCtrl', function($scope, PerusteProjektiService,
      YleinenData, Projektinperuste) {
    PerusteProjektiService.watcher($scope, 'projekti');

    //$scope.projekti = PerusteProjektiService.get();
    
    $scope.koodistoHaku = function(koodisto) {
      
      $scope.projekti.peruste.nimi = koodisto.nimi;
      $scope.projekti.peruste.koodi = koodisto.koodi;
      
      Projektinperuste.query({koodi: $scope.projekti.peruste.koodi}, function (vastaus) {
        var relaatiot = _.map(vastaus, function(kv) {
          var nimi = {
            fi: '',
            sv: '',
            en: ''
          };
          _.forEach(kv.metadata, function(obj) {
            nimi[obj.kieli.toLowerCase()] = obj.nimi;
          });
          return {
            koodi: kv.koodiUri,
            nimi: nimi,
            koodisto: kv.koodisto
          };
        });
        
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
        console.log('relaatio', relaatiot);
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
