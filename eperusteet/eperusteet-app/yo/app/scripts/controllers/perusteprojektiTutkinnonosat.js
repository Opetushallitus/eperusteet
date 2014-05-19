'use strict';
/* global _ */

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('perusteprojekti.editoi.tutkinnonosat', {
        url: '/tutkinnonosat',
        templateUrl: 'views/perusteprojektiTutkinnonosat.html',
        controller: 'PerusteprojektiTutkinnonOsatCtrl',
        naviRest: ['tutkinnonosat'],
        onEnter: ['SivunavigaatioService', function(SivunavigaatioService) {
            SivunavigaatioService.aseta({osiot: true});
          }]
      });
  })
  .controller('PerusteprojektiTutkinnonOsatCtrl', function($scope, $rootScope, $state, $stateParams,
    Navigaatiopolku, PerusteProjektiService, PerusteRakenteet, PerusteenRakenne, TreeCache, Notifikaatiot,
    Editointikontrollit, Kaanna, PerusteTutkinnonosa, TutkinnonOsanTuonti) {

    $scope.editoi = false;
    $scope.suoritustapa = PerusteProjektiService.getSuoritustapa();
    $scope.tosarajaus = '';
    $scope.rakenne = {
      $resolved: false,
      rakenne: {osat: []},
      tutkinnonOsat: {}
    };

    $scope.paivitaRajaus = function(rajaus) { $scope.tosarajaus = rajaus; };

    function haeRakenne() {
      PerusteenRakenne.hae($stateParams.perusteProjektiId, $scope.suoritustapa, function(res) {
        res.$suoritustapa = $scope.suoritustapa;
        res.$resolved = true;
        $scope.rakenne = res;
      });
    }
    $scope.haeRakenne = haeRakenne;
    haeRakenne();

    function tallennaRakenne(rakenne) {
      TreeCache.tallenna(rakenne, $stateParams.perusteenId);
      PerusteenRakenne.tallenna(
        rakenne,
        rakenne.$peruste.id,
        $scope.suoritustapa,
        function() { Notifikaatiot.onnistui(); },
        Notifikaatiot.serverCb
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

    $scope.tuoTutkinnonosa = TutkinnonOsanTuonti.modaali($scope.suoritustapa, function(osat) {
      _.forEach(osat, function(osa) { $scope.lisaaTutkinnonOsa(osa); });
    });

    $scope.lisaaTutkinnonOsa = function(osa, cb) {
      if (osa) {
        osa = {_tutkinnonOsa: osa._tutkinnonOsa};
      }
      else {
        osa = {};
      }
      cb = cb || angular.noop;

      PerusteTutkinnonosa.save({
        perusteenId: $scope.rakenne.$peruste.id,
        suoritustapa: $scope.rakenne.$suoritustapa
      }, osa, function(res) {
        $scope.rakenne.tutkinnonOsat[res._tutkinnonOsa] = res;
        cb();
        $state.go('perusteprojekti.editoi.perusteenosa', {
          perusteenOsaId: res._tutkinnonOsa,
          perusteenOsanTyyppi: 'tutkinnonosa'
        });
      }, function(err) {
        Notifikaatiot.fataali('tallennus-epäonnistui', err);
        cb();
      });
    };

  });
