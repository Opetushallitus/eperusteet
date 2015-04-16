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

 angular.module('eperusteet.esitys')
 .controller('epPerusopetusController', function($q, $scope, $timeout, sisalto, PerusteenOsat,
   $state, $stateParams, epMenuBuilder, Utils, MurupolkuData,
   Oppiaineet, TermistoService, Kieli, $document, $rootScope, epPerusopetusStateService, epEsitysSettings) {
   $scope.showPreviewNote = epEsitysSettings.showPreviewNote;
   $scope.isNaviVisible = _.constant(true);
   $scope.hasContent = function (obj) {
     return _.isObject(obj) && obj.teksti && obj.teksti[Kieli.getSisaltokieli()];
   };
   var peruste = sisalto[0];
   $scope.peruste = peruste;
   MurupolkuData.set({perusteId: peruste.id, perusteNimi: peruste.nimi});
   var oppiaineet = _.zipBy(sisalto[2], 'id');
   $scope.oppiaineetMap = oppiaineet;
   $scope.osaamiset = _.zipBy(sisalto[1], 'id');
   $scope.laajaalaiset = sisalto[1];
   $scope.sisallot = _.zipBy(sisalto[3], 'id');
   $scope.vuosiluokkakokonaisuudet = _(sisalto[3]).each(function(s) { s.vuosiluokat.sort(); })
                                                  .sortBy(function(s) { return _.first(s.vuosiluokat); })
                                                  .value();
   $scope.vuosiluokkakokonaisuudetMap = _.zipBy($scope.vuosiluokkakokonaisuudet, 'id');
   $scope.valittuOppiaine = {};
   $scope.filterSisalto = {};
   $scope.filterOsaamiset = {};
   $scope.tekstisisalto = sisalto[4];
   $scope.state = epPerusopetusStateService.getState();

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
     epPerusopetusStateService.setState($scope.navi);
   });

   $scope.filtterit = {
     moodi: 'sivutus',
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

   function paivitaTavoitteet(inSisallot) {
     if ($scope.valittuOppiaine.vlks) {
       var filteritTyhjat = !inSisallot || _.all($scope.filterOsaamiset, function(v) { return v; });
       _.each($scope.valittuOppiaine.vlks.tavoitteet, function(tavoite) {
         if (filteritTyhjat || _.isEmpty(tavoite.laajattavoitteet)) {
           tavoite.$rejected = false;
         } else if (inSisallot) {
           tavoite.$rejected = _.all(tavoite.laajattavoitteet, function(lt) {
             return $scope.filterOsaamiset[lt];
           });
         }
       });
     }
     installClickHandler();
   }

   $scope.processOppiaine = function (oppiaine, vlkIds, inSisallot) {
     $scope.valittuOppiaine = {};
     $scope.valittuOppiaine.oppiaine = oppiaine;
     if (_.isEmpty(vlkIds)) {
       vlkIds = _(oppiaine.vuosiluokkakokonaisuudet).sortBy(function (item) {
         return _.first($scope.vuosiluokkakokonaisuudetMap[item._vuosiluokkaKokonaisuus].vuosiluokat);
       }).map('_vuosiluokkaKokonaisuus').value();
     }
     vlkIds = _.map(vlkIds, String);
     if (inSisallot) {
       $scope.valittuOppiaine.vuosiluokkakokonaisuudet = _.zipBy(_.filter(oppiaine.vuosiluokkakokonaisuudet, function (vlk) {
         return _.some(vlkIds, function (vlkId) {
           return '' + vlk._vuosiluokkaKokonaisuus === '' + vlkId;
         });
       }), '_vuosiluokkaKokonaisuus');
     } else {
       $scope.valittuOppiaine.vuosiluokkakokonaisuudet = _.zipBy(oppiaine.vuosiluokkakokonaisuudet, '_vuosiluokkaKokonaisuus');
     }
     var vlkId = _.contains(vlkIds, inSisallot) ? inSisallot : _.first(vlkIds);
     $scope.valittuOppiaine.vlks = $scope.valittuOppiaine.vuosiluokkakokonaisuudet[vlkId];
     if ($scope.valittuOppiaine.vlks) {
       $scope.valittuOppiaine.sisallot = $scope.sisallot[$scope.valittuOppiaine.vlks._vuosiluokkaKokonaisuus];
     }

     $scope.activeVlkId = vlkId;
     $scope.oppimaarat = epMenuBuilder.filteredOppimaarat(oppiaine, vlkIds);
     paivitaTavoitteet(inSisallot);
   };

   $scope.chooseFirstOppiaine = function (section) {
     var aineet = _.find((section || $scope.navi.sections[2]).model.sections, {id: 'oppiaineet'});
     var aine = _.find(aineet.items, {depth: 0});
     if (aine) {
       var params = {perusteId: $scope.peruste.id, oppiaineId: aine.$oppiaine.id};
       $timeout(function () {
         $state.go(epEsitysSettings.perusopetusState + '.sisallot', params);
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
       epPerusopetusStateService.setState($scope.navi);
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
         title: 'vuosiluokkakokonaisuudet',
         id: 'vlk',
         items: epMenuBuilder.rakennaVuosiluokkakokonaisuuksienSisalto($scope.vuosiluokkakokonaisuudet, oppiaineet),
         naviClasses: $scope.naviClasses,
         include: 'eperusteet-esitys/views/vlk.html',
         state: $scope.state,
       }, {
         title: 'opetuksen-sisallot',
         id: 'sisalto',
         include: 'eperusteet-esitys/views/navifilters.html',
         nollausLinkki: $state.href(epEsitysSettings.perusopetusState + '.sisallot', {
           oppiaineId: '', vlk: '', valittu: '', osaaminen: '', sisalto: ''
         }),
         model: {
           sections: [{
             $condensed: true,
             items: _.map($scope.vuosiluokkakokonaisuudet, function(kokonaisuus) {
               return { label: kokonaisuus.nimi, value: kokonaisuus.id, $selected: true };
             }),
             update: _.partial(updateSelection, 0)
           }, {
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
             items: _.map(['tehtava', 'tyotavat', 'ohjaus', 'arviointi', 'sisaltoalueet', 'tavoitteet'], function(item, index) {
               return { label: 'perusopetus-' + item, value: item, depth: 0, $selected: true, order: index };
             }),
             update: _.partial(updateSelection, 2)
           }, {
             id: 'osaamiset',
             title: 'tavoitteiden-osaamiset',
             $all: true,
             $open: true,
             items: _.map($scope.osaamiset, function(item) {
               return { label: item.nimi, value: item.id, depth: 0, $selected: true };
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
     if ($state.current.name === epEsitysSettings.perusopetusState) {
       var first = _($scope.navi.sections[0].items).filter(function (item) {
         return item.depth === 0;
       }).first();
       if (first) {
         $state.go('.tekstikappale', {tekstikappaleId: first.$osa.id, perusteId: $scope.peruste.id}, {location: 'replace'});
       }
     }
   });
 })

 .controller('epPerusopetusTekstikappaleController', function($scope, tekstikappale, epTekstikappaleChildResolver,
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

  $scope.$on('perusopetus:stateSet', checkPrevNext);
  checkPrevNext();
})

.controller('epPerusopetusVlkController', function($scope, $stateParams, Utils, MurupolkuData) {
  $scope.vlk = $scope.vuosiluokkakokonaisuudetMap[$stateParams.vlkId];
  MurupolkuData.set({vlkId: $scope.vlk.id, vlkNimi: $scope.vlk.nimi});

  $scope.vlkOrder = function (item) {
    return Utils.nameSort($scope.osaamiset[item._laajaalainenOsaaminen]);
  };
})

.controller('epPerusopetusVlkOppiaineController', function($scope, oppiaine, $stateParams, MurupolkuData) {
  $scope.vlkId = $stateParams.vlkId;
  $scope.processOppiaine(oppiaine, [$scope.vlkId]);
  var vlk = $scope.vuosiluokkakokonaisuudetMap[$scope.vlkId];
  var murupolkuParams = {
    parents: null,
    vlkId: vlk.id,
    vlkNimi: vlk.nimi,
    oppiaineId: oppiaine.id,
    oppiaineNimi: oppiaine.nimi
  };
  if (oppiaine._oppiaine) {
    murupolkuParams.parents = [$scope.oppiaineetMap[oppiaine._oppiaine]];
  }
  MurupolkuData.set(murupolkuParams);
})

.controller('epLaajaalaisetOsaamisetController', function ($scope, Utils) {
  $scope.osaaminenSort = Utils.nameSort;
})

.controller('epPerusopetusSisallotController', function($scope, oppiaine, $stateParams, $rootScope, MurupolkuData) {
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
    $scope.processOppiaine(oppiaine, vlks, $stateParams.valittu || true);
  }
});
