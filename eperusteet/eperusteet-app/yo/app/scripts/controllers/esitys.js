'use strict';
/* global _ */

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('esitys', {
        url: '/esitys',
        template: '<div ui-view></div>',
      })
      .state('esitys.peruste', {
        url: '/:perusteenId',
        templateUrl: '/views/esitys.html',
        controller: 'EsitysCtrl',
        naviRest: [':perusteenId']
        // navigaationimiId: 'peruste',
        //Estää sisällysluettelossa navigoinnin lataamasta sivua uudelleen
        // reloadOnSearch: false
      });
  })
  .controller('EsitysCtrl', function($q, $scope, $rootScope, $location, $anchorScroll,
    $stateParams, Kayttajaprofiilit, Suosikit, Perusteet, Suosikitbroadcast,
    YleinenData, Navigaatiopolku, palvelinhaunIlmoitusKanava) {

    $scope.konteksti = $stateParams.konteksti;
    $scope.perusteValinta = {};
    $scope.syvyys = 2;
    $scope.suosikkiLista = {};
    //$scope.suosikkiPeruste = true;
    var eiSuosikkiTyyli = 'glyphicon glyphicon-star-empty pointer';
    var suosikkiTyyli = 'glyphicon glyphicon-star pointer';
    $scope.suosikkiTyyli = eiSuosikkiTyyli;

    $scope.tallennaRakenne = function(rakenne) {
      console.log(rakenne);
    };

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

    var perusteHakuPromise = (function() {
      if ($stateParams.perusteenId) {
        return Perusteet.get({perusteenId: $stateParams.perusteenId}).$promise;
      } else {
        return $q.reject();
      }
    }());

    var kayttajaProfiiliPromise = Kayttajaprofiilit.get({}).$promise;
    perusteHakuPromise.then(function(peruste) {
      if (peruste.id) {
        $scope.perusteValinta = peruste;
        Navigaatiopolku.asetaElementit({ perusteenId: peruste.nimi });
      } else {
        // TODO perustetta ei löytynyt, virhesivu.
      }
    }, function(error) {
      console.log(error);
      // TODO
      //Virhe tapahtui, esim. perustetta ei löytynyt. Virhesivu.
      // $location.path('/selaus/' + $scope.konteksti);
    });

    kayttajaProfiiliPromise.then(function(profiili) {
      $scope.suosikkiLista = profiili.suosikit;
      $scope.suosikkiTyyli = $scope.onSuosikki();
    }, function() {
      console.log('profiilia ei löytynyt');
      $scope.suosikkiLista = [];
      $scope.suosikkiTyyli = $scope.onSuosikki();
    });

    $scope.onSuosikki = function() {
      for (var i = 0; i < _.size($scope.suosikkiLista); i++) {
        if ($scope.suosikkiLista[i].id === $scope.perusteValinta.id) {
          return suosikkiTyyli;
        }
      }
      return eiSuosikkiTyyli;
    };

    $scope.asetaSuosikiksi = function() {
      if ($scope.suosikkiTyyli === eiSuosikkiTyyli) {

        Suosikit.save({suosikkiId: $scope.perusteValinta.id}, {}, function(vastaus) {
          $scope.suosikkiLista = vastaus.suosikit;
          $scope.suosikkiTyyli = $scope.onSuosikki();
          Suosikitbroadcast.suosikitMuuttuivat();
        });

      } else {

        Suosikit.delete({suosikkiId: $scope.perusteValinta.id}, {}, function(vastaus) {
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

    $scope.rakenne = {
      otsikko: {
        fi: 'Joku perustutkinto'
      },
      laajuus: {
        tyyppi: 'ov',
        maara: 120
      },
      osat: [{
        otsikko: {
          fi: 'Jotkut tutkinnon osat'
        },
        laajuus: {
          tyyppi: 'ov',
          maara: 90
        },
        osat: [{
          otsikko: {
            fi: 'Perus elektroniikka'
          },
          laajuus: {
            tyyppi: 'ov',
            maara: 30
          },
          tutkinnonosa: 1
        }, {
          tyyppi: 'yksi',
          rajoite: 'laajuus',
          osat: [{
            otsikko: {
              fi: 'Sulautetut sovellukset ja projektityöt',
            },
            laajuus: {
              tyyppi: 'ov',
              maara: 20
            },
            tutkinnonosa: 2
          }, {
            otsikko: {
              fi: 'Elektroniikkatuotanto'
            },
            laajuus: {
              tyyppi: 'ov',
              maara: 20
            },
            tutkinnonosa: 3
          }]
        }]
      }]
    };
  });
