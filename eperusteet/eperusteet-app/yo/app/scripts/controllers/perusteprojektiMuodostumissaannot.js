'use strict';
/* global _ */

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('perusteprojekti.editoi.muodostumissaannot', {
        url: '/muodostumissaannot',
        templateUrl: 'views/perusteprojektiMuodostumissaannot.html',
        controller: 'PerusteprojektiMuodostumissaannotCtrl',
        naviRest: ['muodostumissaannot'],
        onEnter: function (SivunavigaatioService) {
          SivunavigaatioService.aseta({osiot: true});
        }
      });
  })
  .controller('PerusteprojektiMuodostumissaannotCtrl', function($scope, $rootScope, $state, $stateParams,
    Navigaatiopolku, PerusteprojektiResource, PerusteProjektiService, PerusteRakenteet, TreeCache, PerusteTutkinnonosat, Perusteet) {

    $scope.rakenne = {
      $resolved: false,
      rakenne: {
        osat: []
      },
      tutkinnonOsat: {}
    };

    function haeRakenne() {
      PerusteprojektiResource.get({ id: $stateParams.perusteProjektiId }, function(vastaus) {
        PerusteProjektiService.save(vastaus);
        Perusteet.get({
          perusteenId: vastaus._peruste
        }, function(peruste) {
          PerusteRakenteet.get({
            perusteenId: peruste.id,
            suoritustapa: peruste.suoritustavat[0].suoritustapakoodi // FIXME
          }, function(rakenne) {
            PerusteTutkinnonosat.query({
              perusteenId: peruste.id,
              suoritustapa: peruste.suoritustavat[0].suoritustapakoodi // FIXME
            }, function(tosat) {
              $scope.rakenne.rakenne = rakenne;
              $scope.rakenne.tutkinnonOsat = _.zipObject(_.pluck(tosat, '_tutkinnonOsa'), tosat);
              $scope.rakenne.$resolved = true;
            });
          }, function() {
            $scope.rakenne.$resolved = true;
          });
        });
      });
    }

    $scope.peruMuutokset = haeRakenne;

    if (TreeCache.nykyinen() !== $stateParams.perusteenId) { haeRakenne(); }
    else { TreeCache.hae(); }

    $scope.tallennaRakenne = function(rakenne) {
      TreeCache.tallenna(rakenne, $stateParams.perusteenId);
      rakenne.tutkinnonOsat = _.values(rakenne.tutkinnonOsat);
      PerusteRakenteet.save({
        perusteenId: PerusteProjektiService.get().peruste.id,
        suoritustapa: 'naytto' // FIXME
      }, rakenne, function(re) {
        console.log(re);
      });
    };
  });
