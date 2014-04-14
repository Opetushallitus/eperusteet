'use strict';

angular.module('eperusteApp')
.config(function($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.when('/perusteprojekti', '/perusteprojekti/uusi');
    $urlRouterProvider.when('/perusteprojekti/', '/perusteprojekti/uusi');
    $stateProvider
      .state('perusteprojekti', {
        url: '/perusteprojekti',
        navigaationimi: 'navi-perusteprojekti',
        template: '<div ui-view></div>'
      })
      .state('perusteprojekti.editoi', {
        url: '/:perusteProjektiId',
        templateUrl: 'views/perusteprojekti.html',
        controller: 'PerusteprojektiCtrl',
        naviBase: ['perusteprojekti', ':perusteProjektiId'],
        navigaationimiId: 'projektiId',
        resolve: {'koulutusalaService': 'Koulutusalat'}
      });
    })
  .controller('PerusteprojektiCtrl', function ($scope, $stateParams, Navigaatiopolku,
   PerusteprojektiResource, PerusteProjektiService, YleinenData, koulutusalaService) {
     PerusteProjektiService.watcher($scope, 'projekti');
     
     $scope.projekti = {};
     //$scope.projekti = PerusteProjektiService.get();
     console.log('projekti', $scope.projekti);
    
    console.log('$stateparams', $stateParams);
    if ($stateParams.perusteProjektiId !== 'uusi') {
      $scope.projekti.id = $stateParams.perusteProjektiId;
      PerusteprojektiResource.get({ id: $stateParams.perusteProjektiId }, function(vastaus) {
        $scope.projekti = vastaus;
        Navigaatiopolku.asetaElementit({ perusteProjektiId: vastaus.nimi });
      }, function(virhe) {
        console.log('virhe', virhe);
      });
    } else {
        Navigaatiopolku.asetaElementit({ perusteProjektiId: 'uusi' });
    }
    
    $scope.valitseKieli = function(nimi) {
      return YleinenData.valitseKieli(nimi);
    };
    
    $scope.koulutusalaNimi = function(koodi) {
      return koulutusalaService.haeKoulutusalaNimi(koodi);
    };
    
  });
