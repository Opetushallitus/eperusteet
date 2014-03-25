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
        resolve: {'koulutusalaService': 'Koulutusalat', konteksti: function() { return 'ammatillinenperuskoulutus'; }}
      })
      .state('selaus.ammatillinenaikuiskoulutus', {
        url: '/ammatillinenaikuiskoulutus',
        templateUrl: 'views/haku.html',
        controller: 'HakuCtrl',
        naviBase: ['haku', 'ammatillinen-aikuiskoulutus'],
        resolve: {'koulutusalaService': 'Koulutusalat', konteksti: function() { return 'ammatillinenaikuiskoulutus'; }}
      });
  })
  .controller('HakuCtrl', function($scope, $rootScope, $window, $state,
    Perusteet, Haku, YleinenData, koulutusalaService, konteksti) {
    var pat = '';
    // Viive, joka odotetaan, ennen kuin haku nimi muutoksesta lähtee serverille.
    var hakuViive = 300; //ms
    $scope.nykyinenSivu = 0;
    $scope.sivuja = 1;
    $scope.kokonaismaara = 0;
    $scope.konteksti = konteksti;
    $scope.kontekstit = YleinenData.kontekstit;
    $scope.koulutusalat = koulutusalaService.haeKoulutusalat(); 
    $scope.hakuParametrit = Haku.hakuParametrit;

    $scope.tutkintotyypit = {
      'koulutustyyppi_1': 'tutkintotyyppikoodi-1',
      'koulutustyyppi_11': 'tutkintotyyppikoodi-2',
      'koulutustyyppi_12': 'tutkintotyyppikoodi-3'
    };

    var alustaKonteksti = function() {
      // Jos ollaan ammatillisen peruskoulutuksen kontekstissa, niin tutkintotyypiksi asetetaan perustutkinto
      // ja tyhjennetään opintoalan valinta
      if ($scope.konteksti === $scope.kontekstit[0]) {
        $scope.hakuParametrit.tutkintotyypit[konteksti] = 'koulutustyyppi_1';
        $scope.hakuParametrit.opintoala = '';
      } else {
        //$scope.hakuParametrit.tutkintotyypit[$scope.konteksti] = Haku.hakuParametrit.tutkintotyypit[$scope.konteksti];
      }
    };

    alustaKonteksti();
    // TODO Päivitä navipolku

    $scope.tyhjenna = function() {
      $scope.query = null;
      $scope.koulutusala = '';
      $scope.valittuOpintoala = '';
      $scope.siirtymaAjalla = null;
      alustaKonteksti();
      $scope.nykyinenSivu = 0;
      $scope.haePerusteet(0);
    };
    var hakuVastaus = function(vastaus) {
      console.log('vastaus', vastaus);
      $scope.perusteet = vastaus;
      $scope.nykyinenSivu = $scope.perusteet.sivu;
      $scope.hakuParametrit.sivukoko = $scope.perusteet.sivukoko;
      $scope.sivuja = $scope.perusteet.sivuja;
      $scope.kokonaismaara = $scope.perusteet.kokonaismäärä;
      $scope.sivut = _.range(0, $scope.perusteet.sivuja);
      pat = new RegExp('(' + $scope.query + ')', 'i');
    };
    $scope.haePerusteet = function(sivu) {

      $scope.hakuParametrit.sivu = sivu;
      $scope.hakuParametrit.tyyppi = $scope.hakuParametrit.tutkintotyypit[$scope.konteksti];
      Haku.hakuParametrit = $scope.hakuParametrit;
      
      Perusteet.query(Haku.hakuParametrit, hakuVastaus, function(virhe) {
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
      if ($scope.hakuParametrit.nimi === null || $scope.hakuParametrit.nimi.length < 3) {
        return otsikko;
      }
      return otsikko.replace(pat, '<b>$1</b>');
    };
    $scope.valitseKieli = function(nimi) {
      return YleinenData.valitseKieli(nimi);
    };

    //$scope.haePerusteet($scope.nykyinenSivu);

    $rootScope.$on('$translateChangeSuccess', function() {
      console.log('translate');
      $scope.tyhjenna();
    });

    $scope.koulutusalaMuuttui = function() {

      if ($scope.hakuParametrit.koulutusala !== '') {
        $scope.opintoalat = _.findWhere($scope.koulutusalat, {koodi: $scope.hakuParametrit.koulutusala}).opintoalat;
      } else {
        $scope.opintoalat = [];
      }
      $scope.hakuParametrit.opintoala = '';
      $scope.hakuMuuttui();
    };

    $scope.koulutusalaNimi = function(koodi) {
      return koulutusalaService.haeKoulutusalaNimi(koodi);
    };

    var opintoalaTemp = $scope.valittuOpintoala;
    $scope.koulutusalaMuuttui();
    $scope.hakuParametrit.opintoala = opintoalaTemp;
  });
