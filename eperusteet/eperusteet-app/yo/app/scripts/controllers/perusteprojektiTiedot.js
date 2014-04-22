'use strict';

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('perusteprojekti.tiedot', {
        url: '/perustiedot',
        templateUrl: 'views/perusteprojektiTiedot.html',
        controller: 'PerusteprojektiTiedotCtrl',
        naviBase: ['perusteprojekti', ':perusteProjektiId'],
        navigaationimiId: 'perusteProjektiId',
        resolve: {'opintoalaService': 'Opintoalat'}
      });
  })
  .controller('PerusteprojektiTiedotCtrl', function($scope, $rootScope, $state, $stateParams,
    PerusteprojektiResource, PerusteProjektiService, Navigaatiopolku, koulutusalaService, opintoalaService) {
    PerusteProjektiService.watcher($scope, 'projekti');

    $scope.Koulutusalat = koulutusalaService;
    $scope.Opintoalat = opintoalaService;
    $scope.projekti = {};
    $scope.projekti.peruste = {};
    $scope.projekti.peruste.nimi = {};
    $scope.projekti.peruste.koulutukset = [];
    PerusteProjektiService.clean();

    $scope.projekti.id = $stateParams.perusteProjektiId;

    $scope.tabs = [{otsikko: 'projekti-perustiedot', url: 'views/partials/perusteprojektiPerustiedot.html'},
                   {otsikko: 'projekti-projektiryhmä', url: 'views/partials/perusteprojektiProjektiryhma.html'},
                   {otsikko: 'projekti-peruste', url: 'views/partials/perusteprojektiPeruste.html'}];

    if ($stateParams.perusteProjektiId !== 'uusi') {
      $scope.projekti.id = $stateParams.perusteProjektiId;
      PerusteprojektiResource.get({ id: $stateParams.perusteProjektiId }, function(vastaus) {
        $scope.projekti = vastaus;
        Navigaatiopolku.asetaElementit({ perusteProjektiId: vastaus.nimi });
      });
    } else {
        Navigaatiopolku.asetaElementit({ perusteProjektiId: 'uusi' });
    }

    $scope.tallennaPerusteprojekti = function() {
      var projekti = PerusteProjektiService.get();
      if (projekti.id !== 'uusi') {
        PerusteprojektiResource.update(projekti, function() {
          PerusteProjektiService.update();
        });
      } else {
        projekti.id = null;
        PerusteprojektiResource.save(projekti, function(vastaus) {
          PerusteProjektiService.update();
          $state.go('perusteprojekti.tiedot', { perusteProjektiId: vastaus.id });
        }, function(virhe) {
          console.log('virhe', virhe);
        });
      }
    };
  });
