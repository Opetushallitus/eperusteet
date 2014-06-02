'use strict';

angular.module('eperusteApp')
  .controller('PerusteprojektiMuodostumissaannotCtrl', function($scope, $stateParams,
              PerusteProjektiService, PerusteenRakenne, TreeCache, Notifikaatiot,
              Editointikontrollit, SivunavigaatioService, Kommentit, KommentitBySuoritustapa) {
    $scope.editoi = false;
    $scope.suoritustapa = PerusteProjektiService.getSuoritustapa();
    $scope.rakenne = {
      $resolved: false,
      rakenne: { osat: [] },
      tutkinnonOsat: {}
    };

    Kommentit.haeKommentit(KommentitBySuoritustapa, { id: $stateParams.perusteProjektiId, suoritustapa: $scope.suoritustapa });

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
        function() { Notifikaatiot.onnistui('tallennus-onnistui'); },
        Notifikaatiot.serverCb
      );
    }

    $scope.muokkaa = function () {
      Editointikontrollit.startEditing();
      $scope.editoi = true;
    };

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

    $scope.$watch('editoi', function (editoi) {
      SivunavigaatioService.aseta({osiot: !editoi});
    });
  });
