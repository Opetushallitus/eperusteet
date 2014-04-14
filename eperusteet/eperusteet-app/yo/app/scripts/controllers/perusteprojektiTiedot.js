'use strict';

angular.module('eperusteApp')
  .config(function($stateProvider, $urlRouterProvider) {
    $stateProvider
      .state('perusteprojektiTiedot', {
        url: '/perusteprojekti',
        navigaationimi: 'navi-perusteprojekti-tiedot',
        template: '<div ui-view></div>'
      })
      .state('perusteprojektiTiedot.editoi', {
        url: '/projektinTiedot/:perusteProjektiId',
        templateUrl: 'views/perusteprojektiTiedot.html',
        controller: 'PerusteprojektiTiedotCtrl',
        naviBase: ['perusteprojekti', ':perusteProjektiId'],
        navigaationimiId: 'projektiId',
        resolve: {'koulutusalaService': 'Koulutusalat',
                  'opintoalaService': 'Opintoalat'}
      });
  })
  .controller('PerusteprojektiTiedotCtrl', function($scope, $rootScope, $state, $stateParams,
    PerusteprojektiResource, PerusteProjektiService, Navigaatiopolku, koulutusalaService, opintoalaService) {
      
      $scope.uusi = 'uusi';

    $scope.Koulutusalat = koulutusalaService;
    $scope.Opintoalat = opintoalaService;
    $scope.projekti = {};
    $scope.projekti.peruste = {};
    $scope.projekti.peruste.nimi = {};
    $scope.projekti.peruste.koulutukset = [];

    $scope.tabs = [{otsikko: 'projekti-perustiedot', url: 'views/partials/perusteprojektiPerustiedot.html'},
                   {otsikko: 'projekti-projektiryhmä', url: 'views/partials/perusteprojektiProjektiryhma.html'},
                   {otsikko: 'projekti-peruste', url: 'views/partials/perusteprojektiPeruste.html'}];

    console.log('$stateparams', $stateParams);
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
      if (projekti.id) {
        PerusteprojektiResource.update(projekti, function() {
          PerusteProjektiService.update();
        });
      } else {
        PerusteprojektiResource.save(projekti, function(vastaus) {
          PerusteProjektiService.update();
          $state.go('perusteprojektiTiedot.editoi', { perusteProjektiId: vastaus.id });
        }, function(virhe) {
          console.log('virhe', virhe);
        });
      }
    };
  });
