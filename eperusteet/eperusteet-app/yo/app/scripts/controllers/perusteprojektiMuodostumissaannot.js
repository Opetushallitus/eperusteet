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
    Navigaatiopolku, PerusteRakenteet, TreeCache) {

    $scope.rakenne = {
      rakenne: {
        osat: []
      },
      tutkinnonOsat: {},
    };

    function haeRakenne() {
      PerusteRakenteet.get({
          perusteenId: $stateParams.perusteenId,
          suoritustapa: 'naytto' // FIXME
      }, function(re) {
        $scope.rakenne = re;
        $scope.rakenne.tutkinnonOsat = _.zipObject(_.pluck($scope.rakenne.tutkinnonOsat, '_tutkinnonOsa'), $scope.rakenne.tutkinnonOsat);
      }, function() {
        $scope.rakenne.$resolved = true;
      });
    }

    $scope.peruMuutokset = haeRakenne;

    if (TreeCache.nykyinen() !== $stateParams.perusteenId) { haeRakenne(); }
    else { TreeCache.hae(); }

    $scope.tallennaRakenne = function(rakenne) {
      TreeCache.tallenna(rakenne, $stateParams.perusteenId);
      rakenne.tutkinnonOsat = _.values(rakenne.tutkinnonOsat);
      PerusteRakenteet.save({ perusteenId: $stateParams.perusteenId }, rakenne, function(re) {
        console.log(re);
      });
    };
  });
