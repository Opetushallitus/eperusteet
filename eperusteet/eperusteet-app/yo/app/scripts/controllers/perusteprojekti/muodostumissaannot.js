'use strict';

angular.module('eperusteApp')
  .controller('PerusteprojektiMuodostumissaannotCtrl', function($scope, $stateParams,
              PerusteProjektiService, PerusteenRakenne, TreeCache, Notifikaatiot,
              Editointikontrollit, SivunavigaatioService, Kommentit, KommentitBySuoritustapa, Lukitus) {
    $scope.editoi = false;
    // $scope.suoritustapa = PerusteProjektiService.getSuoritustapa();
    $scope.suoritustapa = $stateParams.suoritustapa;
    $scope.rakenne = {
      $resolved: false,
      rakenne: { osat: [] },
      tutkinnonOsat: {}
    };
    $scope.versiot = {};

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
      PerusteenRakenne.tallennaRakenne(
        rakenne,
        rakenne.$peruste.id,
        $scope.suoritustapa,
        function() {
          Notifikaatiot.onnistui('tallennus-onnistui');
        },
        function() {
          Lukitus.vapautaSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa);
        }
      );
    }

    $scope.muokkaa = function () {
      Lukitus.lukitseSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa, function() {
        Editointikontrollit.startEditing();
        $scope.editoi = true;
      });
    };

    Editointikontrollit.registerCallback({
      edit: function() {
        $scope.editoi = true;
      },
      validate: function() {
        console.log('Muodostumissäännöiltä puuttuu validointi. Toteuta.');
        return true;
      },
      save: function() {
        tallennaRakenne($scope.rakenne);
        $scope.editoi = false;
      },
      cancel: function() {
        Lukitus.vapautaSisalto($scope.rakenne.$peruste.id, $scope.suoritustapa);
        haeRakenne();
        $scope.editoi = false;
      }
    });

    $scope.$watch('editoi', function (editoi) {
      SivunavigaatioService.aseta({osiot: !editoi});
    });
  });
