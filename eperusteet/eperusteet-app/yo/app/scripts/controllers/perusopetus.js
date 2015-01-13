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
  .controller('PerusopetusSisaltoController', function ($scope, perusteprojektiTiedot, Algoritmit, $state, SuoritustavanSisalto,
      PerusopetusService, TekstikappaleOperations, Editointikontrollit, $stateParams, Notifikaatiot, Utils, VlkUtils) {
    $scope.projekti = perusteprojektiTiedot.getProjekti();
    $scope.peruste = perusteprojektiTiedot.getPeruste();
    TekstikappaleOperations.setPeruste($scope.peruste);
    $scope.rajaus = '';

    $scope.tuoSisalto = SuoritustavanSisalto.tuoSisalto();
    $scope.$esitysurl = $state.href('root.selaus.perusopetus', {
      perusteId: $scope.peruste.id
    });

    $scope.$watch('peruste.sisalto', function () {
      Algoritmit.kaikilleLapsisolmuille($scope.peruste.sisalto, 'lapset', function (lapsi) {
        lapsi.$url = $state.href('root.perusteprojekti.suoritustapa.tekstikappale', {
          suoritustapa: 'perusopetus',
          perusteenOsaViiteId: lapsi.id,
          versio: '' });
      });
    }, true);

    $scope.datat = {
      opetus: {lapset: []},
      sisalto: perusteprojektiTiedot.getYlTiedot().sisalto
    };

    $scope.$watch('datat.opetus.lapset', function () {
      _.each($scope.datat.opetus.lapset, function (area) {
        area.$type = 'ep-parts';
        area.$url = $state.href('root.perusteprojekti.suoritustapa.osalistaus', {suoritustapa: $stateParams.suoritustapa, osanTyyppi: area.tyyppi});
        area.$orderFn = area.tyyppi === PerusopetusService.VUOSILUOKAT ? VlkUtils.orderFn : Utils.nameSort;
        Algoritmit.kaikilleLapsisolmuille(area, 'lapset', function (lapsi) {
          lapsi.$url = $state.href('root.perusteprojekti.suoritustapa.osaalue', {suoritustapa: $stateParams.suoritustapa, osanTyyppi: area.tyyppi, osanId: lapsi.id, tabId: 0});
          if (lapsi.koosteinen) {
            lapsi.lapset = _.sortBy(lapsi.oppimaarat, Utils.nameSort);
          }
        });
      });
    }, true);

    // TODO käytä samaa APIa kuin sivunavissa, koko sisältöpuu kerralla
    _.each(PerusopetusService.sisallot, function (item) {
      var data = {
        nimi: item.label,
        tyyppi: item.tyyppi
      };
      PerusopetusService.getOsat(item.tyyppi, true).then(function (res) {
        data.lapset = res;
      });
      $scope.datat.opetus.lapset.push(data);
    });
    $scope.peruste.sisalto = $scope.datat.sisalto;

    $scope.rajaaSisaltoa = function(value) {
      if (_.isUndefined(value)) { return; }
      var sisaltoFilterer = function(osa, lapsellaOn) {
        osa.$filtered = lapsellaOn || Algoritmit.rajausVertailu(value, osa, 'perusteenOsa', 'nimi');
        return osa.$filtered;
      };
      var filterer = function(osa, lapsellaOn) {
        osa.$filtered = lapsellaOn || Algoritmit.rajausVertailu(value, osa, 'nimi');
        return osa.$filtered;
      };
      Algoritmit.kaikilleTutkintokohtaisilleOsille($scope.datat.opetus, filterer);
      Algoritmit.kaikilleTutkintokohtaisilleOsille($scope.datat.sisalto, sisaltoFilterer);
    };

    $scope.avaaSuljeKaikki = function(value) {
      var open = _.isUndefined(value) ? false : !value;
      if (_.isUndefined(value)) {
        Algoritmit.kaikilleLapsisolmuille($scope.datat.opetus, 'lapset', function(lapsi) {
          open = open || lapsi.$opened;
        });
      }
      Algoritmit.kaikilleLapsisolmuille($scope.datat.sisalto, 'lapset', function(lapsi) {
        lapsi.$opened = !open;
      });
      Algoritmit.kaikilleLapsisolmuille($scope.datat.opetus, 'lapset', function(lapsi) {
        lapsi.$opened = !open;
      });
    };

    $scope.addTekstikappale = function () {
      TekstikappaleOperations.add();
    };

    $scope.edit = function () {
      Editointikontrollit.startEditing();
    };

    Editointikontrollit.registerCallback({
      edit: function() {
        $scope.rajaus = '';
        $scope.avaaSuljeKaikki(true);
      },
      save: function() {
        TekstikappaleOperations.updateViitteet($scope.peruste.sisalto, function () {
          Notifikaatiot.onnistui('osien-rakenteen-päivitys-onnistui');
        });
      },
      cancel: function() {
        $state.go($state.current.name, $stateParams, {
          reload: true
        });
      },
      validate: function() { return true; },
      notify: function (value) {
        $scope.editing = value;
      }
    });

  })

  .controller('OsalistausController', function ($scope, $state, $stateParams, PerusopetusService,
      virheService) {
    $scope.sisaltoState = _.find(PerusopetusService.sisallot, {tyyppi: $stateParams.osanTyyppi});
    if (!$scope.sisaltoState) {
      virheService.virhe('virhe-sivua-ei-löytynyt');
      return;
    }
    var vuosiluokkakokonaisuudet = [];
    $scope.osaAlueet = [];
    PerusopetusService.getOsat($stateParams.osanTyyppi).then(function (res) {
      $scope.osaAlueet = res;
    });
    if ($stateParams.osanTyyppi === PerusopetusService.OPPIAINEET) {
      PerusopetusService.getOsat(PerusopetusService.VUOSILUOKAT, true).then(function (res) {
        vuosiluokkakokonaisuudet = res;
      });
    }

    var oppiaineFilter = {
      template: '<label>{{\'vuosiluokkakokonaisuus\'|kaanna}}' +
        '<select class="form-control" ng-model="options.extrafilter.model"' +
        ' ng-options="obj as obj.nimi|kaanna for obj in options.extrafilter.options">' +
        '<option value="">{{\'kaikki\'|kaanna}}</option>' +
        '</select></label>',
      model : null,
      options: vuosiluokkakokonaisuudet,
      fn: function (/*query, value*/) {
        // TODO vuosiluokkakokonaisuusfiltteri
        return true;
        /*return !!_.find(value.vuosiluokkakokonaisuudet, function (item) {
          return item._id === query.id;
        });*/
      }
    };

    $scope.options = {
      extrafilter: $stateParams.osanTyyppi === PerusopetusService.OPPIAINEET ? oppiaineFilter : null,
    };

    $scope.createUrl = function (value) {
      return $state.href('root.perusteprojekti.suoritustapa.osaalue', {
        suoritustapa: $stateParams.suoritustapa,
        osanTyyppi: $stateParams.osanTyyppi,
        osanId: value.id,
        tabId: 0
      });
    };

    $scope.add = function () {
      $state.go('root.perusteprojekti.suoritustapa.osaalue', {
        suoritustapa: $stateParams.suoritustapa,
        osanTyyppi: $stateParams.osanTyyppi,
        osanId: 'uusi',
        tabId: 0
      });
    };
  })

  // FIXME: Taitaa olla deprekoitunut
  .controller('OsaAlueController', function ($scope, $q, $stateParams, PerusopetusService,
    ProjektinMurupolkuService) {
    $scope.isVuosiluokka = $stateParams.osanTyyppi === PerusopetusService.VUOSILUOKAT;
    $scope.isOppiaine = $stateParams.osanTyyppi === PerusopetusService.OPPIAINEET;
    $scope.isOsaaminen = $stateParams.osanTyyppi === PerusopetusService.OSAAMINEN;
    $scope.versiot = {latest: true};
    $scope.dataObject = PerusopetusService.getOsa($stateParams);
    var labels = _.invert(PerusopetusService.LABELS);
    ProjektinMurupolkuService.set('osanTyyppi', $stateParams.osanTyyppi, labels[$stateParams.osanTyyppi]);
    $scope.dataObject.then(function (res) {
      ProjektinMurupolkuService.set('osanId', $stateParams.osanId, res.nimi);
    });
  })

  .controller('PerusopetusController', function($q, $scope, $timeout, sisalto, PerusteenOsat, OppiaineenVuosiluokkakokonaisuudet, Algoritmit, Notifikaatiot, Oppiaineet) {
    $scope.isNaviVisible = _.constant(true);
    var peruste = sisalto[0];
    var oppiaineet = _.zipBy(sisalto[2], 'id');
    $scope.osaamiset = _.zipBy(sisalto[1], 'id');
    $scope.sisallot = _.zipBy(sisalto[3], 'id');
    $scope.vuosiluokkakokonaisuudet = _(sisalto[3]).each(function(s) { s.vuosiluokat.sort(); })
                                                   .sortBy(function(s) { return _.first(s.vuosiluokat); })
                                                   .value();
    $scope.vuosiluokkakokonaisuudetMap = _.zipBy($scope.vuosiluokkakokonaisuudet, 'id');
    $scope.valittuOppiaine = {};
    $scope.filterSisalto = {};
    $scope.filterOsaamiset = {};
    $scope.tekstisisalto = sisalto[4];

    $scope.filtterit = {
      moodi: 'sivutus',
    };

    $scope.valitseVuosiluokka = function(vuosiluokka) {
      $scope.valittuOppiaine.vlks = undefined;
      $timeout(function() {
        $scope.filtterit.valittuKokonaisuus = vuosiluokka;
        $scope.valittuOppiaine.vlks = $scope.valittuOppiaine.vuosiluokkakokonaisuudet[vuosiluokka];
        $scope.valittuOppiaine.sisallot = $scope.sisallot[$scope.valittuOppiaine.vlks.vuosiluokkaKokonaisuus];
        paivitaTavoitteet();
      });
    };

    function valitseOppiaine(oppiaine) {
      oppiaine = _.isObject(oppiaine) ? oppiaine.value : oppiaine;
      Oppiaineet.get({ perusteId: peruste.id, osanId: oppiaine }, function(res) {
        var valittuOppiaine = {};
        valittuOppiaine.oppiaine = res;
        valittuOppiaine.vuosiluokkakokonaisuudet = _.zipBy(res.vuosiluokkakokonaisuudet, 'vuosiluokkaKokonaisuus');
        $scope.valittuOppiaine = valittuOppiaine;
        $scope.valitseVuosiluokka($scope.valittuOppiaine.vuosiluokkakokonaisuudet[$scope.filtterit.valittuKokonaisuus] ?
                                  $scope.filtterit.valittuKokonaisuus :
                                  _.first(_.keys($scope.valittuOppiaine.vuosiluokkakokonaisuudet)));
      }, Notifikaatiot.serverCb);
    }

    function valitseAktiivinenTekstisisalto(osaId) {
      $scope.valittuTekstisisalto = undefined;
      PerusteenOsat.get({
        osanId: osaId
      }, function(res) {
        $scope.valittuTekstisisalto = res;
      });
    }

    function rakennaTekstisisalto() {
      var suunnitelma = [];
      Algoritmit.kaikilleLapsisolmuille($scope.tekstisisalto, 'lapset', function(osa, depth) {
        suunnitelma.push({
          $osa: osa,
          label: osa.perusteenOsa.nimi,
          depth: depth,
        });
      });
      if ($scope.tekstisisalto && $scope.tekstisisalto.lapset) {
        valitseAktiivinenTekstisisalto($scope.tekstisisalto.lapset[0]._perusteenOsa);
        _.first(suunnitelma).$selected = true;
      }
      return suunnitelma;
    }

    $scope.navi = {
      header: 'perusteen-sisalto',
      showOne: true,
      sections: [{
          include: 'views/partials/perusopetustekstisisalto.html',
          title: 'Opetussuunnitelma',
          $open: true,
          id: 'suunnitelma',
          items: rakennaTekstisisalto(),
          update: function(item, section) {
            valitseAktiivinenTekstisisalto(item.$osa._perusteenOsa);
            _.each(section.items, function(osa) { osa.$selected = false; });
            item.$selected = true;
          }
        }, {
          title: 'Opetuksen sisällöt',
          id: 'sisalto',
          include: 'views/partials/navifilters.html',
          $open: false,
          model: {
            sections: [{
              title: 'Vuosiluokat',
              apply: angular.noop,
              $open: false,
              $condensed: true,
              items: _.map($scope.vuosiluokkakokonaisuudet, function(kokonaisuus) {
                return { label: kokonaisuus.nimi, value: kokonaisuus.id, $selected: true };
              }),
              update: function(item, section) {
                paivitaSivunavi(section);
              }
            }, {
              title: 'Oppiaineet',
              id: 'oppiaineet',
              items: [],
              $open: true,
              include: 'views/partials/perusopetusoppiaineetsivunavi.html',
              apply: angular.noop,
              selectOppiaine: valitseOppiaine,
              isCurrentOppiaine: function(item) {
                return $scope.valittuOppiaine && $scope.valittuOppiaine.oppiaine && $scope.valittuOppiaine.oppiaine.id === item.value;
              }
            }, {
              id: 'sisallot',
              title: 'Oppiaineen sisällöt',
              $all: true,
              $open: false,
              items: _.map(['tehtava', 'ohjaus', 'tyotavat', 'tavoitteet'], function(item) {
                return { label: 'perusopetus-' + item, value: item, depth: 0, $selected: true };
              }),
              update: function(item) {
                $scope.filterSisalto[item.value] = !item.$selected;
              }
            }, {
              id: 'osaamiset',
              title: 'Laaja-alaiset osaamiset',
              $all: true,
              $open: false,
              items: _.map($scope.osaamiset, function(item) {
                return { label: item.nimi, value: item.id, depth: 0, $selected: false };
              }),
              update: function(item) {
                $scope.filterOsaamiset[item.value] = !item.$selected;
                paivitaTavoitteet();
              }
            }]
          }
        }, {
          title: 'Liitteet',
          id: 'liitteet'
        },
      ]
    };

    function paivitaTavoitteet() {
      if ($scope.valittuOppiaine.vlks) {
        var filteritTyhjat = _.all($scope.filterOsaamiset, function(v) {
          return v === true;
        });
        _.each($scope.valittuOppiaine.vlks.tavoitteet, function(tavoite) {
          if (filteritTyhjat || !_.isEmpty(tavoite.laajattavoitteet)) {
            tavoite.$rejected = false;
          }
          else {
            tavoite.$rejected = !_.any(tavoite.laajattavoitteet, function(lt) {
              return $scope.filterOsaamiset[lt] === false;
            });
          }
        });
      }
    }

    function paivitaSivunavi(vlfiltteri) {
      var navi = {};
      var vlfiltteriMap = _.zipBy(vlfiltteri ? vlfiltteri.items : [], 'value', '$selected');
      navi.oppiaineet = [];
      _.forEach(_.values(oppiaineet), function(oa) {
        function vuosiluokkaFiltteri(item) {
          return !_.isEmpty(item.vuosiluokkakokonaisuudet) && (!vlfiltteri || _.any(item.vuosiluokkakokonaisuudet, function(vlk) {
            return vlfiltteriMap[vlk._vuosiluokkaKokonaisuus];
          }));
        }

        if (vuosiluokkaFiltteri(oa)) {
          navi.oppiaineet.push({
            depth: 0,
            label: oa.nimi,
            value: oa.id
          });

          if (oa.koosteinen && oa.oppimaarat && oa.oppimaarat.length > 0) {
            _.each(oa.oppimaarat, function(om) {
              if (vuosiluokkaFiltteri(om)) {
                navi.oppiaineet.push({
                  label: om.nimi,
                  value: om.id,
                  depth: 1
                });
              }
            });
          }
        }
      });

      _.each($scope.navi.sections[1].model.sections, function(v) {
        if (navi[v.id]) {
          v.items = navi[v.id];
        }
      });
    }
    paivitaSivunavi();

    // Watch which main section is currently open
    $scope.$watch(function () {
      return '' + $scope.navi.sections[0].$open + $scope.navi.sections[1].$open + $scope.navi.sections[2].$open;
    }, function () {
      var active = _.find($scope.navi.sections, '$open');
      $scope.activeSection = active ? active.id : null;
    });

    valitseOppiaine(_.first(_.keys(oppiaineet)));
  });
