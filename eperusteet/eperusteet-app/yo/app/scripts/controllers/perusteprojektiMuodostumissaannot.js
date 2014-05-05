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
    Navigaatiopolku, PerusteProjektiService, PerusteRakenteet, PerusteenRakenne, TreeCache) {

    $scope.rakenne = {
      $resolved: false,
      rakenne: {
        osat: []
      },
      tutkinnonOsat: {}
    };

    function haeRakenne() {
      PerusteenRakenne.hae($stateParams.perusteProjektiId, function(res) {
        $scope.rakenne = res;
        $scope.rakenne.$resolved = true;
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
