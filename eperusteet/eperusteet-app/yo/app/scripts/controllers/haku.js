'use strict';
/*global _*/

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('selaus', {
        url: '/selaus',
        template: '<div ui-view></div>'
      })
      .state('selaus.ammatillinenperuskoulutus', {
        url: '/ammatillinenperuskoulutus',
        templateUrl: 'views/haku.html',
        controller: 'HakuCtrl',
        naviBase: ['haku', 'ammatillinen-peruskoulutus'],
        resolve: {'koulutusalaService': 'Koulutusalat'}
      })
      .state('selaus.ammatillinenaikuiskoulutus', {
        url: '/ammatillinenaikuiskoulutus',
        templateUrl: 'views/haku.html',
        controller: 'HakuCtrl',
        naviBase: ['haku', 'ammatillinen-aikuiskoulutus'],
        resolve: {'koulutusalaService': 'Koulutusalat'}
      });
  })
  .controller('HakuCtrl', function($scope, $rootScope, $state, Perusteet, Haku,
    YleinenData, koulutusalaService) {
    var pat = '';
    // Viive, joka odotetaan, ennen kuin haku nimi muutoksesta lähtee serverille.
    var hakuViive = 300; //ms
    $scope.nykyinenSivu = 0;
    $scope.sivuja = 1;
    $scope.kokonaismaara = 0;
    $scope.koulutusalat = koulutusalaService.haeKoulutusalat();
    $scope.hakuparametrit = Haku.getHakuparametrit($state.current.name);

    $scope.tutkintotyypit = {
      'koulutustyyppi_1': 'tutkintotyyppikoodi-1',
      'koulutustyyppi_11': 'tutkintotyyppikoodi-2',
      'koulutustyyppi_12': 'tutkintotyyppikoodi-3'
    };

    $scope.tyhjenna = function() {
      $scope.nykyinenSivu = 0;
      $scope.hakuparametrit = Haku.resetHakuparametrit($state.current.name);
      $scope.haePerusteet($scope.nykyinenSivu);
    };
    
    var hakuVastaus = function(vastaus) {
      $scope.perusteet = vastaus;
      $scope.nykyinenSivu = $scope.perusteet.sivu;
      $scope.hakuparametrit.sivukoko = $scope.perusteet.sivukoko;
      $scope.sivuja = $scope.perusteet.sivuja;
      $scope.kokonaismaara = $scope.perusteet.kokonaismäärä;
      $scope.sivut = _.range(0, $scope.perusteet.sivuja);
      pat = new RegExp('(' + $scope.hakuparametrit.nimi + ')', 'i');
    };
    
    $scope.haePerusteet = function(sivu) {
      $scope.hakuparametrit.sivu = sivu;
      Haku.setHakuparametrit($state.current.name, $scope.hakuparametrit);
      Perusteet.query(Haku.getHakuparametrit($state.current.name), hakuVastaus, function(virhe) {
        if (virhe.status === 404) {
          hakuVastaus(virhe.data);
        }
      });
    };
    
    $scope.sivujaYhteensa = function() {
      return Math.max($scope.sivuja, 1);
    };
    
    $scope.hakuMuuttui = _.debounce(_.bind($scope.haePerusteet, $scope, 0), hakuViive, {'leading': false});

    $scope.edellinenSivu = function() {
      if ($scope.nykyinenSivu > 0) {
        $scope.haePerusteet($scope.nykyinenSivu - 1);
      }
    };
    
    $scope.seuraavaSivu = function() {
      if ($scope.nykyinenSivu < $scope.sivujaYhteensa() - 1) {
        $scope.haePerusteet($scope.nykyinenSivu + 1);
      }
    };
    
    $scope.korosta = function(otsikko) {
      if ($scope.hakuparametrit.nimi === null || $scope.hakuparametrit.nimi.length < 3) {
        return otsikko;
      }
      return otsikko.replace(pat, '<b>$1</b>');
    };
    $scope.valitseKieli = function(nimi) {
      return YleinenData.valitseKieli(nimi);
    };

    /*$rootScope.$on('$translateChangeSuccess', function() {
      $scope.tyhjenna();
    });*/

    $scope.koulutusalaMuuttui = function() {
      if ($scope.hakuparametrit.koulutusala !== '') {
        $scope.opintoalat = _.findWhere($scope.koulutusalat, {koodi: $scope.hakuparametrit.koulutusala}).opintoalat;
      } else {
        $scope.opintoalat = [];
      }
      $scope.hakuMuuttui();
    };
    $scope.koulutusalaMuuttui();

    $scope.koulutusalaNimi = function(koodi) {
      return koulutusalaService.haeKoulutusalaNimi(koodi);
    };
    
    $scope.piilotaTutkintotyyppi = function() {
      return $state.current.name === 'selaus.ammatillinenperuskoulutus';
    };
  });
