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
        url: '/:id',
        templateUrl: 'views/perusteprojekti.html',
        controller: 'PerusteprojektiCtrl',
        navigaationimiId: 'projektiId',
        resolve: {'koulutusalaService': 'Koulutusalat',
                  'opintoalaService': 'Opintoalat'}
      });
  })
  .controller('PerusteprojektiCtrl', function($scope, $rootScope, $state, $stateParams,
    PerusteprojektiResource, PerusteProjektiService, YleinenData, koulutusalaService, opintoalaService) {

    $scope.Koulutusalat = koulutusalaService;
    $scope.Opintoalat = opintoalaService;
    $scope.projekti = {};
    $scope.projekti.peruste = {};
    $scope.projekti.peruste.nimi = {};
    $scope.projekti.peruste.opintoalat = [];

    $scope.$watch('projekti.nimi', function(temp) {
      YleinenData.navigaatiopolkuElementit.projektiId = temp;
      $rootScope.$broadcast('paivitaNavigaatiopolku');
    });

    $scope.tabs = [{otsikko: 'projekti-perustiedot', url: 'views/partials/perusteprojektiPerustiedot.html'},
                   {otsikko: 'projekti-projektiryhmä', url: 'views/partials/perusteprojektiProjektiryhma.html'},
                   {otsikko: 'projekti-peruste', url: 'views/partials/perusteprojektiPeruste.html'}];

    if ($stateParams.id !== 'uusi') {
      $scope.projekti.id = $stateParams.id;
      PerusteprojektiResource.get($scope.projekti, function(vastaus) {
        $scope.projekti = vastaus;
      });
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
          $state.go('perusteprojekti.editoi', { id: vastaus.id });
        });
      }
    };

    $scope.paivitaNavigaatiopolku = function (nimi) {
      YleinenData.navigaatiopolkuElementit.projektiId = nimi;
      $rootScope.$broadcast('paivitaNavigaatiopolku');
    };
  });
