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
  .controller('LukiokoulutussisaltoController',
  function ($scope, perusteprojektiTiedot, Algoritmit, $state, SuoritustavanSisalto, LukioKurssiService,
      LukiokoulutusService, TekstikappaleOperations, Editointikontrollit, $stateParams, Notifikaatiot, Utils, $log) {

    $scope.projekti = perusteprojektiTiedot.getProjekti();
    $scope.peruste = perusteprojektiTiedot.getPeruste();
    TekstikappaleOperations.setPeruste($scope.peruste);
    $scope.rajaus = '';

    $scope.tuoSisalto = SuoritustavanSisalto.tuoSisalto();
    $scope.$esitysurl = $state.href('root.selaus.lukiokoulutus', {
      perusteId: $scope.peruste.id
    });

    $scope.$watch('peruste.sisalto', function () {
      if( !_.isEmpty($scope.peruste.sisalto) ) {
        Algoritmit.kaikilleLapsisolmuille($scope.peruste.sisalto, 'lapset', function () {
          $state.href('root.perusteprojekti.suoritustapa.lukioosat', {
            osanTyyppi: 'osaaminen'
          });
        });
      }
    }, true);

    $scope.datat = {
      opetus: {lapset: []},
      sisalto: perusteprojektiTiedot.getYlTiedot().sisalto
    };

    $scope.$watch('datat.opetus.lapset', function () {
      _.each($scope.datat.opetus.lapset, function (area) {
        area.$type = 'ep-parts';
        area.$url = $state.href('root.perusteprojekti.suoritustapa.lukioosat', {osanTyyppi: area.tyyppi});

        area.$orderFn = Utils.nameSort;

        Algoritmit.kaikilleLapsisolmuille(area, 'lapset', function (lapsi) {
          lapsi.$url = $state.href('root.perusteprojekti.suoritustapa.lukioosaalue', {osanTyyppi: area.tyyppi, osanId: lapsi.id, tabId: 0});
          if (lapsi.koosteinen) {
            lapsi.lapset = _.sortBy(lapsi.oppimaarat, Utils.nameSort);
          }
        });
      });
    }, true);

    // TODO käytä samaa APIa kuin sivunavissa, koko sisältöpuu kerralla
    _.each(LukiokoulutusService.sisallot, function (item) {
      var data = {
        nimi: item.label,
        tyyppi: item.tyyppi
      };
      LukiokoulutusService.getOsat(item.tyyppi, true).then(function (res) {
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

  }
)
.controller('LukioOsalistausController', function ($scope, $state, $stateParams, LukiokoulutusService,
                                                virheService, LukioKurssiService, $log, $q, $translate,
                                                $rootScope, $timeout) {
    $scope.sisaltoState = _.find(
      LukiokoulutusService.sisallot, {tyyppi: $stateParams.osanTyyppi});
    if (!$scope.sisaltoState) {
      $log.error('LukioOsalistausController osaTyyppi: '+ $stateParams.osanTyyppi);
      virheService.virhe('virhe-sivua-ei-löytynyt');
      return;
    }

    $scope.kurssit = [];
    $scope.aihekokonaisuudet = [];
    $scope.osaAlueet = [];
    var kurssitProvider = LukioKurssiService.listByPeruste($scope.peruste.id);
    $scope.isOppiaineet = function() {
      return $stateParams.osanTyyppi === LukiokoulutusService.OPPIAINEET_OPPIMAARAT;
    };

    $scope.isAihekokonaisuus = function() {
      return $stateParams.osanTyyppi === LukiokoulutusService.AIHEKOKONAISUUDET;
    };

    $scope.isOpetuksenTavoitteet = function() {
      return $stateParams.osanTyyppi === LukiokoulutusService.OPETUKSEN_YLEISET_TAVOITTEET;
    };

    if (!$scope.isOppiaineet()) {
      LukiokoulutusService.getOsat($stateParams.osanTyyppi).then(function (res) {
        $scope.osaAlueet = res;
      });
    }
    $scope.options = {};

    $scope.createUrl = function (value) {
      return $state.href('root.perusteprojekti.suoritustapa.lukioosaalue', {
        osanTyyppi: $stateParams.osanTyyppi,
        osanId: value.id,
        tabId: 0
      });
    };

    $scope.add = function () {
      if ($stateParams.osanTyyppi === LukiokoulutusService.KURSSIT) {
        $state.go('root.perusteprojekti.suoritustapa.lisaaLukioKurssi');
        return;
      }
      $state.go('root.perusteprojekti.suoritustapa.lukioosaalue', {
        osanTyyppi: $stateParams.osanTyyppi,
        osanId: 'uusi',
        tabId: 0
      });
    };

    $scope.treehelpers = {
      haku: '',
      defaultCollapsed: true
    };
    $scope.treeRoot = {
      id: -1,
      jnro: 1,
      root:true,
      $$collapsed: true,
      nimi: 'Juuri',
      oppimaarat: [],
      kurssit: []
    };

    function matchesHaku(item, haku) {
      if (!haku) {
        return true;
      }
      var val = item.nimi;
      if (_.isObject(item.nimi)) {
        val = val[$translate.use().toLowerCase()];
      }
      return val.toLowerCase().indexOf(haku.toLowerCase()) !== -1;
    }
    function parents(item, fn) {
      var p = item.$$parentNodes;
      if (p && p.length) {
        _.each(p, function(i) {
          fn(i);
          parents(i, fn);
        });
      }
    }
    function forEachProp(n, fn, props, defaults) {
      if (props && !_.isArray(props)) {
        if (_.isFunction(props)) {
          props = props(n);
        }
      }
      _.each(props || defaults, function (p) {
        if (n[p]) {
          fn(n[p]);
        }
      });
    }
    function forEachChild(n, fn, props) {
      forEachProp(n, function(e) {
        _.each(e, function(c) {
          fn(c, n);
        });
      }, props, ['oppimaarat', 'kurssit']);
    }
    function travelse(n, fn, props, parent) {
      if (_.isArray(n)) {
        _.each(n, function(i) {
          travelse(i, fn, props, parent);
        });
      } else {
        fn(n, parent);
        forEachChild(n, function(c, p) {
          travelse(c, fn, props, p);
        }, props);
      }
    }
    function updateTree(fn) {
      if (fn) {
        travelse($scope.treeRoot, fn);
      }
      $rootScope.$broadcast('genericTree:refresh');
    }
    function piilotaHaunPerusteella(item) {
      $log.info('Haetaan ', item);
      item.$$hide = !matchesHaku(item, $scope.treehelpers.haku);
      if (!item.$$hide) {
        parents(item, function(i) {
          i.$$hide = false;
        });
      }
    }
    function setParent(n, parent) {
      if (parent) {
        if (n.$$parentNodes) {
          n.$$parentNodes.push(parent);
        } else {
          n.$$parentNodes = [parent];
        }
      }
    }
    $scope.treeHaku = function() {
      $timeout(function() {
        $log.info('Haku ', $scope.treehelpers.haku);
        updateTree(piilotaHaunPerusteella);
      });
    };
    var templateAround = function(tmpl) {
      return '<div ng-show="!node.$$hide">'+tmpl+'</div>';
    };

    $scope.treeOsatProvider = $q(function(resolve) {
      var treeScope = {
        root: function() {
          $log.info('Set root.');
          var droot = $q.defer();
          LukiokoulutusService.getOsat($stateParams.osanTyyppi).then(function(oppiaineet) {
            kurssitProvider.then(function(kurssit) {
              $log.info('Kurssit: ', kurssit);
              $scope.treeRoot.oppimaarat = oppiaineet;
              $scope.treeRoot.kurssit = _.filter(kurssit, function(k) {
                return k.oppiaineet.length === 0;
              });
              travelse($scope.treeRoot, function(n, parent) {
                n.$$collapsed = $scope.treehelpers.defaultCollapsed;
                n.$$hide = false;
                setParent(n, parent);
                if (!n.oppiaineet && !n.kurssit) {
                  n.kurssit = _.filter(kurssit, function(k) {
                    return _.map(k.oppiaineet, 'oppiaineId').indexOf(n.id) !== -1;
                  });
                }
              });
              droot.resolve($scope.treeRoot);
            });
          });
          return droot.promise;
        },
        hidden: _.constant(false),
        template: function(n) {
          var commonPart = '<span class="treehandle">H</span>',
            collabsibleCommon = commonPart + '<span ng-click="toggle(node)" class="colorbox suljettu">' +
            '    <span ng-hide="node.$$collapsed" class="glyphicon glyphicon-chevron-down"></span>' +
            '    <span ng-show="node.$$collapsed" class="glyphicon glyphicon-chevron-right"></span>' +
            '</span>';
          if (n.oppiaineet) {
            if (n.oppiaineet.length === 0) {
              return templateAround('<div style="margin-left:{{ 20*node.$$depth }}px;padding:10px;margin-bottom:5px;border:1px solid yellowgreen;">' +
                commonPart + ' Liittämätön kurssi {{ node.nimi | kaanna }} {{ node.id }}</div>', n);
            }
            return templateAround('<div style="margin-left:{{ 20*node.$$depth }}px;padding:10px;margin-bottom:5px;border:1px solid yellowgreen;">' +
              commonPart + ' {{ node.nimi | kaanna }} {{ node.id }}</div>', n);
          } else {
            return templateAround('<div style="margin-left:{{ 20*node.$$depth }}px;padding:10px;margin-bottom:5px;border:1px solid black;">' +
              collabsibleCommon + '<a ng-click="goto(node)">{{ node.nimi | kaanna }}</a></div>', n);
          }
        },
        children: function(node) {
          var children = [];
          if (node.$$collapsed) {
            forEachChild(node, function(c) {
              children.push(c);
            });
          }
          $log.info('Children for ', node, ' are ', children);
          return $q.when(children);
        },
        extension: function(node, scope) {
          scope.testClick = function() {
            $log.info('hello world', node);
          };
          scope.toggle = function(node) {
            node.$$collapsed = !node.$$collapsed;
            $rootScope.$broadcast('genericTree:refresh');
          };
          scope.goto = function(node) {
            $log.info("Goto: ", node);
          };
        },
        useUiSortable: _.constant(true)
      };
      resolve(treeScope);
    });
  })
  .controller('LukioOsaAlueController', function ($scope, $q, $stateParams, LukiokoulutusService,
                                                  ProjektinMurupolkuService) {
    $scope.isOppiaine = $stateParams.osanTyyppi === LukiokoulutusService.OPPIAINEET_OPPIMAARAT;
    $scope.isAihekokonaisuus = $stateParams.osanTyyppi === LukiokoulutusService.AIHEKOKONAISUUDET;
    $scope.versiot = {latest: true};
    $scope.dataObject = LukiokoulutusService.getOsa($stateParams);
    var labels = _.invert(LukiokoulutusService.LABELS);
    ProjektinMurupolkuService.set('osanTyyppi', $stateParams.osanTyyppi, labels[$stateParams.osanTyyppi]);
    $scope.dataObject.then(function (res) {
      ProjektinMurupolkuService.set('osanId', $stateParams.osanId, res.nimi);
    });
  })


  // --------------------------------------------------------------------------------------------------------
  // Kurssit
  // --------------------------------------------------------------------------------------------------------

  .controller('LisaaLukioKurssiController', function($scope,
                                                     $state,
                                                     $q,
                                                     $stateParams,
                                                     LukiokoulutusService,
                                                     LukioKurssiService,
                                                     YleinenData,
                                                     MuokkausUtils,
                                                     Koodisto) {

    $scope.kurssityypit = [];
    function init() {
      $scope.kurssi = {
        nimi: {fi: ''},
        tyyppi: 'PAKOLLINEN',
        koodiUri: null,
        koodiArvo: null
      };
      YleinenData.lukioKurssityypit().then(function(tyypit) {
        $scope.kurssityypit = tyypit;
      });
    }
    init();

    $scope.openKoodisto = Koodisto.modaali(function(koodisto) {
      MuokkausUtils.nestedSet($scope.kurssi, 'koodiUri', ',', koodisto.koodiUri);
      MuokkausUtils.nestedSet($scope.kurssi, 'koodiArvo', ',', koodisto.koodiArvo);
    }, {
      tyyppi: function() { return 'lukionkurssit'; },
      ylarelaatioTyyppi: function() { return ''; },
      tarkista: _.constant(true)
    });

    $scope.save = function() {
      LukioKurssiService.save($scope.kurssi).then(function() {
        $scope.back();
      });
    };

    $scope.back = function() {
      $state.go('root.perusteprojekti.suoritustapa.lukioosat', {osanTyyppi: LukiokoulutusService.KURSSIT});
    };
  });
