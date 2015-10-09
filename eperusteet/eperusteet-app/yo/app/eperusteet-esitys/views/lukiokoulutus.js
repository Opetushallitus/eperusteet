/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */
'use strict';

angular.module('eperusteet.esitys').controller('epLukiokoulutusController',function (
              $q, $scope, $timeout, sisalto, PerusteenOsat,
              $state, $stateParams, epMenuBuilder, Utils, MurupolkuData,
              LukioOppiaineet, TermistoService, Kieli, $document, $rootScope, epLukiokoulutusStateService,
              epEsitysSettings) {
  $scope.showPreviewNote = epEsitysSettings.showPreviewNote;
  $scope.isNaviVisible = _.constant(true);
  $scope.hasContent = function (obj) {
    return _.isObject(obj) && obj.teksti && obj.teksti[Kieli.getSisaltokieli()];
  };
  var peruste = sisalto[0],
    oppiaineet = _.zipBy(sisalto[2], 'id');
  $scope.peruste = peruste;
  MurupolkuData.set({perusteId: peruste.id, perusteNimi: peruste.nimi});
  $scope.oppiaineetMap = oppiaineet;
  $scope.sisallot = _.zipBy(sisalto[3], 'id');
  $scope.valittuOppiaine = {};
  $scope.filterSisalto = {};
  $scope.filterOsaamiset = {};
  $scope.tekstisisalto = sisalto[4];
  $scope.state = epLukiokoulutusStateService.getState();
  TermistoService.setPeruste(peruste);
  $scope.naviClasses = function (item) {
    var classes = ['depth' + item.depth];
    if (item.$selected) {
      classes.push('tekstisisalto-active');
    }
    if (item.$header) {
      classes.push('tekstisisalto-active-header');
    }
    return classes;
  };

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

  $scope.$on('$stateChangeSuccess', function () {
    epLukiokoulutusStateService.setState($scope.navi);
  });

  $scope.filtterit = {
    moodi: 'sivutus',
  };

  function paivitaTavoitteet(inSisallot) {
    if ($scope.valittuOppiaine.vlks) {
      var filteritTyhjat = !inSisallot || _.all($scope.filterOsaamiset, function (v) {
          return v;
        });
      _.each($scope.valittuOppiaine.vlks.tavoitteet, function (tavoite) {
        if (filteritTyhjat || _.isEmpty(tavoite.laajattavoitteet)) {
          tavoite.$rejected = false;
        } else if (inSisallot) {
          tavoite.$rejected = _.all(tavoite.laajattavoitteet, function (lt) {
            return $scope.filterOsaamiset[lt];
          });
        }
      });
    }
    installClickHandler();
  }

  $scope.processOppiaine = function (oppiaine, inSisallot) {
    $scope.valittuOppiaine = {};
    $scope.valittuOppiaine.oppiaine = oppiaine;
    $scope.oppimaarat = epMenuBuilder.filteredOppimaarat(oppiaine, null);
    paivitaTavoitteet(inSisallot);
  };

  $scope.chooseFirstOppiaine = function (section) {
    var aineet = _.find((section || $scope.navi.sections[2]).model.sections, {id: 'oppiaineet'});
    var aine = _.find(aineet.items, {depth: 0});
    if (aine) {
      var params = {perusteId: $scope.peruste.id, oppiaineId: aine.$oppiaine.id};
      $timeout(function () {
        $state.go(epEsitysSettings.lukiokoulutusState + '.sisallot', params);
      });
    }
  };

  $scope.onSectionChange = function (section) {
    if (section.id === 'sisalto' && !section.$open) {
      $scope.chooseFirstOppiaine(section);
    }
  };

  $scope.$on('navifilters:set', function (event, value) {
    if (value.sisalto) {
      _.each($scope.navi.sections[2].model.sections[2].items, function (item) {
        item.$selected = _.isEmpty(value.sisalto) || _.contains(value.sisalto, item.value);
        $scope.filterSisalto[item.value] = !item.$selected;
      });
    }
    if (value.osaaminen) {
      _.each($scope.navi.sections[2].model.sections[3].items, function (item) {
        item.$selected = _.isEmpty(value.osaaminen) || _.contains(value.osaaminen, item.value);
        $scope.filterOsaamiset[item.value] = !item.$selected;
      });
      paivitaTavoitteet();
    }
    if (value.vlk) {
      _.each($scope.navi.sections[2].model.sections[0].items, function (vlk) {
        vlk.$selected = _.isEmpty(value.vlk) || _.contains(value.vlk, vlk.value);
      });
      epMenuBuilder.rakennaSisallotOppiaineet(oppiaineet, $scope.navi.sections[2].model.sections, selectedFilters(0));
      epLukiokoulutusStateService.setState($scope.navi);
    }
  });

  function selectedFilters(sectionId) {
    return _($scope.navi.sections[2].model.sections[sectionId].items).filter('$selected').map('value').value();
  }

  function updateSelection(sectionId) {
    var MAP = {
      0: 'vlk',
      2: 'sisalto',
      3: 'osaaminen'
    };
    var selected = selectedFilters(sectionId);
    var params = {};
    params[MAP[sectionId]] = selected;
    $state.go($state.current.name, _.extend(params, $stateParams));
  }

  $scope.navi = {
    header: 'perusteen-sisalto',
    showOne: true,
    sections: [{
      id: 'suunnitelma',
      include: 'eperusteet-esitys/views/tekstisisalto.html',
      items: epMenuBuilder.rakennaTekstisisalto($scope.tekstisisalto),
      naviClasses: $scope.naviClasses,
      title: 'yhteiset-osuudet'
    }, {
      title: 'opetuksen-sisallot',
      id: 'sisalto',
      include: 'eperusteet-esitys/views/navifilters.html',
      nollausLinkki: $state.href(epEsitysSettings.lukiokoulutusState + '.sisallot', {
        oppiaineId: '', vlk: '', valittu: '', osaaminen: '', sisalto: ''
      }),
      model: {
        sections: [{
          title: 'oppiaineet',
          id: 'oppiaineet',
          items: [],
          naviClasses: $scope.naviClasses,
          $open: true,
          include: 'eperusteet-esitys/views/oppiaineetsivunavi.html',
        }, {
          id: 'sisallot',
          title: 'oppiaineen-sisallot',
          $all: true,
          $open: true,
          items: _.map(['tehtava', 'tyotavat', 'ohjaus', 'arviointi', 'sisaltoalueet', 'tavoitteet'], function (item, index) {
            return {label: 'perusopetus-' + item, value: item, depth: 0, $selected: true, order: index};
          }),
          update: _.partial(updateSelection, 2)
        }, {
          id: 'osaamiset',
          title: 'tavoitteiden-osaamiset',
          $all: true,
          $open: true,
          items: _.map($scope.osaamiset, function (item) {
            return {label: item.nimi, value: item.id, depth: 0, $selected: true};
          }),
          update: _.partial(updateSelection, 3)
        }]
      }
    }
    ]
  };

  epMenuBuilder.rakennaSisallotOppiaineet(oppiaineet, $scope.navi.sections[2].model.sections, selectedFilters(0));
  installClickHandler();

  $timeout(function () {
    if ($state.current.name === epEsitysSettings.lukiokoulutusState) {
      var first = _($scope.navi.sections[0].items).filter(function (item) {
        return item.depth === 0;
      }).first();
      if (first) {
        $state.go('.tekstikappale', {
          tekstikappaleId: first.$osa.id,
          perusteId: $scope.peruste.id
        }, {location: 'replace'});
      }
    }
  });
})
  .controller('epLukiokoulutusTekstikappaleController', function ($scope, tekstikappale, epTekstikappaleChildResolver,
                                                                  MurupolkuData, epParentFinder) {
    $scope.tekstikappale = tekstikappale;
    MurupolkuData.set({tekstikappaleId: tekstikappale.id, tekstikappaleNimi: tekstikappale.nimi});
    $scope.lapset = epTekstikappaleChildResolver.getSisalto();
    $scope.links = {
      prev: null,
      next: null
    };

    MurupolkuData.set('parents', epParentFinder.find($scope.tekstisisalto.lapset, tekstikappale.id, true));

    function checkPrevNext() {
      var items = $scope.navi.sections[0].items;
      var me = _.findIndex(items, function (item) {
        return item.$osa && item.$osa.perusteenOsa && item.$osa.perusteenOsa.id === $scope.tekstikappale.id;
      });
      if (me === -1) {
        return;
      }
      var i = me + 1;
      var meDepth = items[me].depth;
      for (; i < items.length; ++i) {
        if (items[i].depth <= meDepth) {
          break;
        }
      }
      $scope.links.next = i < items.length && items[i].id !== 'laajaalaiset' ? items[i] : null;
      i = me - 1;
      for (; i >= 0; --i) {
        if (items[i].depth <= meDepth) {
          break;
        }
      }
      $scope.links.prev = i >= 0 && items[i].depth >= 0 ? items[i] : null;
    }

    $scope.$on('lukiokoulutus:stateSet', checkPrevNext);
    checkPrevNext();
})

.controller('epLukiokoulutusSisallotController', function($scope, oppiaine, $stateParams, $rootScope, MurupolkuData) {
  $scope.inSisallot = true;

  if (oppiaine) {
    var murupolkuParams = {
      parents: null,
      oppiaineId: oppiaine.id,
      oppiaineNimi: oppiaine.nimi
    };
    if (oppiaine._oppiaine) {
      murupolkuParams.parents = [$scope.oppiaineetMap[oppiaine._oppiaine]];
    }
    MurupolkuData.set(murupolkuParams);
  }

  function makeQueryArray(param, isNumber) {
    var arr = _.isArray(param) ? param : [param];
    return _.compact(isNumber ? _.map(arr, _.ary(parseInt, 1)) : arr);
  }

  var vlks = makeQueryArray($stateParams.vlk, true);

  $rootScope.$broadcast('navifilters:set', {
    vlk: vlks,
    sisalto: makeQueryArray($stateParams.sisalto),
    osaaminen: makeQueryArray($stateParams.osaaminen, true)
  });

  if (!oppiaine) {
    $scope.chooseFirstOppiaine();
  } else {
    $scope.processOppiaine(oppiaine, $stateParams.valittu || true);
  }
});
