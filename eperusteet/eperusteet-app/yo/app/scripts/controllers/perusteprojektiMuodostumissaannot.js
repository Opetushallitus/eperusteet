'use strict';
/* global _ */

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

    $scope.vaihdaSuoritustapa = function() {
      haeRakenne();
    };

    function haeRakenne(st) {
      if (st) {
        $scope.rakenne.$suoritustapa = st.suoritustapakoodi;
      }
      PerusteenRakenne.hae($stateParams.perusteProjektiId, $scope.rakenne.$suoritustapa, function(res) {
        res.$resolved = true;
        res.$suoritustavat = _.sortBy(res.$peruste.suoritustavat, function(st) { return st.suoritustapakoodi; });
        res.$suoritustapa = $scope.rakenne.$suoritustapa || res.$suoritustavat[0].suoritustapakoodi;
        $scope.rakenne = res;
      });
    }
    $scope.haeRakenne = haeRakenne;

    if (TreeCache.nykyinen() !== $stateParams.perusteenId) { haeRakenne(); }
    else { TreeCache.hae(); }

    function tallennaRakenne(rakenne) {
      console.log($scope.rakenne.$suoritustapa);
      TreeCache.tallenna(rakenne, $stateParams.perusteenId);
      PerusteenRakenne.tallenna(
        rakenne,
        rakenne.$peruste.id,
        $scope.rakenne.$suoritustapa,
        function() { Notifikaatiot.onnistui('tallennus-onnistui', ''); },
        function(virhe) { Notifikaatiot.varoitus('tallennus-epäonnistui', virhe); }
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
        haeRakenne();
        $scope.editoi = false;
      }
    });
  });
