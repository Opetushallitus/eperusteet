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
.service('epTekstikappaleChildResolver', function (Algoritmit, $q, PerusteenOsat) {
  var lapset = null;
  this.get = function (sisalto, viiteId) {
    var promises = [];
    var viite = null;
    Algoritmit.kaikilleLapsisolmuille(sisalto, 'lapset', function (item) {
      if ('' + item.id === '' + viiteId) {
        viite = item;
        return false;
      }
    });
    if (viite) {
      Algoritmit.kaikilleLapsisolmuille(viite, 'lapset', function (lapsi) {
        lapsi.$osa = PerusteenOsat.getByViite({viiteId: lapsi.id});
        promises.push(lapsi.$osa.$promise);
      });
      lapset = viite.lapset;
    }
    return $q.all(promises);
  };
  this.getSisalto = function () {
    return lapset;
  };
})

.service('epParentFinder', function () {
  var idToMatch = null;
  var usePerusteenOsa = false;
  function matcher(node, accumulator) {
    if ((usePerusteenOsa && node.perusteenOsa && node.perusteenOsa.id === idToMatch) ||
        (!usePerusteenOsa && node.id === idToMatch)) {
      accumulator.push(node);
      return true;
    }
    var childMatch = _.some(node.lapset, function (lapsi) {
      return matcher(lapsi, accumulator);
    });
    if (childMatch) {
      accumulator.push(node);
      return true;
    }
  }

  function iterateFn(accumulator, value) {
    matcher(value, accumulator);
    return accumulator;
  }

  this.find = function (lapset, matchId, perusteenOsaIdMatch) {
    idToMatch = matchId;
    usePerusteenOsa = !!perusteenOsaIdMatch;
    var parents = _.reduce(lapset, iterateFn, []);
    return _(parents).drop(1).value();
  };
})

.directive('epTekstiotsikko', function () {
  return {
    restrict: 'E',
    scope: {
      model: '=',
      level: '@',
      linkVar: '='
    },
    template: '<span class="otsikko-wrap"><span ng-bind-html="model.$osa.nimi | kaanna | unsafe"></span>' +
    '  <span class="teksti-linkki">' +
    '    <a ng-if="amEsitys" ui-sref="^.tekstikappale({osanId: model.id})" icon-role="new-window"></a>' +
    '    <a ng-if="!amEsitys" ui-sref="^.tekstikappale({tekstikappaleId: model.id})" icon-role="new-window"></a>' +
    '  </span></span>',
    link: function (scope, element) {
      var headerEl = angular.element('<h' + scope.level + '>');
      element.find('.otsikko-wrap').wrap(headerEl);
      scope.amEsitys = scope.linkVar === 'osanId';
    }
  };
});
