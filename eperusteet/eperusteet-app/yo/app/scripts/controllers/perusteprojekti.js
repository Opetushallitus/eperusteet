'use strict';

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('perusteprojekti', {
        url: '/perusteprojekti',
        navigaationimi: 'navi-perusteprojekti',
        template: '<div ui-view></div>',
      })
      .state('perusteprojekti.editoi', {
        url: '/:perusteProjektiId',
        templateUrl: 'views/perusteprojekti.html',
        controller: 'PerusteprojektiCtrl',
        naviBase: ['perusteprojekti', ':perusteProjektiId'],
        navigaationimiId: 'projektiId',
        resolve: {'koulutusalaService': 'Koulutusalat',
                  'opintoalaService': 'Opintoalat'}
      });
  })
  .controller('PerusteprojektiCtrl', function($scope, $rootScope, $state, $stateParams,
    PerusteprojektiResource, PerusteProjektiService, Navigaatiopolku, koulutusalaService, opintoalaService) {

    $scope.Koulutusalat = koulutusalaService;
    $scope.Opintoalat = opintoalaService;
    $scope.projekti = {};
    $scope.projekti.peruste = {};
    $scope.projekti.peruste.nimi = {};
    $scope.projekti.peruste.opintoalat = [];

    $scope.tabs = [{otsikko: 'projekti-perustiedot', url: 'views/partials/perusteprojektiPerustiedot.html'},
                   {otsikko: 'projekti-projektiryhmä', url: 'views/partials/perusteprojektiProjektiryhma.html'},
                   {otsikko: 'projekti-peruste', url: 'views/partials/perusteprojektiPeruste.html'}];

    if ($stateParams.perusteProjektiId !== 'uusi') {
      $scope.projekti.perusteProjektiId = $stateParams.perusteProjektiId;
      PerusteprojektiResource.get({ id: $stateParams.perusteProjektiId }, function(vastaus) {
        $scope.projekti = vastaus;
        Navigaatiopolku.asetaElementit({ perusteProjektiId: vastaus.nimi });
      });
    } else {
        Navigaatiopolku.asetaElementit({ perusteProjektiId: 'uusi' });
    }

    $scope.tallennaPerusteprojekti = function() {
      var projekti = PerusteProjektiService.get();

      if (projekti.perusteProjektiId) {
        PerusteprojektiResource.update(projekti, function() {
          PerusteProjektiService.update();
        });
      } else {
        PerusteprojektiResource.save(projekti, function(vastaus) {
          PerusteProjektiService.update();
          $state.go('perusteprojekti.editoi', { id: vastaus.perusteProjektiId });
        });
      }
    };
  });
