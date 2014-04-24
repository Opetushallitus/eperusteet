'use strict';
/* global _ */

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('perusteprojekti.editoi.muodostumissaannot', {
        url: '/muodostumissaannot',
        templateUrl: 'views/perusteprojektiMuodostumissaannot.html',
        controller: 'PerusteprojektiMuodostumissaannotCtrl',
        naviRest: ['muodostumissaannot']
      });
  })
  .controller('PerusteprojektiMuodostumissaannotCtrl', function($scope, $rootScope, $state, $stateParams,
    Navigaatiopolku, PerusteprojektiResource, PerusteProjektiService, PerusteRakenteet, TreeCache) {

    $scope.rakenne = {
      rakenne: { osat: [] },
      tutkinnonOsat: {},
    };

    function haeRakenne() {
      PerusteprojektiResource.get({ id: $stateParams.perusteProjektiId }, function(vastaus) {
        PerusteProjektiService.save(vastaus);
        PerusteRakenteet.get({
          perusteenId: vastaus.peruste.id,
          suoritustapa: vastaus.peruste.suoritustavat[0].suoritustapakoodi // FIXME
        }, function(re) {
          $scope.rakenne = re;
          $scope.rakenne.tutkinnonOsat = _.zipObject(_.pluck($scope.rakenne.tutkinnonOsat, '_tutkinnonOsa'), $scope.rakenne.tutkinnonOsat);
        }, function() {
          $scope.rakenne.$resolved = true;
        });
      });
    }

    $scope.peruMuutokset = haeRakenne;

    if (TreeCache.nykyinen() !== $stateParams.perusteenId) { haeRakenne(); }
    else { TreeCache.hae(); }

    $scope.tallennaRakenne = function(rakenne) {
      console.log(rakenne);
      // TreeCache.tallenna(rakenne, $stateParams.perusteenId);
      // rakenne.tutkinnonOsat = _.values(rakenne.tutkinnonOsat);
      // PerusteRakenteet.save({
      //   perusteenId: PerusteProjektiService.get().peruste.id,
      //   suoritustapa: 'naytto' // FIXME
      // }, rakenne, function(re) {
      //   console.log(re);
      // });
    };
  });
