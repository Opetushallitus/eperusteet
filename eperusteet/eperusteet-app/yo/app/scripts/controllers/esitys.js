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
        templateUrl: 'views/esitys.html',
        controller: 'EsitysCtrl',
        naviRest: [':perusteenId']
      });
  })
  .controller('EsitysCtrl', function($q, $scope, $stateParams, $state,
    Kayttajaprofiilit, Suosikit, Perusteet, Suoritustapa, Suosikitbroadcast, YleinenData,
    Navigaatiopolku, palvelinhaunIlmoitusKanava) {

    $scope.konteksti = $stateParams.konteksti;
    $scope.peruste = {};
    $scope.syvyys = 2;
    $scope.suosikkiLista = {};
    var eiSuosikkiTyyli = 'glyphicon glyphicon-star-empty pointer';
    var suosikkiTyyli = 'glyphicon glyphicon-star pointer';
    $scope.suosikkiTyyli = eiSuosikkiTyyli;
    $scope.suoritustapa = $stateParams.suoritustapa;
    var suosikkiId = null;
    $scope.suodatin = {};


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
        console.log('peruste', peruste);
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
        $scope.peruste.rakenne = vastaus;
        $scope.suodatin.otsikot = _.pluck(_.pluck(vastaus.lapset, 'perusteenOsa'), 'nimi');
      }, function (virhe) {
          console.log('suoritustapasisältöä ei löytynyt', virhe);
        });
    };

    $scope.onSuosikki = function() {
      for (var i = 0; i < _.size($scope.suosikkiLista); i++) {
        if ($scope.suosikkiLista[i].perusteId === $scope.peruste.id && $scope.suosikkiLista[i].suoritustapakoodi === $scope.suoritustapa) {
          suosikkiId = $scope.suosikkiLista[i].id;
          return suosikkiTyyli;
        }
      }
      suosikkiId = null;
      return eiSuosikkiTyyli;
    };

    $scope.asetaSuosikiksi = function() {
      if ($scope.suosikkiTyyli === eiSuosikkiTyyli) {

        Suosikit.save({}, {perusteId: $scope.peruste.id, suoritustapakoodi: $scope.suoritustapa}, function(vastaus) {
          $scope.suosikkiLista = vastaus.suosikit;
          $scope.suosikkiTyyli = $scope.onSuosikki();
          Suosikitbroadcast.suosikitMuuttuivat();
        });

      } else {
        Suosikit.delete({suosikkiId: suosikkiId}, {}, function(vastaus) {
          $scope.suosikkiLista = vastaus.suosikit;
          $scope.suosikkiTyyli = $scope.onSuosikki();
          Suosikitbroadcast.suosikitMuuttuivat();
        });
      }
    };

    $scope.suodatinValittu = function(suodatinId) {
      var suodatinTmp = _.find($scope.suodatin.otsikot, function(suodatin) {
        return suodatin._id === suodatinId;
      });
      suodatinTmp.valittu = true;

      $scope.suodatinId = '';
    };

    $scope.poistaSuodatin = function (suodatin) {
      suodatin.valittu = false;
    };

    $scope.onkoSuodatettu = function (id) {
      var valitutSuodattimet = _.filter($scope.suodatin.otsikot, 'valittu');
      return valitutSuodattimet.length === 0 || _.isObject(_.find(valitutSuodattimet, function(suodatin) {return suodatin._id === id;}));
    };

    $scope.valitseKieli = function(teksti) {
      return YleinenData.valitseKieli(teksti);
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

    /****************************************
     *
     * Kovakoodattu rakenne-esitys, plsremovewhenfit!!
     *
     **************************************/
    $scope.rakenne = {
      otsikko: 'Tieto- ja tietoliikennealan perustutkinto',
      laajuus: '120 ov',
      osat: [
        {
          otsikko: 'Ammatilliset tutkinnon osat',
          kuvaus: 'Tutkinnon osiin sisältyy työssäoppimista vähintään 20 ov, yrittäjyyttä vähintään 5 ov ja opinnäyte vähintään 2 ov',
          laajuus: '90 ov',
          osat: [
            {
              otsikko: 'Kaikille pakolliset tutkinnon osat',
              osat: [
                {
                  otsikko: 'Elektroniikan ja ICT:n perustehtävät',
                  laajuus: '30 ov',
                  tutkinnonosa: 1
                }
              ]
            },
            {
              tyyppi: 'yksi',
              osat: [
                {
                  otsikko: 'Tieto- ja tietoliikennetekniikan koulutusohjelma, elektroniikka-asentaja',
                  osat: [
                    {
                      otsikko: 'Ammattielektroniikka',
                      laajuus: '20 ov',
                      tutkinnonosa: 1
                    },
                    {
                      tyyppi: 'yksi',
                      osat: [
                        {
                          otsikko: 'Sulautetut sovellukset ja projektityöt',
                          laajuus: '20 ov',
                          tutkinnonosa: 1
                        },
                        {
                          otsikko: 'Elektroniikkatuotanto',
                          laajuus: '20 ov',
                          tutkinnonosa: 1
                        }
                      ]
                    }
                  ]
                },
                {
                  otsikko: 'Tieto- ja tietoliikennetekniikan koulutusohjelma, ICT-asentaja',
                  osat: [
                    {
                      otsikko: 'Tietokone- ja tietoliikenneasennukset',
                      laajuus: '20 ov',
                      tutkinnonosa: 1
                    },
                    {
                      tyyppi: 'yksi',
                      osat: [
                        {
                          otsikko: 'Palvelinjärjestelmät ja projektityöt',
                          laajuus: '20 ov',
                          tutkinnonosa: 1
                        },
                        {
                          otsikko: 'Tietoliikennelaiteasennukset ja kaapelointi',
                          laajuus: '20 ov',
                          tutkinnonosa: 1
                        },
                      ]
                    }
                  ]
                }
              ]
            },
            {
              otsikko: 'Seuraavista kohdista tutkinnon osia yhteensä 20ov',
              osat: [
                {
                  otsikko: 'Kaikille valinnaiset tutkinnon osat',
                  laajuus: '10-20 ov',
                  osat: [
                      {
                          otsikko: 'Huoltopalvelut',
                          laajuus: '10 ov'
                      },
                      {
                          otsikko: 'Valvonta ja ilmoitusjärjestelmäasennukset',
                          laajuus: '10 ov'
                      },
                      {
                          otsikko: 'Kodin elektroniikka ja asennukset',
                          laajuus: '10 ov'
                      },
                      {
                          otsikko: 'RF-työt',
                          laajuus: '10 ov'
                      },
                      {
                          otsikko: 'Sähköasennukset',
                          laajuus: '10 ov',
                      },
                      {
                          otsikko: 'Tutkinnon osa ammatillisesta perustutkinnosta',
                          laajuus: '0-20 ov'
                      },
                      {
                          otsikko: 'Tutkinnon osa ammattitutkinnosta'
                      },
                      {
                          otsikko: 'Tutkinnon osa erikoisammattitutkinnosta'
                      },
                      {
                          otsikko: 'Paikallisesti tarjottava tutkinnon osa',
                          laajuus: '0-20 ov'
                      }
                  ]
                },
                {
                  otsikko: 'Muut valinnaiset tutkinnon osat ammatillisessa peruskoulutuksessa',
                  laajuus: '0-10 ov',
                  osat: [
                      {
                          otsikko: 'Yrittäjyys',
                          laajuus: '10 ov'
                      },
                      {
                          otsikko: 'Työpaikkaohjaajaksi valmentautuminen',
                          laajuus: '2 ov'
                      },
                      {
                          otsikko: 'Ammattitaitoa täydentävät tutkinnon osat (yhteiset opinnot)'
                      },
                      {
                          otsikko: 'Lukio-opinnot'
                      }
                  ]
                }
              ]
            }
          ]

        },
        {
          otsikko: 'Ammattitaitoa täydentävät tutkinnon osat ammatillisessa peruskoulutuksessa (yhteiset opinnot)',
          laajuus: '20 ov',
          osat: [
            {
              tyyppi: 'selite',
              otsikko: ['Opetuskieleltään ruotsinkielisessä koulutuksessa toisen kotimaisen kielen opintojen laajuus on 2 ov, jolloin pakollisten ammattitaitoa täydentävien tutkinnon osien laajuus on 17 ov ja valinnaisten 3 ov.', 'Liikunnan pakollisten opintojen laajuus on 1 ov ja terveystiedon pakollisten opintojen laajuus on 1 ov. Koulutuksen järjestäjä voi päättää liikunnan ja terveystiedon pakollisten opintojen jakamisesta poikkea- valla tavalla kuitenkin siten, että niiden yhteislaajuus on kaksi opintoviikkoa.']
            },
            {
              otsikko: 'Pakolliset tutkinnon osat',
              osat: [
                {
                  otsikko: 'Äidinkieli',
                  laajuus: '4 ov'
                },
                {
                  otsikko: 'Toinen kotimainen kieli',
                  tyyppi: 'yksi',
                  osat: [
                    {
                      otsikko: 'Toinen kotimainen kieli ruotsi',
                      laajuus: '1ov'
                    },
                    {
                      otsikko: 'Toinen kotimainen kieli suomi',
                      laajuus: '2ov'
                    }
                  ]
                },
                {
                  otsikko: 'Vieras kieli',
                  laajuus: '2 ov'
                },
                {
                  otsikko: 'Matematiikka',
                  laajuus: '3 ov'
                },
                {
                  otsikko: 'Fysiikka ja kemia',
                  laajuus: '2 ov'
                },
                {
                  otsikko: 'Yhteiskunta-, yritys- ja työelämätieto',
                  laajuus: '1 ov'
                },
                {
                  otsikko: 'Liikunta',
                  laajuus: '1 ov'
                },
                {
                  otsikko: 'Terveystieto',
                  laajuus: '1 ov'
                },
                {
                  otsikko: 'Taide ja kulttuuri',
                  laajuus: '1 ov'
                }
              ]
            },
            {
              otsikko: 'Valinnaiset tutkinnon osat',
              osat: [
                {
                  otsikko: 'Äidinkieli',
                  laajuus: '0-4 ov'
                },
                {
                  otsikko: 'Vieras kieli',
                  laajuus: '0-4 ov'
                },
                {
                  otsikko: 'Matematiikka',
                  laajuus: '0-4 ov'
                },
                {
                  otsikko: 'Fysiikka ja kemia',
                  laajuus: '0-4 ov'
                },
                {
                  otsikko: 'Yhteiskunta-, yritys- ja työelämätieto',
                  laajuus: '0-4 ov'
                },
                {
                  otsikko: 'Liikunta',
                  laajuus: '0-4 ov'
                },
                {
                  otsikko: 'Terveystieto',
                  laajuus: '0-4 ov'
                },
                {
                  otsikko: 'Taide ja kulttuuri',
                  laajuus: '0-4 ov'
                },
                {
                  otsikko: 'Ympäristötieto',
                  laajuus: '0-4 ov'
                },
                {
                  otsikko: 'Tieto- ja viestintätekniikka',
                  laajuus: '0-4 ov'
                },
                {
                  otsikko: 'Etiikka',
                  laajuus: '0-4 ov'
                },
                {
                  otsikko: 'Kulttuurien tuntemus',
                  laajuus: '0-4 ov'
                },
                {
                  otsikko: 'Psykologia',
                  laajuus: '0-4 ov'
                },
                {
                  otsikko: 'Yritystoiminta',
                  laajuus: '0-4 ov'
                }
              ]
            }
          ]



        },
        {
          otsikko: 'Vapaasti valittavat tutkinnon osat ammatillisessa peruskoulutuksessa',
          laajuus: '10 ov'
        },

      ]
    };

  });
