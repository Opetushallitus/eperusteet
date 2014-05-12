'use strict';

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('perusteprojekti.editoi.muodostumissaannot', {
        url: '/tutkinnonrakenne',
        templateUrl: 'views/perusteprojektiMuodostumissaannot.html',
        controller: 'PerusteprojektiMuodostumissaannotCtrl',
        naviRest: ['muodostumissaannot'],
        onEnter: ['SivunavigaatioService', function (SivunavigaatioService) {
          SivunavigaatioService.aseta({osiot: true});
        }]
      });
  })
  .controller('PerusteprojektiMuodostumissaannotCtrl', function($scope, $rootScope, $state, $stateParams,
              Navigaatiopolku, PerusteProjektiService, PerusteRakenteet, PerusteenRakenne, TreeCache, Notifikaatiot,
              Editointikontrollit) {
    $scope.editoi = false;
    $scope.rakenne = {
      $resolved: false,
      rakenne: {
        osat: []
      },
      tutkinnonOsat: {}
    };

    function haeRakenne() {
      $scope.rakenne.$resolved = false;
      PerusteenRakenne.hae($stateParams.perusteProjektiId, function(res) {
        $scope.rakenne = res;
        $scope.rakenne.$resolved = true;
        $scope.rakenne.$suoritustapa = $scope.rakenne.$suoritustapa || $scope.rakenne.$peruste.suoritustavat[0].suoritustapakoodi;
      });
    }

    if (TreeCache.nykyinen() !== $stateParams.perusteenId) { haeRakenne(); }
    else { TreeCache.hae(); }

    function tallennaRakenne(rakenne) {
      TreeCache.tallenna(rakenne, $stateParams.perusteenId);
      PerusteenRakenne.tallenna(
        rakenne,
        rakenne.$peruste.id,
        $scope.rakenne.$suoritustapa,
        function() { Notifikaatiot.onnistui('tallentaminen-onnistui', ''); },
        function(virhe) { Notifikaatiot.varoitus('tallentaminen-epäonnistui', virhe); }
      );
    }

    Editointikontrollit.registerCallback({
      edit: function() {
        $scope.editoi = true;
      },
      save: function() {
        tallennaRakenne($scope.rakenne);
        $scope.editoi = false;
      },
      cancel: function() {
        $scope.editoi = false;
      }
    });
  });
