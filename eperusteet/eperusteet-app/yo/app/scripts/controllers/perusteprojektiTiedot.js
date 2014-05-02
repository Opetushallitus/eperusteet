'use strict';

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('perusteprojekti.tiedot', {
        //url: '/perustiedot',
        templateUrl: 'views/partials/perusteprojektiTiedotUusi.html',
        controller: 'PerusteprojektiTiedotCtrl',
        naviBase: ['perusteprojekti', ':perusteProjektiId'],
        navigaationimiId: 'perusteProjektiId',
        resolve: {'opintoalaService': 'Opintoalat'},
        abstract: true
      }).state('perusteprojekti.tiedot.uusi', {
        url: '/perustiedot',
        templateUrl: 'views/perusteprojektiTiedot.html',
        controller: 'PerusteprojektiTiedotCtrl',
        naviBase: ['perusteprojekti', ':perusteProjektiId'],
        navigaationimiId: 'perusteProjektiId',
        onEnter: function (SivunavigaatioService) {
          SivunavigaatioService.aseta({osiot: false});
        }
      })
      .state('perusteprojekti.editoi.tiedot', {
        url: '/perustiedot',
        templateUrl: 'views/perusteprojektiTiedot.html',
        controller: 'PerusteprojektiTiedotCtrl',
        naviBase: ['perusteprojekti', ':perusteProjektiId'],
        navigaationimiId: 'perusteProjektiId',
        resolve: {'opintoalaService': 'Opintoalat'},
        onEnter: function (SivunavigaatioService) {
          SivunavigaatioService.aseta({osiot: false});
        }
      });
  })
  .controller('PerusteprojektiTiedotCtrl', function($scope, $rootScope, $state, $stateParams,
    PerusteprojektiResource, PerusteProjektiService, Navigaatiopolku, koulutusalaService, opintoalaService) {
    PerusteProjektiService.watcher($scope, 'projekti');

    $scope.Koulutusalat = koulutusalaService;
    $scope.Opintoalat = opintoalaService;
    $scope.projekti = {};
    PerusteProjektiService.clean();

    $scope.projekti.id = $stateParams.perusteProjektiId;

    $scope.tabs = [{otsikko: 'projekti-perustiedot', url: 'views/partials/perusteprojektiPerustiedot.html'},
                   {otsikko: 'projekti-toimikausi', url: 'views/partials/perusteprojektiToimikausi.html'}];

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
        // Poista tämä hackkin, kun keksitty parempi tapa viedä koulutustyyppi uuden projektin luonnissa.
        // Uuden projektin luonti dto:ssa kulkee koulutustyyppi, mutta ei normaalissa perusteprojektiDto:ssa
        delete projekti.koulutustyyppi;
        PerusteprojektiResource.update(projekti, function(vastaus) {
          PerusteProjektiService.save(vastaus);
          PerusteProjektiService.update();
          avaaProjektinSisalto(vastaus.id);
        }, function (virhe) {
          console.log('projektin tallennusvirhe', virhe);
        });
      } else {
        projekti.id = null;
        PerusteprojektiResource.save(projekti, function(vastaus) {
          PerusteProjektiService.save(vastaus);
          PerusteProjektiService.update();
          avaaProjektinSisalto(vastaus.id);
        }, function(virhe) {
          console.log('projektin luontivirhe', virhe);
        });
      }
    };

    var avaaProjektinSisalto = function(projektiId) {
      $state.go('perusteprojekti.editoi.sisalto', {perusteProjektiId: projektiId}, {reload:true});
    };

  });
