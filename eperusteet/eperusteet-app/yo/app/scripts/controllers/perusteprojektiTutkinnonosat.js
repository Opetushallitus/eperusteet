'use strict';
/* global _ */

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('perusteprojekti.editoi.tutkinnonosat', {
        url: '/tutkinnonosat',
        templateUrl: 'views/perusteprojektiTutkinnonosat.html',
        controller: 'PerusteprojektiMuodostumissaannotCtrl',
        naviRest: ['tutkinnonosat'],
        onEnter: ['SivunavigaatioService', function (SivunavigaatioService) {
          SivunavigaatioService.aseta({osiot: true});
        }]
      });
  })
  .controller('PerusteprojektiMuodostumissaannotCtrl', function($scope, $rootScope, $state, $stateParams,
              Navigaatiopolku, PerusteProjektiService, PerusteRakenteet, PerusteenRakenne, TreeCache, Notifikaatiot,
              Editointikontrollit, Kaanna) {
    $scope.rakenne = {
      $resolved: false,
      rakenne: { osat: [] },
      tutkinnonOsat: {}
    };

    $scope.editoi = false;
    $scope.tosarajaus = '';

    $scope.vaihdaSuoritustapa = function() { haeRakenne(); };

    $scope.paivitaRajaus = function(rajaus) { $scope.tosarajaus = rajaus; };

    function haeRakenne(st) {
      if (st) { $scope.rakenne.$suoritustapa = st.suoritustapakoodi; }
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

    $scope.rajaaTutkinnonOsia = function(haku) {
      return Kaanna.kaanna(haku.nimi).toLowerCase().indexOf($scope.tosarajaus.toLowerCase()) !== -1;
    };
  });
