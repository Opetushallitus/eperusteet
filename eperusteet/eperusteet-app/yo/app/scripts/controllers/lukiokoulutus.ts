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
/*global jQuery*/

angular.module('eperusteApp')
  .controller('LukiokoulutussisaltoController',
  function ($scope, perusteprojektiTiedot, Algoritmit, $state, SuoritustavanSisalto, LukioKurssiService,
      LukiokoulutusService, TekstikappaleOperations, Editointikontrollit, $stateParams, Notifikaatiot, Utils) {

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
          $log, virheService) {
    $scope.sisaltoState = _.find(LukiokoulutusService.sisallot, {tyyppi: $stateParams.osanTyyppi});
    if (!$scope.sisaltoState) {
      $log.error('LukioOsalistausController osaTyyppi: '+ $stateParams.osanTyyppi);
      virheService.virhe('virhe-sivua-ei-löytynyt');
      return;
    }

    $scope.kurssit = [];
    $scope.aihekokonaisuudet = [];
    $scope.osaAlueet = [];
    $scope.isOppiaineet = function() {
      return $stateParams.osanTyyppi === LukiokoulutusService.OPPIAINEET_OPPIMAARAT;
    };

    $scope.isAihekokonaisuus = function() {
      return $stateParams.osanTyyppi === LukiokoulutusService.AIHEKOKONAISUUDET;
    };

    $scope.isOpetuksenTavoitteet = function() {
      return $stateParams.osanTyyppi === LukiokoulutusService.OPETUKSEN_YLEISET_TAVOITTEET;
    };

    $scope.isPerusOsalistaus = function() {
      return !$scope.isOppiaineet() && !$scope.isOpetuksenTavoitteet() && !$scope.isAihekokonaisuus();
    };

    if (!$scope.isOppiaineet()) {
      LukiokoulutusService.getOsat($stateParams.osanTyyppi).then(function (res) {
        $scope.osaAlueet = res;
      });
    }
    $scope.options = {};

    $scope.createUrl = function (value) {
      if ($stateParams.osanTyyppi === LukiokoulutusService.KURSSIT) {
        return $state.href('root.perusteprojekti.suoritustapa.kurssi', {
          kurssiId: value.id
        });
      }
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

  .directive('lukioOppiaineet', function() {
    return {
      templateUrl: 'views/directives/lukiokoulutus/oppiaineet.html',
      restrict: 'E',
      scope: {
      },
      controller: 'LukioOppiaineKurssiPuuController'
    };
  })
  .controller('LukioOppiaineKurssiPuuController', function($scope, $stateParams, $q, $translate,
                                                           $rootScope, $timeout, $log, Lukitus,
                                                           LukioKurssiService, LukiokoulutusService,
                                                           $state, Editointikontrollit) {
    var kurssitProvider = LukioKurssiService.listByPeruste(LukiokoulutusService.getPerusteId());
    $scope.treehelpers = {
      haku: '',
      liittamattomienHaku: '',
      defaultCollapsed: false,
      editMode: false
    };
    $scope.treeRoot = {
      id: -1,
      jnro: 1,
      root:true,
      $$collapsed: false,
      nimi: 'Juuri',
      oppimaarat: [],
      kurssit: [],
      lapset: []
    };

    function textMatch(txt, to) {
      if (!to) {
        return true;
      }
      if (!txt) {
        return false;
      }
      return txt.toLowerCase().indexOf(to.toLowerCase()) !== -1;
    }
    function matchesHaku(node, haku) {
      if (!haku) {
        return true;
      }
      var nimi = node.nimi,
          koodiArvo = node.koodiArvo;
      if (_.isObject(node.nimi)) {
        nimi = nimi[$translate.use().toLowerCase()];
      }
      return textMatch(nimi, haku) ||
            textMatch(koodiArvo, haku);
    }
    function parents(node, fn) {
      node = node.$$nodeParent;
      while (node) {
        fn(node);
        node = node.$$nodeParent;
      }
    }
    function traverse(node, ch, fn, parent) {
      if (_.isArray(node)) {
        _.each(node, function(i) {
          traverse(i, ch, fn, parent);
        });
      } else if (node) {
        fn(node, parent);
        _.each(ch(node), function(c) {
          traverse(c, ch, fn, node);
        });
      }
    }
    function updateTree(fn) {
      if (fn) {
        traverse($scope.treeRoot, _.property('lapset'), fn);
      }
    }
    function piilotaHaunPerusteella(haku) {
      return function(item) {
        $log.info('Haetaan ', item);
        item.$$hide = !matchesHaku(item, haku);
        if (!item.$$hide) {
          parents(item, function(i) {
            i.$$hide = false;
          });
        }
      };
    }
    $scope.treeHaku = function() {
      $timeout(function() {
        $log.info('Haku ', $scope.treehelpers.haku);
        updateTree(piilotaHaunPerusteella($scope.treehelpers.haku));
      });
    };
    $scope.treeLiittamattomienHaku = function() {
      $timeout(function() {
        $log.info('Liittämättömien haku ', $scope.treehelpers.liittamattomienHaku);
        _.each($scope.liittamattomatKurssit, piilotaHaunPerusteella($scope.treehelpers.liittamattomienHaku));
      });
    };

    var handleMove = function(e, ui, cb) {
      var dropTarget = ui.item.sortable.droptarget;
      if (dropTarget) {
        var listItem = dropTarget.closest('.recursivetree');
        var parentScope = listItem ? listItem.scope() : null;
        if (parentScope && parentScope.node) {
          cb(ui.item.sortable.model, parentScope.node);
        } else {
          cb(ui.item.sortable.model, $scope.treeRoot);
        }
      }
    };
    var acceptMove = function(node, to) {
      $log.info('accept move', node, 'to', to);
      return (node.dtype === 'oppiaine' && to.root && !node.koosteinen) ||
        (node.dtype === 'oppiaine' && to.dtype === 'oppiaine' &&
              to.koosteinen && !node.koosteinen) ||
        (node.dtype === 'oppiaine' && to.root) ||
        (node.dtype === 'kurssi' && to.dtype === 'oppiaine' && !to.root && !to.koosteinen);
    };
    var moved = function(node, to, index) {
      $log.info('moved', node, 'to', to);
      if (node.dtype === 'kurssi'  && to.dtype === 'oppiaine') {
        $log.info('moved kurssi to oppiaine');
        var from = node.$$nodeParent;
        node.oppiaineet.push({
          oppiaineId: to.id,
          nimi: to.nimi,
          jarjestys: index
        });
        if (from) {
          _.remove(from.kurssit, node);
        }
        to.kurssit.push(node);
      }
    };
    var removeKurssiFromOppiaine = function(node) {
      $log.info('remove', node);
      var oppiaine = node.$$nodeParent;
      node.oppiaineet = _.filter(node.oppiaineet, function(oa) {
        $log.info('oa', oa.oppiaineId, oppiaine.id);
        return oa.oppiaineId !== oppiaine.id;
      });
      $log.info('new oppiaineet', node.oppiaineet);
      _.remove(oppiaine.kurssit, node);
      _.remove(oppiaine.lapset, node);
      if (_.isEmpty(node.oppiaineet)) {
        $scope.liittamattomatKurssit.push(node);
      }
    };

    $scope.liittamattomatKurssitConfig = {
      connectWith: '.recursivetree',
      handle: '.treehandle',
      cursorAt: { top : 2, left: 2 },
      cursor: 'move',
      delay: 100,
      disabled: true,
      tolerance: 'pointer',
      update: function(e,ui) {
        handleMove(e,ui, function(from, to) {
          if (!acceptMove(from, to)) {
            $log.info('cancel source');
            ui.item.sortable.cancel();
          } else {
            moved(from, to, ui.item.sortable.index);
          }
        });
      }
    };
    $scope.activeTab = 'puu';
    $scope.selectTab = function(tab) {
      $scope.activeTab = tab;
    };
    $scope.oppiaineet = [];
    $scope.kurssit = [];
    $scope.liittamattomatKurssit = [];
    $scope.createUrl = function(node) {
      if (node.dtype === 'kurssi') {
        return $state.href('root.perusteprojekti.suoritustapa.kurssi', {
          kurssiId: node.id
        });
      } else if(node.dtype === 'oppiaine') {
        return $state.href('root.perusteprojekti.suoritustapa.lukioosaalue', {
          osanId: node.id,
          osanTyyppi: 'oppiaineet_oppimaarat',
          tabId: 0
        });
      }
    };

    $scope.kurssiTreeConfig = {
      placeholder: 'placeholder',
      cursorAt: { top : 5, left: 5 },
      update: function(e,ui) {
        handleMove(e,ui, function(from, to) {
          if (!acceptMove(from, to)) {
            if (!angular.element(e.toElement).hasClass('liittamaton-kurssi')) {
              $log.info('cancel main');
              ui.item.sortable.cancel();
            }
          } else {
            moved(from, to, ui.item.sortable.index);
          }
        });
      }
    };

    var initTree = function () {
      var oppiaineet = _.cloneDeep($scope.oppiaineet),
          kurssit = _.cloneDeep($scope.kurssit);
      $log.info('Kurssit: ', kurssit, "oppiaineet:", oppiaineet);
      $scope.treeRoot.kurssit = [];
      $scope.treeRoot.oppimaarat = oppiaineet;
      _.each(oppiaineet, function(oppiaine) {
        oppiaine.dtype = 'oppiaine';
        oppiaine.lapset = [];
      });
      _.each(kurssit, function(kurssi) {
        kurssi.$$hide = false;
        kurssi.$$collapsed = $scope.treehelpers.defaultCollapsed;
        kurssi.dtype = 'kurssi';
        kurssi.lapset = [];
      });
      $scope.treeRoot.lapset = _.union($scope.treeRoot.oppimaarat, $scope.treeRoot.kurssit);
      $scope.liittamattomatKurssit = _.cloneDeep(_.filter(kurssit, function (k) {
        return k.oppiaineet.length === 0;
      }));
      traverse($scope.treeRoot, _.property('lapset'), function (node) {
        node.$$hide = false;
        node.$$collapsed = $scope.treehelpers.defaultCollapsed;
        node.dtype = !node.oppiaineet ? 'oppiaine' : 'kurssi';
        if (node.dtype === 'oppiaine') {
          var inParent = function (oa) {
            return oa.oppiaineId === node.id;
          };
          node.kurssit =  _(kurssit)
            .filter(function (kurssi) {return _.any(kurssi.oppiaineet, inParent);})
            .map(_.cloneDeep)
            .sortBy(function(kurssi) {return _(kurssi.oppiaineet).filter(inParent).first().jarjestys;})
            .value();
        }
        node.lapset = _.union(_.sortBy(node.oppimaarat || [], _.property('jnro')), node.kurssit || []);
      });
    };

    var updateEditMode = function(editMode) {
      $scope.treehelpers.editMode = editMode;
      $scope.liittamattomatKurssitConfig.disabled = !editMode;
      $rootScope.$broadcast('genericTree:refresh');
    };

    Editointikontrollit.registerCallback({
      edit: function() {
        Lukitus.lukitseLukiorakenne().then(function() {
          updateEditMode(true);
        });
      },
      save: function() {
        LukioKurssiService.updateOppiaineKurssiStructure($scope.treeRoot,
          $scope.liittamattomatKurssit).then($state.reload);
      },
      cancel: function() {
        Lukitus.vapauta().then($state.reload);
      },
      validate: function() {
        return true;
      },
      notify: function () {
      }
    });

    $scope.toEditMode = function() {
      Editointikontrollit.startEditing();
    };

    $scope.treeOsatProvider = $q(function(resolve) {
      var templateAround = function(tmpl) {
        return '<div ng-show="!node.$$hide" class="opetussialtopuu-solmu {{node.dtype}}-solmu" ' +
          'ng-class="{ \'opetussialtopuu-solmu-paataso\': (node.$$depth === 0) }">'+tmpl+'</div>';
      };
      var treeScope = {
        root: function() {
          $log.info('Set root.');
          return $q(function (resolveRoot) {
              LukiokoulutusService.getOsat($stateParams.osanTyyppi).then(function (oppiaineet) {
                kurssitProvider.then(function (kurssit) {
                  $scope.kurssit = kurssit;
                  $scope.oppiaineet = oppiaineet;
                  initTree();
                  resolveRoot($scope.treeRoot);
                });
              });
            }
          );
        },
        hidden: function(node) {
          if (!node) {
            return true;
          }
          return node.$$hide || (node.$$nodeParent && node.$$nodeParent.$$collapsed);
        },
        template: function(n) {
          var handle = $scope.treehelpers.editMode ? '<span icon-role="drag" class="treehandle"></span>' : '',
              collapse = !$scope.treehelpers.editMode ?  '<span ng-show="node.lapset.length" ng-click="toggle(node)" class="colorbox collapse-toggle">' +
                '    <span ng-hide="node.$$collapsed" class="glyphicon glyphicon-chevron-down"></span>' +
                '    <span ng-show="node.$$collapsed" class="glyphicon glyphicon-chevron-right"></span>' +
                '</span>' : '';
          if (n.dtype === 'kurssi') {
            var remove = $scope.treehelpers.editMode ? '   <span class="remove" icon-role="remove" ng-click="removeKurssiFromOppiaine(node)"></span>' : '';
            return templateAround('<div class="puu-node kurssi-node" ng-class="{\'liittamaton\': node.oppiaineet.length === 0}">' + handle +
              '   <a ng-click="goto(node)"><span ng-bind="node.koodiArvo"></span> ' +
              '   <span ng-bind="node.nimi | kaanna"></span></a>' + remove +
              '</div>', n);
          } else {
            return templateAround('<div class="puu-node oppiaine-node">' +
              handle + collapse + '<a ng-click="goto(node)">{{ node.nimi | kaanna }}</a></div>', n);
          }
        },
        children: function(node) {
          if (!node) {
            return $q.when([]);
          }
          return $q.when(node.lapset);
        },
        extension: function(node, scope) {
          scope.toggle = function(node) {
            node.$$collapsed = !node.$$collapsed;
          };
          scope.removeKurssiFromOppiaine = function(node) {
            return removeKurssiFromOppiaine(node);
          };
          scope.goto = function(node) {
            $log.info('Goto: ', node);
            if (node.dtype === 'kurssi') {
              return $state.go('root.perusteprojekti.suoritustapa.kurssi', {
                kurssiId: node.id
              });
            } else if(node.dtype === 'oppiaine') {
              $state.go('root.perusteprojekti.suoritustapa.lukioosaalue', {
                osanId: node.id,
                osanTyyppi: 'oppiaineet_oppimaarat',
                tabId: 0
              });
            }
          };
        },
        useUiSortable: function() {
          return !$scope.treehelpers.editMode
        }
      };
      resolve(treeScope);
    });

    $scope.addOppiaine = function() {
      $state.go('root.perusteprojekti.suoritustapa.lukioosaalue', {
        osanId: 'uusi',
        osanTyyppi: 'oppiaineet_oppimaarat',
        tabId: 0
      });
    };
    $scope.addKurssi = function() {
      $state.go('root.perusteprojekti.suoritustapa.lisaaLukioKurssi');
    };
  });
