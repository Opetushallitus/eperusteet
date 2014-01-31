'use strict';

angular.module('eperusteApp')
  .config(function($routeProvider) {
    $routeProvider
      .when('/selaus/:konteksti/:perusteId', {
        templateUrl: 'views/esitys.html',
        controller: 'EsitysCtrl',
        //Estää sisällysluettelossa navigoinnin lataamasta sivua uudelleen
        reloadOnSearch: false
      });
  })
  .controller('EsitysCtrl', function($q, $scope, $location, $anchorScroll, $routeParams,
    Kayttajaprofiilit, Suosikit, Perusteet, Suosikitbroadcast, YleinenData, palvelinhaunIlmoitusKanava) {

    $scope.perusteValinta = {};
    $scope.syvyys = 2;
    $scope.suosikkiLista = {};
    //$scope.suosikkiPeruste = true;
    var eiSuosikkiTyyli = 'glyphicon glyphicon-star-empty pointer';
    var suosikkiTyyli = 'glyphicon glyphicon-star pointer';
    $scope.suosikkiTyyli = eiSuosikkiTyyli;
    $scope.kontekstit = YleinenData.kontekstit;

    $scope.terveydentilaOptiot = [
      {teksti: 'Kaikki', valittu: true},
      {teksti: 'Terveydentila optio 1', valittu: false},
      {teksti: 'Terveydentila optio 2', valittu: false},
      {teksti: 'Terveydentila optio 3', valittu: false},
      {teksti: 'Terveydentila optio 4', valittu: false}
    ];

    $scope.todistuksetOptiot = [
      {teksti: 'Kaikki', valittu: true},
      {teksti: 'Todistukset optio 1', valittu: false},
      {teksti: 'Todistukset optio 2', valittu: false},
      {teksti: 'Todistukset optio 3', valittu: false}
    ];

    $scope.arviointiOptiot = [
      {teksti: 'Kaikki', valittu: true},
      {teksti: 'Oppilaan arviointi oppiaineessa', valittu: false},
      {teksti: 'Oppiaineen hyvän edistymisen kuvaus', valittu: false},
      {teksti: 'Oppiaineen hyvän osaamisen kuvaus', valittu: false},
      {teksti: 'Oppiaineen päätösarvioinnin kriteerit arvosanalle 8', valittu: false},
      {teksti: 'Todistukset', valittu: false},
      {teksti: 'Erityisen tutkinnon suoritusten arviointi ja muutokset', valittu: false}
    ];

    $scope.maarayksetOptiot = [
      {teksti: 'Kaikki', valittu: true},
      {teksti: 'Määräykset optio 1', valittu: false},
      {teksti: 'Määräykset optio 2', valittu: false},
      {teksti: 'Määräykset optio 3', valittu: false},
      {teksti: 'Määräykset optio 4', valittu: false},
      {teksti: 'Määräykset optio 5', valittu: false}
    ];

    var hakuAloitettuKäsittelijä = function() {
      $scope.hakuMenossa = true;
    };

    var hakuLopetettuKäsittelijä = function() {
      $scope.hakuMenossa = false;
    };
    palvelinhaunIlmoitusKanava.kunHakuAloitettu($scope, hakuAloitettuKäsittelijä);
    palvelinhaunIlmoitusKanava.kunHakuLopetettu($scope, hakuLopetettuKäsittelijä);



    if ($routeParams.konteksti && $scope.kontekstit.indexOf($routeParams.konteksti.toLowerCase()) !== -1) {
      $scope.konteksti = $routeParams.konteksti;
    } else {
      $location.path('/selaus/ammatillinenperuskoulutus');
    }

    var perusteHakuPromise = (function() {
      if ($routeParams.perusteId) {
        return Perusteet.get({perusteenId: $routeParams.perusteId});
      } else {
        return undefined;
      }
    }());

    var kayttajaProfiiliPromise = Kayttajaprofiilit.get({id: 1});

    $q.all([perusteHakuPromise.$promise, kayttajaProfiiliPromise.$promise]).then(function(vastaus) {

      var peruste = vastaus[0];
      if (peruste.id) {
        $scope.perusteValinta = peruste;
      } else {
        // perustetta ei löytynyt, virhesivu.
      }

      var profiili = vastaus[1];
      $scope.suosikkiLista = profiili.suosikit;
      $scope.suosikkiTyyli = $scope.onSuosikki();


    }, function(/*virhe*/) {
      //Virhe tapahtui, esim. perustetta ei löytynyt. Virhesivu.
      $location.path('/selaus/' + $scope.konteksti);
    });


    $scope.onSuosikki = function() {
      for (var i = 0; i < $scope.suosikkiLista.length; i++) {
        if ($scope.suosikkiLista[i].id === $scope.perusteValinta.id) {
          return suosikkiTyyli;
        }
      }
      return eiSuosikkiTyyli;
    };

    $scope.vierity = function(id) {
      $location.hash(id);
      $anchorScroll();
    };

    $scope.asetaSuosikiksi = function() {
      if ($scope.suosikkiTyyli === eiSuosikkiTyyli) {

        Suosikit.save({id: 1, suosikkiId: $scope.perusteValinta.id}, {}, function(vastaus) {
          $scope.suosikkiLista = vastaus.suosikit;
          $scope.suosikkiTyyli = $scope.onSuosikki();
          Suosikitbroadcast.suosikitMuuttuivat();
        });

      } else {

        Suosikit.delete({id: 1, suosikkiId: $scope.perusteValinta.id}, {}, function(vastaus) {
          $scope.suosikkiLista = vastaus.suosikit;
          $scope.suosikkiTyyli = $scope.onSuosikki();
          Suosikitbroadcast.suosikitMuuttuivat();
        });
      }

    };

    $scope.valitseKieli = function(teksti) {
      return YleinenData.valitseKieli(teksti);
    };

    $scope.$on('optioPoistettu', function() {
      $scope.$broadcast('optiotMuuttuneet');
    });


  });
