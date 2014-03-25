'use strict';
/*global _*/

angular.module('eperusteApp')
  .directive('tree', function($compile) {
    function swap(container, from, to) {
      if (from >= 0 && to >= 0 && from < _.size(container) && to < _.size(container)) {
        var temp = container[to];
        container[to] = container[from];
        container[from] = temp;
      }
    }

    function generoiMallista(rakenne) {
      if (rakenne.tyyppi === 'selite') {
        return '<span>' + rakenne.otsikko[0] + ' ';
      }
      return '<span><span translate>' + 'puu-tyyppi-' + rakenne.tyyppi + '</span> ';
    }

    function kaikilleRakenteille(osat, f) {
      if (!osat || !f) {
        return;
      }
      _.forEach(osat, function(r) {
        r = f(r);
        if (r.osat) {
          kaikilleRakenteille(r.osat);
        }
      });
    }

    return {
      restrict: 'AE',
      transclude: false,
      terminal: true,
      scope: {
        rakenne: '=',
        vanhempi: '='
      },
      link: function(scope, el, attrs) {
        scope.poista = function(i, a) { _.remove(a.osat, i); };
        scope.alas = function(i, a) {
          var index = a.osat.indexOf(i);
          swap(a.osat, index, index + 1);
        };
        scope.ylos = function(i, a) {
          var index = a.osat.indexOf(i);
          swap(a.osat, index, index - 1);
        };

        var kentta = '';
        if (scope.rakenne.tyyppi) {
          kentta = generoiMallista(scope.rakenne);
        } else {
          kentta = '<span>{{ rakenne.otsikko.fi }} ';
        }

        kentta +=
          '<a href="" ng-click="ylos(rakenne, vanhempi)"><span class="glyphicon glyphicon-chevron-up"></span></a>' +
          '<a href="" ng-click="alas(rakenne, vanhempi)"><span class="glyphicon glyphicon-chevron-down"></span></a>' +
          '<a href="" ng-click="poista(rakenne, vanhempi)"><span class="glyphicon glyphicon-remove"></span></a>';

        var template = '';
        if (scope.rakenne && _.isArray(scope.rakenne.osat)) {
          template =
            '<div class="panel panel-default">' +
            '<div class="panel-heading">' +
            kentta +
            '</div>' +
            '<div ng-if="!osa.collapse" class="panel-body">' +
            '<div ng-repeat="osa in rakenne.osat"><tree rakenne="osa" vanhempi="rakenne"></tree></div>' +
            '</div>' +
            '</div>' +
            '';
        } else {
          template = kentta;
        }

        var templateElement = angular.element(template);
        $compile(templateElement)(scope);
        el.replaceWith(templateElement);
      }
    };
  });
