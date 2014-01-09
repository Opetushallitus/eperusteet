'use strict';
/*global _*/

angular.module('eperusteApp')
  .controller('HakuCtrl', function($scope, $window, $routeParams, $location, Perusteet,
    Haku, YleinenData, Koulutusalat) {

    var pat = '';
    // Viive, joka odotetaan, ennen kuin haku nimi muutoksesta lähtee serverille.
    var hakuViive = 300; //ms
    $scope.nykyinenSivu = Haku.hakuParametrit.sivu;
    $scope.sivukoko = 25; //$window.innerHeight > 500 ? 25 : 15;
    $scope.sivuja = 1;
    $scope.kokonaismaara = 0;
    $scope.query = Haku.hakuParametrit.nimi;
    $scope.koulutusala = Haku.hakuParametrit.ala;
    $scope.tutkintotyyppi = Haku.hakuParametrit.tyyppi;
    $scope.kontekstit = YleinenData.kontekstit;
    $scope.kieli = YleinenData.kieli;
    $scope.koulutusalat = Koulutusalat.query();
    $scope.opintoalat = [];
    $scope.valittuOpintoala = Haku.hakuParametrit.opintoala;


    $scope.tutkintotyypit = {
      1: 'tutkintotyyppikoodi-1',
      2: 'tutkintotyyppikoodi-2',
      3: 'tutkintotyyppikoodi-3'
    };
    $scope.koulutusalakoodit = {
      1: 'koulutusalakoodi-1',
      2: 'koulutusalakoodi-2',
      3: 'koulutusalakoodi-3',
      4: 'koulutusalakoodi-4',
      5: 'koulutusalakoodi-5',
      6: 'koulutusalakoodi-6',
      7: 'koulutusalakoodi-7',
      8: 'koulutusalakoodi-8'
    };
    $scope.opintoalakoodit = {
      1: 'opintoalakoodi-1',
      2: 'opintoalakoodi-2',
      3: 'opintoalakoodi-3',
      4: 'opintoalakoodi-4',
      5: 'opintoalakoodi-5',
      6: 'opintoalakoodi-6',
      7: 'opintoalakoodi-7',
      8: 'opintoalakoodi-8',
      9: 'opintoalakoodi-9',
      10: 'opintoalakoodi-10',
      11: 'opintoalakoodi-11',
      12: 'opintoalakoodi-12',
      13: 'opintoalakoodi-13',
      14: 'opintoalakoodi-14',
      15: 'opintoalakoodi-15',
      16: 'opintoalakoodi-16',
      17: 'opintoalakoodi-17',
      18: 'opintoalakoodi-18',
      19: 'opintoalakoodi-19',
      20: 'opintoalakoodi-20',
      21: 'opintoalakoodi-21',
      22: 'opintoalakoodi-22',
      23: 'opintoalakoodi-23',
      24: 'opintoalakoodi-24',
      25: 'opintoalakoodi-25',
      26: 'opintoalakoodi-26',
      27: 'opintoalakoodi-27',
      28: 'opintoalakoodi-28',
      29: 'opintoalakoodi-29',
      30: 'opintoalakoodi-30',
      31: 'opintoalakoodi-31',
      32: 'opintoalakoodi-32',
      33: 'opintoalakoodi-33',
      34: 'opintoalakoodi-34',
      35: 'opintoalakoodi-35',
      36: 'opintoalakoodi-36',
      37: 'opintoalakoodi-37',
      38: 'opintoalakoodi-38'
    };
    var alustaTutkintotyyppi = function() {
      // Jos ollaan ammatillisen peruskoulutuksen kontekstissa, niin tutkintotyypiksi asetetaan perustutkinto
      if ($scope.konteksti === $scope.kontekstit[0]) {
        $scope.tutkintotyyppi = 1;
      } else {
        $scope.tutkintotyyppi = '';
      }
    };

    if ($routeParams.konteksti && $scope.kontekstit.indexOf($routeParams.konteksti.toLowerCase()) !== -1) {
      $scope.konteksti = $routeParams.konteksti;
      alustaTutkintotyyppi();
    } else {
      $location.path('/selaus/ammatillinenperuskoulutus');
    }

    $scope.tyhjenna = function() {
      $scope.query = null;
      $scope.koulutusala = '';
      $scope.valittuOpintoala = '';
      alustaTutkintotyyppi();
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
        ala: $scope.koulutusala,
        opintoala: $scope.valittuOpintoala,
        sivukoko: $scope.sivukoko,
        tyyppi: $scope.tutkintotyyppi,
        kieli: YleinenData.kieli
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

      $scope.hakuMuuttui();
    };
  });
