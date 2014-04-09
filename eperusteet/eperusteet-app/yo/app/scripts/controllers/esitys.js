'use strict';
/* global _ */

angular.module('eperusteApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('esitys', {
        url: '/esitys',
        template: '<div ui-view></div>'
      })
      .state('esitys.peruste', {
        url: '/:perusteenId/:suoritustapa',
        templateUrl: '/views/esitys.html',
        controller: 'EsitysCtrl',
        naviRest: [':perusteenId']
      });
  })
  .controller('EsitysCtrl', function($q, $scope, $stateParams, Kayttajaprofiilit, Suosikit,
      Perusteet, Suosikitbroadcast, Suoritustapa, YleinenData, Navigaatiopolku,
      palvelinhaunIlmoitusKanava, PerusteRakenteet, TreeCache) {

    $scope.konteksti = $stateParams.konteksti;
    $scope.peruste = {};
    $scope.syvyys = 2;
    $scope.suosikkiLista = {};

    $scope.rakenne = {};
    if (TreeCache.nykyinen() !== $stateParams.perusteenId) {
      PerusteRakenteet.get({ perusteenId: $stateParams.perusteenId }, function(re) {
        $scope.rakenne = re;
        $scope.rakenne.tutkinnonOsat = _.zipObject(_.pluck($scope.rakenne.tutkinnonOsat, '_tutkinnonOsa'), $scope.rakenne.tutkinnonOsat);
      });
    } else {
      TreeCache.hae();
    }

    $scope.tallennaRakenne = function(rakenne) {
      TreeCache.tallenna(rakenne, $stateParams.perusteenId);
    };

    var eiSuosikkiTyyli = 'glyphicon glyphicon-star-empty pointer';
    var suosikkiTyyli = 'glyphicon glyphicon-star pointer';
    $scope.suosikkiTyyli = eiSuosikkiTyyli;
    $scope.suoritustapa = $stateParams.suoritustapa;

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
        $scope.peruste = peruste;
        Navigaatiopolku.asetaElementit({ perusteenId: peruste.nimi });
        haeSuoritustapaSisalto(peruste.id);
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

    var haeSuoritustapaSisalto = function (id) {
      Suoritustapa.get({perusteenId: id, suoritustapa: $scope.suoritustapa}, function(vastaus) {
        console.log('suoritustapa vastaus', vastaus);
        $scope.peruste.rakenne = vastaus;
        console.log($scope.peruste);
      }, function (virhe) {
          // console.log('suoritustapasisältöä ei löytynyt', virhe);
      });
    };

    $scope.onSuosikki = function() {
      for (var i = 0; i < _.size($scope.suosikkiLista); i++) {
        if ($scope.suosikkiLista[i].id === $scope.peruste.id) {
          return suosikkiTyyli;
        }
      }
      return eiSuosikkiTyyli;
    };

    $scope.asetaSuosikiksi = function() {
      if ($scope.suosikkiTyyli === eiSuosikkiTyyli) {

        Suosikit.save({suosikkiId: $scope.peruste.id}, {}, function(vastaus) {
          $scope.suosikkiLista = vastaus.suosikit;
          $scope.suosikkiTyyli = $scope.onSuosikki();
          Suosikitbroadcast.suosikitMuuttuivat();
        });

      } else {

        Suosikit.delete({suosikkiId: $scope.peruste.id}, {}, function(vastaus) {
          $scope.suosikkiLista = vastaus.suosikit;
          $scope.suosikkiTyyli = $scope.onSuosikki();
          Suosikitbroadcast.suosikitMuuttuivat();
        });
      }
    };

    $scope.$on('optioPoistettu', function() {
      $scope.$broadcast('optiotMuuttuneet');
    });

    $scope.vaihdaSuoritustapa = function(suoritustapa) {
      $state.go('esitys.peruste', {perusteenId: $stateParams.perusteenId, suoritustapa: suoritustapa});
    };


    var hakuAloitettuKäsittelijä = function() {
      $scope.hakuMenossa = true;
    };

    var hakuLopetettuKäsittelijä = function() {
      $scope.hakuMenossa = false;
    };
    palvelinhaunIlmoitusKanava.kunHakuAloitettu($scope, hakuAloitettuKäsittelijä);
    palvelinhaunIlmoitusKanava.kunHakuLopetettu($scope, hakuLopetettuKäsittelijä);

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
  });
