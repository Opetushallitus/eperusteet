'use strict';
/*global _*/

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('selaus', {
        url: '/selaus',
        template: '<div ui-view></div>',
        // controller: 'HakuCtrl',
        // navigaationimi: 'navi-hakuehdot',
        // resolve: {'koulutusalaService': 'Koulutusalat'}
      })
      .state('selaus.konteksti', {
        url: '/:konteksti',
        templateUrl: 'views/haku.html',
        controller: 'HakuCtrl',
        navigaationimi: 'navi-hakuehdot',
        resolve: {'koulutusalaService': 'Koulutusalat'}
      });
  })
  .controller('HakuCtrl', function($scope, $rootScope, $window, $state, $stateParams,
      Perusteet, Haku, YleinenData, koulutusalaService) {

    var pat = '';
    // Viive, joka odotetaan, ennen kuin haku nimi muutoksesta lähtee serverille.
    var hakuViive = 300; //ms
    $scope.nykyinenSivu = Haku.hakuParametrit.sivu;
    $scope.sivukoko = 20; //$window.innerHeight > 500 ? 25 : 15;
    $scope.sivuja = 1;
    $scope.kokonaismaara = 0;
    $scope.query = Haku.hakuParametrit.nimi;
    $scope.koulutusala = Haku.hakuParametrit.koulutusala;
    $scope.tutkintotyyppi = Haku.hakuParametrit.tyyppi;
    $scope.siirtymaAjalla = Haku.hakuParametrit.siirtyma;
    $scope.valittuOpintoala = Haku.hakuParametrit.opintoala;
    $scope.konteksti = $stateParams.konteksti;
    $scope.kontekstit = YleinenData.kontekstit;
    $scope.kieli = YleinenData.kieli;
    $scope.koulutusalat = koulutusalaService.haeKoulutusalat();

    $scope.tutkintotyypit = {
      'koulutustyyppi_1': 'tutkintotyyppikoodi-1',
      'koulutustyyppi_11': 'tutkintotyyppikoodi-2',
      'koulutustyyppi_12': 'tutkintotyyppikoodi-3'
    };

    var alustaKonteksti = function() {
      // Jos ollaan ammatillisen peruskoulutuksen kontekstissa, niin tutkintotyypiksi asetetaan perustutkinto
      // ja tyhjennetään opintoalan valinta
      if ($scope.konteksti === $scope.kontekstit[0]) {
        //$scope.tutkintotyyppi = 1;
        $scope.tutkintotyyppi = 'koulutustyyppi_1';
        $scope.valittuOpintoala = '';
      } else {
        $scope.tutkintotyyppi = '';
      }
    };

    if ($stateParams.konteksti && $scope.kontekstit.indexOf($stateParams.konteksti.toLowerCase()) !== -1) {
      $scope.konteksti = $stateParams.konteksti;
      alustaKonteksti();
      $rootScope.$broadcast('paivitaNavigaatiopolku');
    } else {
      $state.go('selaus.konteksti', { konteksti: 'ammatillinenperuskoulutus' });
    }

    $scope.tyhjenna = function() {
      $scope.query = null;
      $scope.koulutusala = '';
      $scope.valittuOpintoala = '';
      alustaKonteksti();
      $scope.nykyinenSivu = 0;
      $scope.haePerusteet(0);
    };
    var hakuVastaus = function(vastaus) {
      console.log('haku', vastaus);
      $scope.perusteet = vastaus;
      $scope.nykyinenSivu = $scope.perusteet.sivu;
      $scope.sivukoko = $scope.perusteet.sivukoko;
      $scope.sivuja = $scope.perusteet.sivuja;
      $scope.kokonaismaara = $scope.perusteet.kokonaismäärä;
      $scope.sivut = _.range(0, $scope.perusteet.sivuja);
      pat = new RegExp('(' + $scope.query + ')', 'i');
    };
    $scope.haePerusteet = function(sivu) {

      Haku.hakuParametrit = {
        sivu: sivu,
        nimi: $scope.query,
        koulutusala: $scope.koulutusala,
        opintoala: $scope.valittuOpintoala,
        sivukoko: $scope.sivukoko,
        tyyppi: $scope.tutkintotyyppi,
        kieli: YleinenData.kieli,
        siirtyma: $scope.siirtymaAjalla
      };
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
      if ($scope.query === null || $scope.query.length < 3) {
        return otsikko;
      }
      return otsikko.replace(pat, '<b>$1</b>');
    };
    $scope.valitseKieli = function(nimi) {
      return YleinenData.valitseKieli(nimi);
    };

    //$scope.haePerusteet($scope.nykyinenSivu);

    $scope.$on('$translateChangeSuccess', function() {
      $scope.tyhjenna();
      $scope.haePerusteet(0);
    });

    $scope.koulutusalaMuuttui = function() {

      if ($scope.koulutusala !== '') {
        $scope.opintoalat = _.findWhere($scope.koulutusalat, {koodi: $scope.koulutusala}).opintoalat;
      } else {
        $scope.opintoalat = [];
      }
      $scope.valittuOpintoala = '';
      $scope.hakuMuuttui();
    };

    $scope.koulutusalaNimi = function(koodi) {
      return koulutusalaService.haeKoulutusalaNimi(koodi);
    };

    var opintoalaTemp = $scope.valittuOpintoala;
    $scope.koulutusalaMuuttui();
    $scope.valittuOpintoala = opintoalaTemp;
  });
