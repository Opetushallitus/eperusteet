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

/* Sets sivunavi items active based on current state */
angular.module('eperusteet.esitys')
.service('epPerusopetusStateService', function ($state, $stateParams, epSivunaviUtils, $rootScope,
  epEsitysSettings) {
  var state = {};
  var section = null;

  function processSection(navi, index, cb) {
    section = navi.sections[index];
    if (index === 1) {
      state.vlk = true;
    }
    section.$open = true;
    _.each(section.items, function (item, index) {
      (cb || angular.noop)(item, index);
      item.$hidden = item.depth > 0;
    });
  }

  this.setState = function (navi) {
    this.state = {};
    _.each(navi.sections, function (section) {
      section.$open = false;
      _.each(section.items, function (item) {
        item.$selected = false;
        item.$header = false;
      });
    });
    section = null;
    var selected = null;
    var items = null;

    function setParentOppiaineHeader() {
      if (selected && selected.$oppiaine._oppiaine) {
        var found = _.find(items, function (item) {
          return item.$oppiaine && '' + item.$oppiaine.id === '' + selected.$oppiaine._oppiaine;
        });
        if (found) {
          found.$header = true;
        }
      }
    }

    function textCallback(item)  {
      if (item.$osa) {
        item.$selected = '' + $stateParams.tekstikappaleId === '' + item.$osa.id;
        item.$hidden = item.depth > 0;
      } else if (item.id === 'laajaalaiset') {
        item.$selected = $state.is(epEsitysSettings.perusopetusState + '.laajaalaiset');
      }
      if (item.$selected) {
        selected = item;
      }
    }

    var states = {
      laajaalaiset: {
        index: 0,
        callback: textCallback
      },
      tekstikappale: {
        index: 0,
        callback: textCallback
      },
      vuosiluokkakokonaisuus: {
        index: 1,
        callback: function (item) {
          if (item.$vkl) {
            item.$selected = '' + $stateParams.vlkId === '' + item.$vkl.id;
          }
          if (item.$selected) {
            selected = item;
          }
        }
      },
      vlkoppiaine: {
        index: 1,
        callback: function (item) {
          if (item.$vkl) {
            item.$header = '' + $stateParams.vlkId === '' + item.$vkl.id;
            parentVlkId = item.$vkl.id;
          }
          if (item.$oppiaine) {
            item.$selected = '' + $stateParams.oppiaineId === '' + item.$oppiaine.id &&
              $stateParams.vlkId === '' + parentVlkId;
          }
          if (item.$selected) {
            selected = item;
          }
        },
        actions: function () {
          items = section.items;
          setParentOppiaineHeader();
        }
      },
      sisallot: {
        index: 2,
        actions: function () {
          items = section.model.sections[1].items;
          _.each(items, function (item) {
            if (item.$oppiaine) {
              item.$selected = '' + $stateParams.oppiaineId === '' + item.$oppiaine.id;
              if (item.$selected) {
                selected = item;
              }
            }
          });
          setParentOppiaineHeader();
        }
      }
    };

    var parentVlkId = null;
    _.each(states, function (value, key) {
      if (_.endsWith($state.current.name, key)) {
        processSection(navi, value.index, value.callback || angular.noop);
        (value.actions || angular.noop)();
      }
    });

    if (selected && section) {
      var menuItems = items || section.items;
      var parent = selected.$parent;
      while (_.isNumber(parent)) {
        menuItems[parent].$header = true;
        parent = menuItems[parent].$parent;
      }
      epSivunaviUtils.unCollapse(menuItems, selected);
      epSivunaviUtils.traverse(menuItems, 0);
      $rootScope.$broadcast('perusopetus:stateSet');
    }
  };
  this.getState = function () {
    return state;
  };
});
