'use strict';
/*global _*/

angular.module('eperusteApp')
  .config(function($routeProvider) {
    $routeProvider
      .when('/selaus/:konteksti', {
        templateUrl: 'views/haku.html',
        controller: 'HakuCtrl',
        navigaationimi: 'Hakuehdot'
      });
  })
  .controller('HakuCtrl', function($scope, $rootScope, $window, $routeParams, $location,
    Perusteet, Haku, YleinenData, Koulutusalat) {

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
    $scope.kontekstit = YleinenData.kontekstit;
    $scope.kieli = YleinenData.kieli;
    

    $scope.koulutusalat = [];
    Koulutusalat.query(
      function(vastaus) {
        $scope.koulutusalat = vastaus;
        // Jotta valittu opintoala säilyisi sivulle palatessa otetaan se talteen
        // ennen kuin kutsutaan koulutusalaMuuttui metodia.
        var opintoalaTemp = $scope.valittuOpintoala;
        $scope.koulutusalaMuuttui();
        $scope.valittuOpintoala = opintoalaTemp;
      }
    );

    $scope.tutkintotyypit = {
      1: 'tutkintotyyppikoodi-1',
      2: 'tutkintotyyppikoodi-2',
      3: 'tutkintotyyppikoodi-3'
    };

    var alustaKonteksti = function() {
      // Jos ollaan ammatillisen peruskoulutuksen kontekstissa, niin tutkintotyypiksi asetetaan perustutkinto
      // ja tyhjennetään opintoalan valinta
      if ($scope.konteksti === $scope.kontekstit[0]) {
        $scope.tutkintotyyppi = 1;
        $scope.valittuOpintoala = '';
      } else {
        $scope.tutkintotyyppi = '';
      }
    };

    if ($routeParams.konteksti && $scope.kontekstit.indexOf($routeParams.konteksti.toLowerCase()) !== -1) {
      $scope.konteksti = $routeParams.konteksti;
      alustaKonteksti();
      $rootScope.$broadcast('paivitaNavigaatiopolku');
    } else {
      $location.path('/selaus/ammatillinenperuskoulutus');
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
    $scope.haePerusteet($scope.nykyinenSivu);
    $scope.$on('kieliVaihtui', function() {
      $scope.tyhjenna();
      $scope.haePerusteet(0);
    });

    $scope.koulutusalaMuuttui = function() {
      if ($scope.koulutusala !== '') {
        $scope.opintoalat = $scope.koulutusalat[parseInt($scope.koulutusala, 10) - 1].opintoalat;
      } else {
        $scope.opintoalat = [];
      }
      $scope.valittuOpintoala = '';
      $scope.hakuMuuttui();
    };
  });
