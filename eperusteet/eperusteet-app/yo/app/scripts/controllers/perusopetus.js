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
        lapsi.$url = lapsi.perusteenOsa.tunniste === 'laajaalainenosaaminen' ?
          $state.href('root.perusteprojekti.suoritustapa.osalistaus', {
            suoritustapa: 'perusopetus',
            osanTyyppi: 'osaaminen'
          }) :
          $state.href('root.perusteprojekti.suoritustapa.tekstikappale', {
              suoritustapa: 'perusopetus',
              perusteenOsaViiteId: lapsi.id,
              versio: ''
            });
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

  .controller('PerusopetusController', function($q, $scope, $timeout, sisalto, PerusteenOsat, OppiaineenVuosiluokkakokonaisuudet, Utils,
                                                Algoritmit, Notifikaatiot, Oppiaineet, TermistoService, Kieli, $document, $rootScope) {
    $scope.isNaviVisible = _.constant(true);
    $scope.hasContent = function (obj) {
      return _.isObject(obj) && obj.teksti && obj.teksti[Kieli.getSisaltokieli()];
    };
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
    $scope.currentSection = 'suunnitelma';
    $scope.activeSection = 'suunnitelma';

    TermistoService.setPeruste(peruste);

    function clickHandler(event) {
      var ohjeEl = angular.element(event.target).closest('.popover, .popover-element');
      if (ohjeEl.length === 0) {
        $rootScope.$broadcast('ohje:closeAll');
      }
    }
    function installClickHandler() {
      $document.off('click', clickHandler);
      $timeout(function () {
        $document.on('click', clickHandler);
      });
    }
    $scope.$on('$destroy', function () {
      $document.off('click', clickHandler);
    });

    $scope.filtterit = {
      moodi: 'sivutus',
    };

    $scope.onSectionChange = function(section) {
      $scope.currentSection = section.id;
      $scope.activeSection = section.id;

      if (_.isEmpty($scope.valittuOppiaine) && section.id === 'sisalto') {
        selectOppiaine(_.first(_.keys(oppiaineet)));
      }
    };

    $scope.valitseOppiaineenVuosiluokka = function(vuosiluokka) {
      $timeout(function() {
        $scope.filtterit.valittuKokonaisuus = vuosiluokka;
        $scope.valittuOppiaine.vlks = $scope.valittuOppiaine.vuosiluokkakokonaisuudet[vuosiluokka];
        if ($scope.valittuOppiaine.vlks) {
          $scope.valittuOppiaine.sisallot = $scope.sisallot[$scope.valittuOppiaine.vlks._vuosiluokkaKokonaisuus];
        }
        paivitaTavoitteet();
      });
    };

    function activeSection() {
      return $scope.activeSection;
    }

    function isCurrentOppiaine(item) {
      var id = _.isObject(item) ? item.value : item;
      return $scope.valittuOppiaine && $scope.valittuOppiaine.oppiaine && $scope.valittuOppiaine.oppiaine.id === id;
    }

    function selectOppiaine(oppiaine) {
      var id = _.isObject(oppiaine) ? oppiaine.value : oppiaine;
      Oppiaineet.get({ perusteId: peruste.id, osanId: id }, function(res) {
        var valittuOppiaine = {};
        valittuOppiaine.oppiaine = res;
        valittuOppiaine.vuosiluokkakokonaisuudet = _.zipBy(res.vuosiluokkakokonaisuudet, '_vuosiluokkaKokonaisuus');
        $scope.valittuOppiaine = valittuOppiaine;

        var valittavaVuosiluokka = $scope.valittuOppiaine.vuosiluokkakokonaisuudet[$scope.filtterit.valittuKokonaisuus] ?
          $scope.filtterit.valittuKokonaisuus :
          _.first(_.keys($scope.valittuOppiaine.vuosiluokkakokonaisuudet));

        if ($scope.currentSection === 'vlk') {
          valittavaVuosiluokka = $scope.valittuVuosiluokkakokonaisuus.id;
        }

        $scope.valitseOppiaineenVuosiluokka(valittavaVuosiluokka, true);
        $scope.activeSection = 'sisalto';
      }, Notifikaatiot.serverCb);
    }

    function valitseAktiivinenTekstisisalto(osaId) {
      PerusteenOsat.get({
        osanId: osaId
      }, function(res) {
        $scope.valittuTekstisisalto = res;
        $scope.activeSection = 'suunnitelma';
      });
    }

    function rakennaVuosiluokkakokonaisuuksienSisalto() {
      var sisalto = _.map($scope.vuosiluokkakokonaisuudet, function(vkl) {
        return {
          $oppiaineet: _(oppiaineet).filter(function(oa) {
              return _.some(oa.vuosiluokkakokonaisuudet, function(oavkl) {
                return _.parseInt(oavkl._vuosiluokkaKokonaisuus) === vkl.id;
              });
            }).value(),
          $vkl: vkl,
          label: vkl.nimi,
          depth: 0,
        };
      });

      if (!_.isEmpty(sisalto)) {
        _.first(sisalto).$selected = true;
        $scope.valittuVuosiluokkakokonaisuus = _.first(sisalto).$vkl;
      }
      return sisalto;
    }

    function rakennaTekstisisalto() {
      var suunnitelma = [];
      Algoritmit.kaikilleLapsisolmuille($scope.tekstisisalto, 'lapset', function(osa, depth) {
        suunnitelma.push({
          $osa: osa,
          label: osa.perusteenOsa ? osa.perusteenOsa.nimi : '',
          depth: depth,
        });
      });
      if ($scope.tekstisisalto && $scope.tekstisisalto.lapset && !_.isEmpty($scope.tekstisisalto.lapset)) {
        valitseAktiivinenTekstisisalto($scope.tekstisisalto.lapset[0]._perusteenOsa);
        _.first(suunnitelma).$selected = true;
      }
      return suunnitelma;
    }

    function paivitaTavoitteet() {
      if ($scope.valittuOppiaine.vlks) {
        var filteritTyhjat = _.all($scope.filterOsaamiset, function(v) { return v; });
        _.each($scope.valittuOppiaine.vlks.tavoitteet, function(tavoite) {
          if (filteritTyhjat || _.isEmpty(tavoite.laajattavoitteet)) {
            tavoite.$rejected = false;
          }
          else {
            tavoite.$rejected = _.all(tavoite.laajattavoitteet, function(lt) {
              return $scope.filterOsaamiset[lt];
            });
          }
        });
      }
      installClickHandler();
    }

    function paivitaSivunavi() {
      var navi = {};
      navi.oppiaineet = [];
      _.forEach(_.values(oppiaineet), function(oa) {
        navi.oppiaineet.push(oa);
      });

      _.each($scope.navi.sections[2].model.sections, function(v) {
        if (navi[v.id]) {
          v.items = navi[v.id];
        }
      });
    }

    $scope.navi = {
      header: 'perusteiden-sisalto',
      showOne: true,
      sections: [{
          id: 'suunnitelma',
          include: 'views/partials/perusopetustekstisisalto.html',
          items: rakennaTekstisisalto(),
          title: 'yhteiset-osuudet',
          update: function(item, section) {
            valitseAktiivinenTekstisisalto(item.$osa._perusteenOsa);
            _.each(section.items, function(osa) { osa.$selected = false; });
            item.$selected = true;
          }
        }, {
          title: 'vuosiluokkakokonaisuudet',
          id: 'vlk',
          items: rakennaVuosiluokkakokonaisuuksienSisalto(),
          include: 'views/partials/perusopetuksenvuosiluokkakokonaisuus.html',
          update: function(item, section) {
            _.each(section.items, function(osa) { osa.$selected = false; });
            item.$selected = true;
            $scope.valittuVuosiluokkakokonaisuus = item.$vkl;
            $scope.activeSection = 'vlk';
          },
          selectOppiaine: selectOppiaine,
          isCurrentOppiaine: isCurrentOppiaine,
          activeSection: activeSection,
          currentSection: function() { return $scope.currentSection; },
          $oppiaineOrder: Utils.oppiaineSort
        }, {
          title: 'opetuksen-sisallot',
          id: 'sisalto',
          include: 'views/partials/navifilters.html',
          model: {
            sections: [{
              $condensed: true,
              items: _.map($scope.vuosiluokkakokonaisuudet, function(kokonaisuus) {
                return { label: kokonaisuus.nimi, value: kokonaisuus.id, $selected: true };
              }),
              update: paivitaSivunavi
            }, {
              title: 'oppiaineet',
              id: 'oppiaineet',
              items: [],
              $open: true,
              include: 'views/partials/perusopetusoppiaineetsivunavi.html',
              selectOppiaine: selectOppiaine,
              isCurrentOppiaine: isCurrentOppiaine,
              activeSection: activeSection,
              $oppiaineOrder: Utils.oppiaineSort
            }, {
              id: 'sisallot',
              title: 'oppiaineen-sisallot',
              $all: true,
              items: _.map(['tehtava', 'ohjaus', 'tyotavat', 'tavoitteet'], function(item, index) {
                return { label: 'perusopetus-' + item, value: item, depth: 0, $selected: true, order: index };
              }),
              update: function(item) {
                $scope.filterSisalto[item.value] = !item.$selected;
              }
            }, {
              id: 'osaamiset',
              title: 'tavoitteiden-osaamiset',
              $all: true,
              items: _.map($scope.osaamiset, function(item) {
                return { label: item.nimi, value: item.id, depth: 0, $selected: true };
              }),
              update: function(item) {
                $scope.filterOsaamiset[item.value] = !item.$selected;
                paivitaTavoitteet();
              }
            }]
          }
        }
      ]
    };

    paivitaSivunavi();
  });
