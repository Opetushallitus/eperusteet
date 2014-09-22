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
/* global _ */

angular.module('eperusteApp')
  .service('Navigaatiopolku', function($rootScope, $state, YleinenData, Kaanna, PerusteProjektiService) {
    var naviElementit = {};
    var naviPolku = [];
    var params = {};
    var state = {};

    function haeTaiUndef(olio) {
      for (var i = 1; i < arguments.length; ++i) {
        if (_.isObject(olio) && olio[arguments[i]]) {
          olio = olio[arguments[i]];
        }
        else {
          return null;
        }
      }
      return olio;
    }

    function updateTitle() {
      var title = Kaanna.kaanna('ePerusteet') + ': ' +
        (naviPolku.length === 0 ? Kaanna.kaanna('Etusivu') :  Kaanna.kaanna(_.last(naviPolku).arvo));
      angular.element('head > title').html(title);
    }

    function päivitä() {
      params.suoritustapa = PerusteProjektiService.getSuoritustapa() || 'naytto';
      if (!state.name) {
        return;
      }
      naviPolku = _(state.name.split('.'))
        .difference(YleinenData.naviOmit)
        .map(function(el) {
          var url = haeTaiUndef(naviElementit, el, 'url');
          if (url) { url = $state.href(url, params); }
          return {
            arvo: haeTaiUndef(naviElementit, el, 'nimi') || el,
            kentta: el,
            url: url || ''
          };
        })
        .value();
      updateTitle();
      $rootScope.$broadcast('update:navipolku');
    }

    $rootScope.$on('$stateChangeSuccess', function(event, toState, toParams) {
      state = toState;
      params = toParams;
      päivitä();
    });

    return {
      hae: function() { return _.clone(naviPolku); },
      asetaElementit: function(elementit) {
        naviElementit = _.merge(_.clone(naviElementit), elementit);
        päivitä();
      }
    };
  });
