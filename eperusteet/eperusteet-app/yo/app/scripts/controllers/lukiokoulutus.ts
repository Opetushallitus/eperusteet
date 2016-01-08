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

interface PaginationDetails {
  currentPage:number,
  showPerPage:number,
  total?:number,
  multiPage?:boolean,
  changePage:(to:number) => void
}

angular.module('eperusteApp')
  .controller('LukiokoulutussisaltoController',
  function ($scope, perusteprojektiTiedot, Algoritmit, $state, SuoritustavanSisalto, LukioKurssiService,
      LukiokoulutusService, TekstikappaleOperations, Editointikontrollit, $stateParams, Notifikaatiot, Utils, YleinenData) {

    $scope.projekti = perusteprojektiTiedot.getProjekti();
    $scope.peruste = perusteprojektiTiedot.getPeruste();
    TekstikappaleOperations.setPeruste($scope.peruste);
    $scope.rajaus = '';

    $scope.tuoSisalto = SuoritustavanSisalto.tuoSisalto();
    $scope.$esitysurl = YleinenData.getPerusteEsikatseluHost() + '/lukio/' + $scope.peruste.id + '/tiedot';

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

    $scope.$watch('peruste.sisalto', function () {
      Algoritmit.kaikilleLapsisolmuille($scope.peruste.sisalto, 'lapset', function (lapsi) {
        switch (lapsi.perusteenOsa.osanTyyppi) {
          case 'tekstikappale':
            lapsi.$url = $state.href('root.perusteprojekti.suoritustapa.tekstikappale', {
                                      suoritustapa: 'lukiokoulutus',
                                      perusteenOsaViiteId: lapsi.id,
                                      versio: ''
                                      });
            break;
          case 'opetuksenyleisettavoitteet':
            lapsi.$url = $state.href('root.perusteprojekti.suoritustapa.lukioosat', {osanTyyppi: 'opetuksen_yleiset_tavoitteet'});
                break;
          case 'aihekokonaisuudet':
            lapsi.$url = $state.href('root.perusteprojekti.suoritustapa.lukioosat', {osanTyyppi: 'aihekokonaisuudet'});
            break;
          case 'lukioopetussuunnitelmarakenne':
            lapsi.$url = $state.href('root.perusteprojekti.suoritustapa.lukioosat', {osanTyyppi: 'oppiaineet_oppimaarat'});
            break;
        }
      });
    }, true);

    // TODO käytä samaa APIa kuin sivunavissa, koko sisältöpuu kerralla
    _.each(LukiokoulutusService.sisallot, function (item) {
      var data = {
        nimi: item.label,
        tyyppi: item.tyyppi,
        lapset: []
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
                                                  ProjektinMurupolkuService, Kommentit, KommentitByPerusteenOsa) {
    $scope.isOppiaine = $stateParams.osanTyyppi === LukiokoulutusService.OPPIAINEET_OPPIMAARAT;
    if( $stateParams.osanId !== 'uusi' ) {
      Kommentit.haeKommentit(KommentitByPerusteenOsa, {id: $stateParams.perusteProjektiId, perusteenOsaId: $stateParams.osanId});
    }
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
  .controller('LukioOppiaineKurssiPuuController', function($scope, $stateParams, $q, $translate, Kaanna, Kieli,
                                                           $rootScope, $timeout, $log, Lukitus, Notifikaatiot,
                                                           LukioKurssiService, LukiokoulutusService, VersionHelper,
                                                           $state, Editointikontrollit, Kommentit, KommentitBySuoritustapa) {
    $scope.osanTyyppi = $stateParams.osanTyyppi;
    Kommentit.haeKommentit(KommentitBySuoritustapa, {id: $stateParams.perusteProjektiId, suoritustapa: $scope.osanTyyppi});
    $scope.treehelpers = {
      haku: '',
      liittamattomienHaku: '',
      liitettyjenHaku: '',
      defaultCollapsed: false,
      editMode: false
    };
    var collapseFun = (defaultCollapse) => (n) => {
      if ($scope.treehelpers.editMode) {
        return defaultCollapse ? !(n.dtype == 'oppiaine' && n.koosteinen) : false;
      } else {
        return defaultCollapse;
      }
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

    $scope.dontFixVersiot = true;
    $scope.versiot = {latest: true};

    $scope.haeVersiot = function (force, cb) {
      VersionHelper.getLukioRakenneVersions($scope.versiot, null, force, cb);
    };

    $scope.vaihdaVersio = function (v) {
      $scope.versiot.hasChanged = true;
      if ($scope.versiot.chosen) {
        VersionHelper.setUrl($scope.versiot);
      }
    };

    $scope.revertCb = function (response) {
      Lukitus.vapauta();
      $scope.haeVersiot(true, function (versiot) {
        $scope.versiot.chosen = versiot[0];
        $scope.versiot.latest = true;
      });
      Notifikaatiot.onnistui('lukiorakenne-palautettu');
    };

    $scope.rakenne = null;
    $scope.versio = $stateParams.versio ? $stateParams.versio.replace('/', '') : null;
    var versioCallback = null,
      rakenneDefer = null,
      rakenneProvider = null,
      kurssitProvider  = null;
    if ($scope.versio) {
      rakenneDefer = $q.defer();
      var kurssiDefer = $q.defer();
      rakenneProvider = rakenneDefer.promise;
      kurssitProvider = kurssiDefer.promise;
      versioCallback = function(versiot) {
        var versionInt = parseInt($scope.versio, 10);
        $scope.versiot.chosen = versiot[versiot.length-versionInt];
        $scope.versiot.latest = versionInt == versiot.length;
        LukioKurssiService.getRakenneVersion($scope.versiot.chosen.numero, function(rakenne) {
          rakenneDefer.resolve(rakenne);
          kurssiDefer.resolve(rakenne.kurssit);
        });
      };
    } else {
      kurssitProvider = LukioKurssiService.listByPeruste(LukiokoulutusService.getPerusteId());
    }
    $scope.haeVersiot(true, versioCallback);

    function textMatch(txt, to) {
      if (!to) {
        return true;
      }
      if (!txt.length) {
        return false;
      }
      var words = to.toLowerCase().split(/ /);
      var found = {};
      for (var part in txt) {
        if (!txt[part]) {
          continue;
        }
        var lower = txt[part].toLowerCase();
        for (var i in words) {
          if (words[i] && lower.indexOf(words[i]) !== -1) {
            found[i] = true;
          }
        }
      }
      for (var j = 0; j < words.length; ++j) {
        if (!found[j]) {
          return false;
        }
      }
      return true;
    }
    function matchesHaku(node, haku) {
      if (!haku) {
        return true;
      }
      var nimi = node.nimi,
          koodiArvo = node.koodiArvo,
          lokalisoituKoodi = Kaanna.kaanna(node.lokalisoituKoodi);
      if (_.isObject(node.nimi)) {
        nimi = nimi[Kieli.getSisaltokieli().toLowerCase()];
      }
      return textMatch([nimi, koodiArvo, lokalisoituKoodi], haku);
    }
    function parents(node, fn) {
      node = node.$$nodeParent;
      while (node) {
        fn(node);
        node = node.$$nodeParent;
      }
    }
    function traverse(node, extractor, fn, parent?) {
      if (_.isArray(node)) {
        _.each(node, function(i) {
          traverse(i, extractor, fn, parent);
        });
      } else if (node) {
        fn(node, parent);
        _.each(extractor(node), function(c) {
          traverse(c, extractor, fn, node);
        });
      }
    }
    function traverseTree(fn) {
      if (fn) {
        traverse($scope.treeRoot, _.property('lapset'), fn);
      }
    }
    function piilotaHaunPerusteella(haku) {
      return function(item) {
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
        traverseTree(piilotaHaunPerusteella($scope.treehelpers.haku));
        initIndexes();
      });
    };
    $scope.treeLiittamattomienHaku = function() {
      $timeout(function() {
        _.each($scope.liittamattomatKurssit, piilotaHaunPerusteella($scope.treehelpers.liittamattomienHaku));
        initIndexes();
      });
    };

    $scope.treeLiitettyjenHaku = function() {
      $timeout(function() {
        _.each($scope.liitetytKurssit, piilotaHaunPerusteella($scope.treehelpers.liitettyjenHaku));
        initIndexes();
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
      if (!node) {
        return false;
      }

      var exists = false;

      //Tarkistetaan, että onko kurssi jo kyseisen oppiaineen/oppimäärän kurssi, mikäli ei siirretä oppiaineen/oppimäärän sisällä.
      // Jos on jo, siirtoa ei sallita.
      if (node.dtype === 'kurssi' && to.dtype === 'oppiaine' && !to.root && !to.koosteinen &&
        (_.isUndefined(node.$$nodeParent) || node.$$nodeParent.id !== to.id)) {
        _.each(to.kurssit, function(kurssi) {
          if( kurssi.id === node.id ) {
            exists = true;
          }
        });
      }

      return ((node.dtype === 'oppiaine' && to.root && !node.$$nodeParent) ||
        (node.dtype === 'oppiaine' && to.dtype === 'oppiaine' && to.koosteinen && !node.koosteinen && node.$$nodeParent) ||
        (node.dtype === 'kurssi' && to.dtype === 'oppiaine' && !to.root && !to.koosteinen)) && !exists;
    };

    var initIndexes = function() {
      updateIndexs($scope.liittamattomatKurssit, $scope.liittamattomatKurssitPagination);
      updateIndexs($scope.liitetytKurssit, $scope.liitetytKurssitPagination);
    };
    var updateIndexs = function (arr, pagination:PaginationDetails) {
      var i = 0, startIndex,
          endIndex;
      if (pagination) {
        startIndex = (pagination.currentPage-1) * pagination.showPerPage;
        endIndex = startIndex + pagination.showPerPage-1;
      }
      _.each(arr, function(item) {
        item.$$index = i;
        item.$$pagingShow = true;
        if (pagination) {
          item.$$pagingShow = item.$$index >= startIndex && item.$$index <= endIndex;
        }
        if (!item.$$hide) {
          i++;
        }
      });
      initPages(arr, pagination);
    };
    var changePage = function(arr, pagination:PaginationDetails, to:number):void {
      pagination.currentPage = to;
      updateIndexs(arr, pagination);
    };
    var countNotHidden = function(arr):number {
      var count:number = 0;
      _.each(arr, function(item) {
        if (!item.$$hide) {
          count++;
        }
      });
      return count;
    };
    var initPages = function(arr, pagination:PaginationDetails):void {
      if (!pagination.currentPage || pagination.currentPage < 0) {
        pagination.currentPage = 1;
      }
      if (!pagination.showPerPage || pagination.showPerPage < 0) {
        pagination.showPerPage = 10;
      }
      if (!pagination.changePage) {
        pagination.changePage = function(pageNum:number) { changePage(arr, pagination, pageNum);};
      }
      pagination.total = countNotHidden(arr);
      pagination.multiPage = pagination.total  > pagination.showPerPage;
      if (pagination.total > 0 && pagination.total <= (pagination.currentPage - 1) * pagination.showPerPage) {
        changePage(arr, pagination, pagination.currentPage - 1);
      }
    };
    var moved = function(node, to, index) {
      if (node.dtype === 'kurssi'  && to.dtype === 'oppiaine') {
        var from = node.$$nodeParent;
        node.oppiaineet.push({
          oppiaineId: to.id,
          nimi: to.nimi,
          jarjestys: index
        });
        if (from) {
          _.remove(node.oppiaineet, function(oa) {
            return oa.oppiaineId === from.id;
          });
          _.remove(from.kurssit, node);
        } else {
          $scope.liitetytKurssit.push(_.cloneDeep(node));
        }
        to.kurssit.push(node);
      }
      initIndexes();
    };
    var removeKurssiFromOppiaine = function(node) {
      $rootScope.$broadcast('genericTree:beforeChange');
      var oppiaine = node.$$nodeParent;
      node.oppiaineet = _.filter(node.oppiaineet, function(oa) {
        return oa.oppiaineId !== oppiaine.id;
      });
      _.remove(oppiaine.kurssit, node);
      _.remove(oppiaine.lapset, node);
      var foundInTree = false,
        inLiittamattomat = _.filter($scope.liittamattomatKurssit,
          function(liittamaton) {return liittamaton.id == node.id;});
      traverseTree(function(c) {
        if (c.dtype == 'kurssi' && c.id == node.id) {
          foundInTree = true;
        }
      });
      if (!foundInTree && _.isEmpty(inLiittamattomat)) {
        $scope.liittamattomatKurssit.push(node);
        _.remove($scope.liitetytKurssit, function(liitetty) {
          return liitetty.id == node.id;
        });
      }
      initIndexes();
      $timeout(() => {
        $rootScope.$broadcast('genericTree:afterChange');
      });
    };

    $scope.oppiaineet = [];
    $scope.kurssit = [];
    $scope.liittamattomatKurssit = [];
    $scope.liittamattomatKurssitPagination = <PaginationDetails>{showPerPage: 5, currentPage: 1};
    $scope.liitetytKurssit = [];
    $scope.liitetytKurssitPagination = <PaginationDetails>{showPerPage: 5, currentPage: 1};

    $scope.gotoNode = function(node) {
      if (node.dtype === 'kurssi' || !node.dtype) {
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
    $scope.nodeHref = function(node) {
      if (node.dtype === 'kurssi' || !node.dtype) {
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
      return null;
    };
    var setCollapseForAll = function(collapse) {
      traverseTree(function(node) {
        if (collapse instanceof Function) {
          node.$$collapsed = collapse(node);
        } else {
          node.$$collapsed = collapse;
        }
      });
    };
    $scope.togglaaPolut = function () {
      $scope.treehelpers.defaultCollapsed = !$scope.treehelpers.defaultCollapsed;
      setCollapseForAll(collapseFun($scope.treehelpers.defaultCollapsed));
    };

    $scope.kurssiTreeConfig = {
      placeholder: 'placeholder',
      handle: '.treehandle',
      cursorAt: { top : 5, left: 5 },
      update: function(e,ui) {
        handleMove(e,ui, function(from, to) {
          if (acceptMove(from, to)) {
            moved(from, to, ui.item.sortable.index);
          } else {
            if (!angular.element(ui.item.context).hasClass('liittamaton-kurssi')) {
              ui.item.sortable.cancel();
            }
          }
        });
      },
      change: function(e,ui) {
        var dropTarget = e.target,
            listItem = angular.element(dropTarget).closest('.recursivetree'),
            parentScope = listItem ? listItem.scope() : null;
        if (parentScope && parentScope.node) {
          if (acceptMove(ui.item.sortable.model, parentScope.node)) {
            angular.element(dropTarget).addClass('is-draggable-into');
          } else {
            angular.element(dropTarget).removeClass('is-draggable-into');
          }
        }
      }
    };

    $scope.liittamattomatKurssitConfig = {
      placeholder: 'placeholder',
      handle: '.treehandle',
      cursorAt: { top : 2, left: 2 },
      update: function(e,ui) {
        handleMove(e,ui, function(from, to) {
          if (!acceptMove(from, to)) {
            ui.item.sortable.cancel();
          } else {
            moved(from, to, ui.item.sortable.index);
          }
        });
      }
    };

    $scope.liitetytKurssitConfig = {
      placeholder: 'placeholder',
      handle: '.treehandle',
      cursorAt: { top : 2, left: 2 },
      update: function(e,ui) {
        handleMove(e,ui, function(from, to) {
          if (!acceptMove(from, to)) {
            ui.item.sortable.cancel();
          } else {
            moved(from, to, ui.item.sortable.index);
          }
        });
      }
    };

    var initTree = function () {
      var oppiaineet = _.cloneDeep($scope.oppiaineet),
        kurssit = _.cloneDeep($scope.kurssit);
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
      traverse($scope.treeRoot, _.property('lapset'), function (node) {
        node.$$hide = false;
        node.$$collapsed = $scope.treehelpers.defaultCollapsed;
        node.dtype = !node.oppiaineet ? 'oppiaine' : 'kurssi';
        if (node.dtype === 'oppiaine') {
          node.kurssit = LukioKurssiService.filterOrderedKurssisByOppiaine(kurssit, function (oa) {
            return oa.oppiaineId === node.id;
          });
        }
        node.lapset = _.union(_.sortBy(node.oppimaarat || [], _.property('jnro')), node.kurssit || []);
      });
    };

    var updateEditMode = function(editMode) {
      $scope.treehelpers.editMode = editMode;
      $rootScope.$broadcast('genericTree:refresh');
    };

    Editointikontrollit.registerCallback({
      edit: function() {
        $scope.treehelpers.defaultCollapsed = true;
        updateEditMode(true);
        setCollapseForAll(collapseFun($scope.treehelpers.defaultCollapsed));
      },
      save: function(kommentti) {
        LukioKurssiService.updateOppiaineKurssiStructure($scope.treeRoot,
          $scope.liittamattomatKurssit, kommentti).then($state.reload);
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
      Lukitus.lukitseLukiorakenne().then(function() {
        Editointikontrollit.startEditing();
      });
    };

    function treehandleTemplate() {
      return $scope.treehelpers.editMode ? '<span icon-role="drag" class="treehandle"></span>' : '';
    }
    function timestampTemplate() {
      return !$scope.treehelpers.editMode ? '<span class="aikaleima" ng-bind="node.muokattu || 0 | aikaleima: \'ago\'" title="{{\'muokattu\' | kaanna }} {{node.muokattu || 0 | aikaleima}}"></span>' : '';
    }
    function kurssiColorbox() {
      return '  <span class="colorbox kurssi-tyyppi {{node.tyyppi.toLowerCase()}}"></span>';
    }
    function kurssiName() {
      var name = '<span ng-bind="(node.nimi | kaanna) + ((node.lokalisoituKoodi | kaanna) ? \' (\'+(node.lokalisoituKoodi | kaanna)+\')\' : \'\')" title="{{node.nimi | kaanna}} {{(node.lokalisoituKoodi | kaanna) ? \'(\'+(node.lokalisoituKoodi | kaanna)+\')\' : \'\'}}"></span>';
      if (!$scope.treehelpers.editMode) {
        name = '<a ng-href="{{createHref(node)}}">'+name+'</a>';
      }
      return name;
    }

    $scope.treeOsatProvider = $q(function(resolve) {
      var templateAround = function(tmpl) {
        return '<div class="tree-list-item" ng-show="!node.$$hide" ' +
          'ng-class="{ \'opetussialtopuu-solmu-paataso\': (node.$$depth === 0), \'bubble\': node.dtype != \'kurssi\',' +
          '           \'bubble-osa\': node.dtype === \'kurssi\',' +
          '           \'empty-item\': !node.lapset.length }">'+tmpl+'</div>';
      };
      resolve({
        root: function() {
          return $q(function (resolveRoot) {
            if (rakenneProvider) {
              rakenneProvider.then(function(rakenne) {
                $scope.kurssit = rakenne.kurssit;
                $scope.oppiaineet = rakenne.oppiaineet;
                initTree();
                resolveRoot($scope.treeRoot);
              });
            } else {
              LukiokoulutusService.getOsat($stateParams.osanTyyppi).then(function(oppiaineet) {
                kurssitProvider.then(function (kurssit) {
                  $scope.kurssit = kurssit;
                  $scope.oppiaineet = oppiaineet;
                  initTree();
                  resolveRoot($scope.treeRoot);
                });
              });
            }
          });
        },
        children: function(node) {
          if (!node) {
            return $q.when([]);
          }
          return $q.when(node.lapset);
        },
        hidden: function(node) {
          if (!node) {
            return true;
          }
          return node.$$hide || (node.$$nodeParent && node.$$nodeParent.$$collapsed);
        },
        template: function(n) {
          var handle = treehandleTemplate(),
              collapse = (!$scope.treehelpers.editMode || n.dtype === 'oppiaine')
                ?  '<span ng-show="node.lapset.length" ng-click="toggle(node)"' +
                '           class="colorbox collapse-toggle" ng-class="{\'suljettu\': node.$$collapsed}">' +
                '    <span ng-hide="node.$$collapsed" class="glyphicon glyphicon-chevron-down"></span>' +
                '    <span ng-show="node.$$collapsed" class="glyphicon glyphicon-chevron-right"></span>' +
                '</span>' : '',
              editTime = timestampTemplate(),
              icon = '';
          if (n.dtype === 'kurssi') {
            var remove = $scope.treehelpers.editMode ? '   <span class="remove" icon-role="remove" ng-click="removeKurssiFromOppiaine(node)"></span>' : '',
                name = kurssiName();
            return templateAround('<div class="puu-node kurssi-node" ng-class="{\'liittamaton\': node.oppiaineet.length === 0}">'+
              handle + kurssiColorbox() + editTime +
              '   <div class="node-content left" ng-class="{ \'empty-node\': !node.lapset.length }">' + name + '   </div>' + remove +
              '</div>');
          } else {
            var name = '{{ node.nimi | kaanna }}';
            if (!$scope.treehelpers.editMode) {
              name = '<a ng-href="{{createHref(node)}}" title="'+name+'">'+name+'</a>';
            }
            return templateAround('<div class="puu-node oppiaine-node">'+handle
                + collapse + icon + editTime + '<div class="node-content left" ng-class="{ \'empty-node\': !node.lapset.length }">' +
                '<strong>'+name+'</strong></div></div>');
          }
        },
        extension: function(node, scope) {
          scope.toggle = function(node) {
            node.$$collapsed = !node.$$collapsed;
          };
          scope.removeKurssiFromOppiaine = function(node) {
            return removeKurssiFromOppiaine(node);
          };
          scope.createHref = function(node) {
            return $scope.nodeHref(node);
          };
          scope.goto = function(node) {
            $scope.gotoNode(node);
          };
        },
        useUiSortable: function() {
          return !$scope.treehelpers.editMode
        }
      });
    });

    $scope.liittamattomatOsatProvider = $q(function(resolve) {
      var templateAround = function(tmpl) {
        return '<div class="liittamaton-kurssi recursivetree tree-list-item bubble-osa empty-item"' +
          '          ng-show="!node.$$hide && node.$$pagingShow">'+tmpl+'</div>';
      };
      resolve({
        root:function() {
          return $q(function (resolveRoot) {
            kurssitProvider.then(function (kurssit) {
              $scope.liittamattomatKurssit = _.cloneDeep(_.filter(kurssit, function (k) {
                return k.oppiaineet.length === 0;
              }));
              _.each($scope.liittamattomatKurssit, function(kurssi) {
                kurssi.dtype = 'kurssi';
              });
              resolveRoot({
                lapset: $scope.liittamattomatKurssit
              });
              updateIndexs($scope.liittamattomatKurssit, $scope.liittamattomatKurssitPagination);
            });
          });
        },
        children: function(node) {
          if (!node) {
            return $q.when([]);
          }
          return $q.when(node.lapset || []);
        },
        hidden: function(node) {
          if (!node) {
            return true;
          }
          return node.$$hide  || !node.$$pagingShow;
        },
        template: function(n) {
          return templateAround('<div class="puu-node kurssi-node">' + treehandleTemplate() +
            kurssiColorbox() + timestampTemplate() +
            '   <div class="node-content left">' + kurssiName() + '</div>' +
            '</div>');
        },
        useUiSortable: function() {
          return !$scope.treehelpers.editMode
        },
        extension: function(node, scope) {
          scope.createHref = function(node) {
            return $scope.nodeHref(node);
          };
          scope.goto = function(node) {
            $scope.gotoNode(node);
          };
        }
      });
    });

    $scope.liitetytOsatProvider = $q(function(resolve) {
      var templateAround = function(tmpl) {
        return '<div class="liittamaton-kurssi recursivetree tree-list-item bubble-osa empty-item"' +
          '          ng-show="!node.$$hide && node.$$pagingShow">'+tmpl+'</div>';
      };
      resolve({
        root:function() {
          return $q(function (resolveRoot) {
            kurssitProvider.then(function (kurssit) {
              $scope.liitetytKurssit = _.cloneDeep(_.filter(kurssit, function (k) {
                return k.oppiaineet.length != 0;
              }));
              _.each($scope.liitetytKurssit, function(kurssi) {
                kurssi.dtype = 'kurssi';
              });
              resolveRoot({
                lapset: $scope.liitetytKurssit
              });
              updateIndexs($scope.liitetytKurssit, $scope.liitetytKurssitPagination);
            });
          });
        },
        children: function(node) {
          if (!node) {
            return $q.when([]);
          }
          return $q.when(node.lapset || []);
        },
        hidden: function(node) {
          if (!node) {
            return true;
          }
          return node.$$hide || !node.$$pagingShow;
        },
        template: function(n) {
          return templateAround('<div class="puu-node kurssi-node">' + treehandleTemplate() +
            kurssiColorbox() + timestampTemplate() +
            '   <div class="node-content left">' + kurssiName() + '</div>' +
            '</div>');
        },
        useUiSortable: function() {
          return !$scope.treehelpers.editMode
        },
        extension: function(node, scope) {
          scope.goto = function(node) {
            $scope.gotoNode(node);
          };
          scope.createHref = function(node) {
            return $scope.nodeHref(node);
          };
        }
      });
    });

    $scope.addOppiaine = function() {
      $state.go('root.perusteprojekti.suoritustapa.lukioosaalue', {
        osanId: 'uusi',
        osanTyyppi: 'oppiaineet_oppimaarat',
        tabId: 0,
        editEnabled: true
      });
    };
    $scope.addKurssi = function() {
      $state.go('root.perusteprojekti.suoritustapa.lisaaLukioKurssi');
    };
  });
