'use strict';
/* global _ */

angular.module('eperusteApp')
  .service('Navigaatiopolku', function($rootScope, $state, YleinenData) {
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

    function päivitä() {
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
      $rootScope.$emit('update:navipolku');
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
