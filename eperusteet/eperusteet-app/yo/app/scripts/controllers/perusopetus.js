/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

'use strict';
/*global _*/

angular.module('eperusteApp')
  .service('PerusopetusService', function (Vuosiluokkakokonaisuudet) {
    this.OSAAMINEN = 'osaaminen';
    this.VUOSILUOKAT = 'vuosiluokat';
    this.OPPIAINEET = 'oppiaineet';
    var tiedot = null;
    this.setTiedot = function (value) {
      console.log("setTiedot");
      tiedot = value;
    };
    this.getPerusteId = function () {
      return tiedot.getProjekti()._peruste;
    };

    this.sisallot = [
      {
        tyyppi: this.OSAAMINEN,
        label: 'laaja-alainen-osaaminen',
        emptyPlaceholder: 'tyhja-placeholder-osaaminen',
        addLabel: 'lisaa-osaamiskokonaisuus'
      },
      {
        tyyppi: this.VUOSILUOKAT,
        label: 'vuosiluokkakokonaisuudet',
        emptyPlaceholder: 'tyhja-placeholder-vuosiluokat',
        addLabel: 'lisaa-vuosiluokkakokonaisuus'
      },
      {
        tyyppi: this.OPPIAINEET,
        label: 'oppiaineet',
        emptyPlaceholder: 'tyhja-placeholder-oppiaineet',
        addLabel: 'lisaa-oppiaine'
      },
    ];

    this.getTekstikappaleet = function () {
      // TODO oikea data
      return [
          {
            perusteenOsa: {nimi: {fi: 'Paikallisen opetussuunnitelman merkitys ja laadinta'}},
            lapset: [
              {perusteenOsa: {nimi: {fi: 'Opetussuunnitelman perusteet ja paikallinen opetussuunnitelma'}}},
              {perusteenOsa: {nimi: {fi: 'Opetussuunnitelman laatimista ohjaavat periaatteet'}}},
            ]
          },
          {
            perusteenOsa: {nimi: {fi: 'Perusopetus yleissivistyksen perustana'}},
            lapset: [
              {perusteenOsa: {nimi: {fi: 'Opetuksen järjestämistä ohjaavat velvoitteet'}}},
              {perusteenOsa: {nimi: {fi: 'Perusopetuksen arvoperusta'}}},
            ]
          }
      ];
    };

    this.getOsat = function (tyyppi) {
      console.log("getOsat", tyyppi);
      // TODO oikea data
      switch(tyyppi) {
        case this.OSAAMINEN:
          return [
            {perusteenOsa: {id: 1, nimi: {fi: 'Ajattelu ja oppimaan oppiminen'}, teksti: {fi: 'Yleinen kuvaus ajattelulle.'}}},
            {perusteenOsa: {id: 2, nimi: {fi: 'Kulttuurinen osaaminen, vuorovaikutus ja ilmaisu'}, teksti: {fi: 'Yleinen kuvaus kulttuuriselle osaamiselle.'}}},
            {perusteenOsa: {id: 3, nimi: {fi: 'Itsestä huolehtiminen ja arjenhallinta'}, teksti: {fi: 'Yleinen kuvaus itsestä huolehtimiselle.'}}},
          ];
        case this.VUOSILUOKAT:
          Vuosiluokkakokonaisuudet.query({perusteId: tiedot.getProjekti()._peruste});
          return [
            {
              id: 1001,
              nimi: {fi: 'Vuosiluokat 1-2'},
              vuosiluokat: [1, 2],
              osaamisenkuvaukset: [
                {osaaminen: 1, teksti: {fi: 'Työskentelyn lähtökohtana ovat oppilaiden omat kokemukset ja kysymykset. Ihmettelylle, oivaltamiselle, uuden löytämiselle ja oppimisen ilolle jätetään tilaa. Oppilaita kannustetaan kysymiseen ja kuuntelemiseen sekä pienimuotoiseen tiedon etsintään, omien ideoiden tuottamiseen ja työn tulosten esittämiseen. Leikinomaisten ongelmanratkaisu- ja tutkimustehtävien avulla viritetään uteliaisuutta ja kiinnostusta ympäröivän maailman ilmiöitä kohtaan sekä vahvistetaan taitoa jäsentää, nimetä ja kuvailla ympäristöä. Oman ja yhteisen työn suunnittelemista, tavoitteiden asettamista ja työskentelyn arviointia harjoitellaan. Monipuolinen liikkuminen ja motoriset harjoitukset tukevat ajattelun kehittymistä. Muistin, mielikuvituksen sekä eettisen ja esteettisen ajattelun kehittymistä tuetaan satujen ja tarinoiden, pelien, lorujen, laulujen ja leikkien, taiteen eri muotojen sekä monipuolisen vuorovaikutuksen avulla. Oppilaiden kanssa pohditaan opintoihin liittyviä mahdollisia valintoja ja autetaan ymmärtämään erilaisten vaihtoehtojen merkitys.'}}
              ],
              tekstikappaleet: [
                {nimi: {fi: 'Vapaa tekstikappale'}, teksti: {fi: 'Vapaasisältöinen tekstikappale, tekstiä.'}},
                {nimi: {fi: 'Toinen tekstikappale'}, teksti: {fi: 'Lisää tekstikappaleen tekstiä.'}}
              ]
            },
            {
              id: 1000,
              nimi: {fi: 'Vuosiluokat 7-9'},
              vuosiluokat: [7, 8, 9],
              osaamisenkuvaukset: [
                {osaaminen: 1, teksti: {fi: 'Kuvaus osaamiskokonaisuudelle 1.'}}
              ],
              tekstikappaleet: [
                {nimi: {fi: 'Joku tekstikappale'}, teksti: {fi: 'Tekstikappaleen tekstiä.'}},
                {nimi: {fi: 'Toinen tekstikappale'}, teksti: {fi: 'Lisää tekstikappaleen tekstiä.'}}
              ]
            },
            {
              id: 1002,
              nimi: {fi: 'Vuosiluokat 3-6'},
              vuosiluokat: [3, 4, 5, 6],
              osaamisenkuvaukset: [],
              tekstikappaleet: []
            },

          ];
        case this.OPPIAINEET:
          return [
            {
              nimi: {fi: 'Matematiikka'},
              vuosiluokkakokonaisuudet: [
                {
                  _id: 1000,
                },
                {
                  _id: 1001,
                  sisaltoalueet: [
                    {id: 2000, nimi: {fi: 'Ajattelun taidot'}, teksti: {fi: 'Tarjotaan oppilaalle mahdollisuuksia löytää yhtäläisyyksiä, eroja ja säännönmukaisuuksia sekä käyttää käänteisyyden periaatetta. Vertaillaan, luokitellaan ja asetetaan järjestykseen sekä havaitaan syy- ja seuraussuhteita. Tutustuttaminen ohjelmoinnin alkeisiin alkaa laatimalla toimintaohjeita, joita myös testataan.'}},
                    {id: 2001, nimi: {fi: 'Luvut ja laskutoimitukset sekä algebra'}, teksti: {fi: '<p>Laskutoimituksissa käytetään luonnollisia lukuja. Varmistetaan, että oppilas hallitsee lukumäärän, lukusanan ja numeromerkinnän välisen yhteyden. Ymmärrystä luvuista laajennetaan laskemalla, hahmottamalla ja arvioimalla lukumääriä. Kehitetään oppilaan lukujonotaitoja sekä taitoa vertailla lukuja ja asettaa niitä järjestykseen. Tutkitaan lukujen ominaisuuksia kuten parillisuutta, monikertoja ja puolittamista. Laskutaidon perustaksi varmistetaan, että oppilaat tuntevat ja osaavat käyttää lukujen 2 – 10 hajotelmia.</p><p>Ohjataan oppilaita käyttämään lukuja tarkoituksenmukaisella tavalla eri tilanteissa, lukumäärän ja järjestyksen ilmaisemisessa, laskutoimituksissa sekä mittaamisen tuloksissa.</p>'}},
                    {id: 2002, nimi: {fi: 'Geometria ja mittaaminen'}, teksti: {fi: '<p>Kehitetään oppilaan taitoa hahmottaa kolmiulotteista ympäristöä ja tasoa sekä taitoa käyttää suunta- ja sijaintikäsitteitä.</p><p>Tutkitaan yhdessä kappaleita ja tasokuvioita. Tunnistamisen lisäksi rakennetaan ja piirretään. Ohjataan oppilasta löytämään ja nimeämään ominaisuuksia, joiden mukaan kappaleita ja tasokuviota myös luokitellaan.</p>'}}
                  ],
                  kohdealueet: [
                    {
                      nimi: {fi: 'Merkitys, arvot ja asenteet'},
                      tavoitteet: [
                        {
                          kuvaus: {fi: 'Tukea oppilaan innostusta ja kiinnostusta matematiikkaa kohtaan sekä positiivisen minäkuvan ja itseluottamuksen kehittymistä'},
                          sisaltoalueet: [2000, 2001, 2002],
                          osaaminen: [1, 2, 3],
                          arviointi: [
                            {kohde: {fi: 'Ensimmäinen kohde'}, kuvaus: {fi: 'Oppilas tekee asian A ja asian B.'}},
                            {kohde: {fi: 'Arvioinnin kohde 2'}, kuvaus: {fi: 'Oppilas tekee asian C ja hoitaa D:n ilman ohjausta.'}}
                          ]
                        }
                      ]
                    },
                    {
                      nimi: {fi: 'Työskentelyn taidot'},
                      tavoitteet: [
                        {
                          kuvaus: {fi: 'kehittää oppilaan taitoa tehdä havaintoja matematiikan näkökulmasta sekä tulkita ja hyödyntää niitä eri tilanteissa'},
                          sisaltoalueet: [2000, 2001, 2002],
                          osaaminen: [1, 2]
                        }
                      ]
                    },
                    {
                      nimi: {fi: 'Käsitteelliset ja tiedonalakohtaiset tavoitteet'},
                      tavoitteet: [
                        {
                          kuvaus: {fi: 'ohjaa oppilasta ymmärtämään matematiikan käsitteitä ja merkintätapoja'},
                          sisaltoalueet: [2000, 2001],
                          osaaminen: [1, 2]
                        },
                        {
                          kuvaus: {fi: 'ohjaa ymmärtämään lukukäsitteen ja kymmenjärjestelmän periaatteita'},
                          sisaltoalueet: [2001],
                          osaaminen: [1, 2]
                        },
                        {
                          kuvaus: {fi: 'tutustuttaa oppilasta peruslaskutoimitusten ominaisuuksiin ja periaatteisiin'},
                          sisaltoalueet: [2001],
                          osaaminen: [1, 2]
                        }
                      ]
                    }
                  ],
                  tekstikappaleet: [
                    {nimi: {fi: 'Oppiaineen tehtävä'}, teksti: {fi: 'Matematiikan opetuksen tehtävänä on kehittää oppilaan loogista, täsmällistä ja luovaa matemaattista ajattelua. Opetus luo pohjan matemaattisten käsitteiden ja rakenteiden ymmärtämiselle sekä kehittää oppilaan kykyä käsitellä tietoa ja ratkaista ongelmia. Matematiikan kumulatiivisesta luonteesta johtuen opetus etenee systemaattisesti. Konkretia ja toiminnallisuus ovat keskeinen osa matematiikan opetusta ja opiskelua. Oppimista tuetaan hyödyntämällä tieto- ja viestintäteknologiaa.'}},
                    {nimi: {fi: 'Ohjaus ja tuki'}, teksti: {fi: 'Oppilaiden osaamisessa on huomattavia eroja jo ennen koulun alkamista. Hierarkkisena oppiaineena matematiikan perusasioiden hallinta on välttämätön edellytys uusien sisältöjen oppimiselle. Tarjottavan tuen tulee kohdentua sekä edeltävien tieto- ja taitopuutteiden korjaamiseen että uusien sisältöjen oppimiseen. Matematiikan oppimisen valmiuksille ja matematiikan oppimiselle on varattava riittävästi aikaa ja tuen on oltava systemaattista. Oppilaan matematiikan osaamista ja taitojen kehittymistä seurataan ja tarvittaessa annetaan lisätukea heti tuen tarpeen ilmetessä. Tarjottava tuki antaa oppilaalle mahdollisuuden ymmärtää matematiikkaa ikätasonsa mukaisesti ja kehittää taitojaan niin, että oppimisen ja osaamisen ilo säilyvät. Oppilaille tarjotaan sopivia välineitä oppimisen tueksi ja hänelle tarjotaan mahdollisuuksia oivaltaa ja ymmärtää itse. Opiskelussa hyödynnetään arjen kokemuksia, ohjattuja pedagogisia leikkejä, pelejä, toimintamateriaaleja ja kuvia. Lisäksi moniaistillisuus ja kehollisuus luovat pohjaa oppimiselle. Oppilaalle turvataan mahdollisuus riittävään harjoitteluun.'}},
                  ]
                },
                {
                  _id: 1002
                },
              ],
              tekstikappaleet: [],
              tehtava: {nimi: {fi: 'Oppiaineen tehtävä'}, teksti: {fi: 'Oppiaineen tehtävän kuvaus yleisellä tasolla.'}},
              osaalue: {},
            },
            {
              nimi: {fi: 'Liikunta'},
              vuosiluokkakokonaisuudet: [
                {
                  _id: 1000,
                }
              ],
              tekstikappaleet: [],
              tehtava: {},
              osaalue: {},
            }
          ];
        default:
          return [];
      }
    };
  })

  .controller('PerusopetusSisaltoController', function ($scope, perusteprojektiTiedot, Algoritmit, $state,
      PerusopetusService) {
    $scope.projekti = perusteprojektiTiedot.getProjekti();
    $scope.peruste = perusteprojektiTiedot.getPeruste();
    $scope.rajaus = '';

    //$scope.peruste.sisalto = perusteprojektiTiedot.getSisalto();
    $scope.datat = {
      opetus: {lapset: []},
      sisalto: {lapset: PerusopetusService.getTekstikappaleet()}
    };
    console.log("sisalto ctrl");
    _.each(PerusopetusService.sisallot, function (item) {
      var data = {
        perusteenOsa: {nimi: item.label},
        tyyppi: item.tyyppi,
        lapset: PerusopetusService.getOsat(item.tyyppi)
      };
      $scope.datat.opetus.lapset.push(data);
    });
    $scope.peruste.sisalto = $scope.datat.sisalto;

    $scope.opetusHref = function (sisalto) {
      return $state.href('root.perusteprojekti.osalistaus', {osanTyyppi: sisalto.tyyppi});
    };

    $scope.rajaaSisaltoa = function(value) {
      if (_.isUndefined(value)) { return; }
      var filterer = function(osa, lapsellaOn) {
        osa.$filtered = lapsellaOn || Algoritmit.rajausVertailu(value, osa, 'perusteenOsa', 'nimi');
        return osa.$filtered;
      };
      Algoritmit.kaikilleTutkintokohtaisilleOsille($scope.datat.opetus, filterer);
      Algoritmit.kaikilleTutkintokohtaisilleOsille($scope.datat.sisalto, filterer);
    };

    $scope.avaaSuljeKaikki = function(sisalto, state) {
      var open = false;
      Algoritmit.kaikilleLapsisolmuille(sisalto, 'lapset', function(lapsi) {
        open = open || lapsi.$opened;
      });
      Algoritmit.kaikilleLapsisolmuille(sisalto, 'lapset', function(lapsi) {
        lapsi.$opened = _.isUndefined(state) ? !open : state;
      });
    };
  })

  .controller('OsalistausController', function ($scope, $state, $stateParams, PerusopetusService,
      virheService, PerusteprojektiTiedotService) {
    $scope.sisaltoState = _.find(PerusopetusService.sisallot, {tyyppi: $stateParams.osanTyyppi});
    if (!$scope.sisaltoState) {
      virheService.virhe('virhe-sivua-ei-löytynyt');
      return;
    }
    // TODO real data
    $scope.osaAlueet = [];
    var vuosiluokkakokonaisuudet = [];
    PerusteprojektiTiedotService.then(function (value) {
      PerusopetusService.setTiedot(value);
      $scope.osaAlueet = _.map(PerusopetusService.getOsat($stateParams.osanTyyppi), function (osa) {
        return $stateParams.osanTyyppi === PerusopetusService.OSAAMINEN ? osa.perusteenOsa : osa;
      });
      if ($stateParams.osanTyyppi === PerusopetusService.OPPIAINEET) {
        vuosiluokkakokonaisuudet = PerusopetusService.getOsat(PerusopetusService.VUOSILUOKAT);
      }
    });

    var oppiaineFilter = {
      template: '<label>{{\'vuosiluokkakokonaisuus\'|kaanna}}' +
        '<select class="form-control" ng-model="options.extrafilter.model"' +
        ' ng-options="obj as obj.nimi|kaanna for obj in options.extrafilter.options">' +
        '<option value="">{{\'kaikki\'|kaanna}}</option>' +
        '</select></label>',
      model : null,
      options: vuosiluokkakokonaisuudet,
      fn: function (query, value) {
        return !!_.find(value.vuosiluokkakokonaisuudet, function (item) {
          return item._id === query.id;
        });
      }
    };

    $scope.options = {
      extrafilter: $stateParams.osanTyyppi === PerusopetusService.OPPIAINEET ? oppiaineFilter : null,
    };

    $scope.createUrl = function (/*value*/) {
      // TODO proper parameters
      return $state.href('root.perusteprojekti.osaalue', {osanTyyppi: $stateParams.osanTyyppi, osanId: ''});
    };
  })

  .controller('OsaAlueController', function ($scope, $q, $stateParams, PerusopetusService,
      PerusteprojektiTiedotService) {
    $scope.isVuosiluokka = $stateParams.osanTyyppi === PerusopetusService.VUOSILUOKAT;
    $scope.isOppiaine = $stateParams.osanTyyppi === PerusopetusService.OPPIAINEET;
    // TODO real data
    $scope.versiot = {latest: true};
    var tekstikappale = {
      nimi: {fi: 'Tekstikappaleen otsikko'},
      teksti: {fi: '<p>Tekstikappaleen tekstiä lorem ipsum.</p>'}
    };
    $scope.dataObject = $q.defer();
    console.log("osaalue ctrl");
    var data = {};
    PerusteprojektiTiedotService.then(function (value) {
      PerusopetusService.setTiedot(value);
      data = ($scope.isVuosiluokka || $scope.isOppiaine) ?
        PerusopetusService.getOsat($stateParams.osanTyyppi)[0] : tekstikappale;
      _.extend($scope.dataObject, data);
      $scope.dataObject.resolve(data);
    });
  })

  /* protokoodia --> */
  .controller('PerusopetusController', function($scope, FilterWatcher, PerusOpetusTiedot, $timeout) {
    $scope.isNaviVisible = function () { return true; };

    var applyFilters = function ($event) {
      if ($event) {
        $event.preventDefault();
        $event.stopPropagation();
      }
      _.each($scope.navi.sections, function (mainsection) {
        if (mainsection.model) {
          _.each(mainsection.model.sections, function (section) {
            $scope.filtterit[section.title] = _(section.items).filter('$selected').pluck('value').value();
          });
        }
      });
      setPage();
    };

    $scope.navi = {
      header: 'perusteen-sisalto',
      oneAtATime: true,
      sections: [
        {
          title: 'Opetussuunnitelma',
          id: 'suunnitelma',
          items: PerusOpetusTiedot.yleiset
        },
        {
          title: 'Opetuksen sisällöt',
          id: 'sisalto',
          include: 'views/partials/navifilters.html',
          $open: true,
          model: {
            oneAtATime: false,
            sections: [
              {
                title: 'Vuosiluokat',
                apply: applyFilters,
                $open: true,
                $condensed: true,
                items: _.map(PerusOpetusTiedot.luokat, function (luokka) {
                  return {label: 'Vuosiluokka ' + luokka, value: luokka};
                })
              },
              {
                title: 'Oppiaineet',
                items: PerusOpetusTiedot.oppiaineet,
                $open: true,
                apply: applyFilters
              },
              {
                title: 'Oppiaineen sisällöt',
                $open: true,
                items: PerusOpetusTiedot.oppiaineenSisallot,
                apply: applyFilters
              },
            ]
          }
        },
        {
          title: 'Liitteet',
          id: 'liitteet'
        },
      ]
    };

    $scope.filtterit = {moodi: 'sivutus', sivu: 1};
    var setPage = function () {
      // set page to first valid page
      if ($scope.filtterit.moodi === 'sivutus' && !_.isEmpty($scope.filtterit.Vuosiluokat)) {
        $scope.filtterit.sivu = $scope.filtterit.Vuosiluokat[0];
      }
    };
    $scope.$watch('filtterit.moodi', function () {
      setPage();
    });

    // Watch which main section is currently open
    $scope.$watch(function () {
      return '' + $scope.navi.sections[0].$open + $scope.navi.sections[1].$open + $scope.navi.sections[2].$open;
    }, function () {
      var active = _.find($scope.navi.sections, '$open');
      $scope.activeSection = active ? active.id : null;
      if ($scope.activeSection === 'sisalto') {
        $timeout(function () {
          applyFilters();
        });
      }
    });

    FilterWatcher.register(applyFilters);
  })
  .service('FilterWatcher', function () {
    var cb = angular.noop;
    this.register = function (callback) {
      cb = callback;
    };
    this.notify = function () {
      cb();
    };
  })

  .directive('multichoice', function (FilterWatcher) {
    return {
      restrict: 'AE',
      templateUrl: 'views/partials/multichoice.html',
      scope: {
        items: '=',
        condensed: '='
      },
      link: function (scope) {
        _.each(scope.items, function (item) {
          item.$selected = true;
        });
        // activate initial choices when choices have been rendered
        FilterWatcher.notify();
      },
      controller: function ($scope) {
        $scope.model = {
          all: true
        };
        $scope.toggle = function () {
          _.each($scope.items, function (item) {
            item.$selected = $scope.model.all;
          });
        };
        $scope.update = function () {
          var all = _.filter($scope.items, '$selected').length === $scope.items.length;
          if (all && !$scope.model.all) {
            $scope.model.all = true;
          }
          if (!all && $scope.model.all) {
            $scope.model.all = false;
          }
        };
      }
    };
  })

  /*.controller('TagFilterController', function ($scope) {
    $scope.model = {
      tags: {
        items: [],
        clear: function () {
          $scope.model.tags.items = [];
        }
      },
      dialog: {
        isOpen: false,
        addButtonPressed: function () {
          $scope.model.dialog.isOpen = !$scope.model.dialog.isOpen;
        },
        close: function () {
          $scope.model.dialog.isOpen = false;
        }
      }
    };
  })*/

  .service('PerusOpetusTiedot', function () {
    this.yleiset = [
      {label: '1 PAIKALLISEN OPETUSSUUNNITELMAN MERKITYS JA LAADINTA', depth: 0},
      {label: '1.1 Opetussuunnitelman perusteet ja paikallinen opetussuunnitelma', depth: 1},
      {label: '1.2 Opetussuunnitelman laatimista ohjaavat periaatteet', depth: 1},
      {label: '1.3 Opetussuunnitelman arviointi ja kehittäminen 6', depth: 1},
      {label: '1.4 Paikallisen opetussuunnitelman laadinta ja keskeiset opetusta ohjaavat ratkaisut', depth: 1},
      {label: '2 PERUSOPETUS YLEISSIVISTYKSEN PERUSTANA', depth: 0},
      {label: '2.1 Opetuksen järjestämistä ohjaavat velvoitteet', depth: 1},
      {label: '2.2 Perusopetuksen arvoperusta', depth: 1},
      {label: '2.3 Oppimiskäsitys', depth: 1},
      {label: '2.4 Paikallisesti päätettävät asiat', depth: 1},
      {label: '3 PERUSOPETUKSEN TEHTÄVÄ JA TAVOITTEET', depth: 0},
      {label: '3.1 Perusopetuksen tehtävä', depth: 1},
      {label: '3.2 Opetuksen ja kasvatuksen valtakunnalliset tavoitteet', depth: 1},
      {label: '3.3 Laaja-alainen osaaminen', depth: 1},
      {label: '3.4 Paikallisesti päätettävät asiat', depth: 1},
      {label: '4 YHTENÄISEN PERUSOPETUKSEN TOIMINTAKULTTUURI', depth: 0},
      {label: '4.1 Toimintakulttuurin merkitys ja kehittäminen', depth: 1},
      {label: '4.2 Toimintakulttuurin kehittämistä ohjaavat periaatteet', depth: 1},
      {label: '4.3 Oppimisympäristöt ja työtavat', depth: 1},
      {label: '4.4 Opetuksen eheyttäminen ja monialaiset oppimiskokonaisuudet', depth: 1},
      {label: '4.5 Paikallisesti päätettävät asiat', depth: 1},
      {label: '5 OPPIMISEN JA HYVINVOINNIN EDISTÄMINEN KOULUTYÖN JÄRJESTÄMISESSÄ', depth: 0},
      {label: '5.1 Yhteinen vastuu koulupäivästä', depth: 1},
      {label: '5.2 Yhteistyö', depth: 1},
      {label: '5.3 Opetuksen järjestäminen eri tilanteissa', depth: 1},
      {label: '5.4 Perusopetusta tukeva muu toiminta', depth: 1},
      {label: '5.5 Paikallisesti päätettävät asiat', depth: 1},
      {label: '6 OPPIMISEN ARVIOINTI', depth: 0},
      {label: '6.1 Arvioinnin tehtävät ja oppimista tukeva arviointikulttuuri', depth: 1},
      {label: '6.2 Arvioinnin luonne ja yleiset periaatteet', depth: 1},
      {label: '6.3 Arvioinnin kohteet', depth: 1},
      {label: '6.4 Opintojen aikainen arviointi', depth: 1},
      {label: '6.4.1 Arviointi lukuvuoden aikana', depth: 2},
      {label: '6.4.2 Arviointi lukuvuoden päättyessä', depth: 2},
      {label: '6.4.3 Opinnoissa eteneminen perusopetuksen aikana', depth: 2},
      {label: '6.4.4 Arviointi nivelvaiheissa', depth: 2},
      {label: '6.5 Perusopetuksen päättöarviointi', depth: 1},
      {label: '6.5.1 Päättöarvosanan muodostaminen', depth: 2},
      {label: '6.5.2 Johonkin oppiaineeseen tai erityiseen tehtävään painottuva opetus ja päättöarviointi', depth: 2},
      {label: '6.6 Perusopetuksessa käytettävät todistukset ja todistusmerkinnät', depth: 1},
      {label: '6.7 Paikallisesti päätettävät asiat', depth: 1},
      {label: '7 OPPIMISEN JA KOULUNKÄYNNIN TUKI', depth: 0},
      {label: '7.1 Tuen järjestämistä ohjaavat periaatteet', depth: 1},
      {label: '7.1.1 Ohjaus tuen aikana', depth: 2},
      {label: '7.1.2 Kodin ja koulun yhteistyö tuen aikana', depth: 2},
      {label: '7.2 Yleinen tuki', depth: 1},
      {label: '7.3 Tehostettu tuki', depth: 1},
      {label: '7.3.1 Pedagoginen arvio', depth: 2},
      {label: '7.3.2 Oppimissuunnitelma tehostetun tuen aikana', depth: 2},
      {label: '7.4 Erityinen tuki', depth: 1},
      {label: '7.4.1 Pedagoginen selvitys', depth: 2},
      {label: '7.4.2 Erityisen tuen päätös', depth: 2},
      {label: '7.4.3 Henkilökohtainen opetuksen järjestämistä koskeva suunnitelma', depth: 2},
      {label: '7.4.4 Oppiaineen oppimäärän yksilöllistäminen ja opetuksesta vapauttaminen', depth: 2},
      {label: '7.4.5 Pidennetty oppivelvollisuus', depth: 2},
      {label: '7.4.6 Toiminta-alueittain järjestettävä opetus', depth: 2},
      {label: '7.5 Perusopetuslaissa säädetyt tukimuodot', depth: 1},
      {label: '7.5.1 Tukiopetus', depth: 2},
      {label: '7.5.2 Osa-aikainen erityisopetus', depth: 2},
      {label: '7.5.3 Opetukseen osallistumisen edellyttämät palvelut ja apuvälineet', depth: 2},
      {label: '7.6 Paikallisesti päätettävät asiat', depth: 1},
      {label: '8 OPPILASHUOLTO', depth: 0},
      {label: '9 KIELI- JA KULTTUURIRYHMIEN OPETUS', depth: 0},
      {label: '9.1 Saamelaiset ja saamenkieliset', depth: 1},
      {label: '9.2 Romanit', depth: 1},
      {label: '9.3 Viittomakieliset', depth: 1},
      {label: '9.4 Muutoin monikieliset oppilaat', depth: 1},
      {label: '9.5 Paikallisesti päätettävät asiat', depth: 1},
      {label: '10 KAKSIKIELINEN OPETUS', depth: 0},
      {label: '10.1 Kaksikielisen opetuksen tavoitteet ja opetuksen järjestämisen lähtökohtia', depth: 1},
      {label: '10.2 Laajamittainen kaksikielinen opetus', depth: 1},
      {label: '10.3 Suppeampi kaksikielinen opetus', depth: 1},
      {label: '10.4 Paikallisesti päätettävät asiat', depth: 1},
      {label: '11 ERITYISEEN MAAILMANKATSOMUKSEEN TAI KASVATUSOPILLISEEN JÄRJESTELMÄÄN PERUSTUVA PERUSOPETUS', depth: 0},
      {label: '11.1 Opetuksen järjestämisen periaatteet', depth: 1},
      {label: '11.2 Paikallisesti päätettävät asiat', depth: 1},
      {label: '12 VALINNAISET OPINNOT', depth: 0},
    ];

    this.luokat = _.range(1,10);

    var oppiainelista = [
      {label: 'Äidinkieli ja kirjallisuus'},
      {label: 'Suomenkieli ja kirjallisuus', depth: 1},
      {label: 'Saamenkieli ja kirjallisuus', depth: 1},
      {label: 'Romanikieli ja kirjallisuus', depth: 1},
      {label: 'Toinen kotimainen kieli'},
      {label: 'Ruotsi, A-oppimäärä', depth: 1},
      {label: 'Ruotsi, B1-oppimäärä', depth: 1},
      {label: 'Matematiikka'},
      {label: 'Fysiikka'},
      {label: 'Kemia'},
      {label: 'Musiikki'},
      {label: 'Liikunta'},
    ];
    this.oppiaineet = _.map(oppiainelista, function (oppiaine) {
      oppiaine.depth = oppiaine.depth || 0;
      oppiaine.value = oppiaine.label;
      return oppiaine;
    });

    this.oppiaineenSisallot = _.map([
      'Oppiaineen tehtävä',
      'Opetuksen tavoitteet',
      'Tavoitteisiin liittyvät keskeiset sisältöalueet',
      'Oppimisympäristöihin ja työtapoihin liittyvät tavoitteet',
      'Ohjaus ja tuki oppiaineessa',
      'Oppilaan oppimisen arviointi'
    ], function (sisalto) {
      return {label: sisalto, value: sisalto, depth: 0};
    });
  });
