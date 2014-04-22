'use strict';
/*global _*/

angular.module('eperusteApp')
  .controller('ProjektinPerusteCtrl', function($scope, PerusteProjektiService,
      YleinenData, Koodisto) {
    PerusteProjektiService.watcher($scope, 'projekti');

    $scope.rajaaKoodit = function(koodi) {
      return koodi.koodi.indexOf('_3') !== -1;
    };

    $scope.koodistoHaku = function(koodisto) {

      $scope.projekti.peruste.nimi = koodisto.nimi;
      $scope.projekti.peruste.koodi = koodisto.koodi;
      $scope.projekti.peruste.koulutukset.length = 0;
      $scope.projekti.peruste.koulutukset.push({});
      $scope.projekti.peruste.koulutukset[0].koulutuskoodi = koodisto.koodi;
      $scope.projekti.peruste.suoritustavat = [{suoritustapakoodi: 'ops'}];

      Koodisto.haeAlarelaatiot($scope.projekti.peruste.koodi, function (relaatiot) {
        _.forEach(relaatiot, function(rel) {
          switch (rel.koodisto.koodistoUri) {
            case 'koulutusalaoph2002':
              $scope.projekti.peruste.koulutukset[0].koulutusalakoodi = rel.koodi;
              break;
            case 'opintoalaoph2002':
              $scope.projekti.peruste.koulutukset[0].opintoalakoodi = rel.koodi;
              break;
            case 'koulutustyyppi':
              if (rel.koodi === 'koulutustyyppi_1' || rel.koodi === 'koulutustyyppi_11' || rel.koodi === 'koulutustyyppi_12') {
                $scope.projekti.peruste.tutkintokoodi = rel.koodi;
              }
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
