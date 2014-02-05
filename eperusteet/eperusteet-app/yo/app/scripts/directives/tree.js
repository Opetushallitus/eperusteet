'use strict';
/*global _*/
/*global jQuery*/
angular.module('eperusteApp')
  .directive('tree', function() {

    function link(scope) {

        scope.getNodeType = function(node) {
            if (node.tyyppi === 'yksi') {
                return 'YKSI';
            }
            if (node.tyyppi ===  'selite') {
                return 'SELITE';
            }
            if ('osat' in node && node.osat.length > 0) {
                return 'KOOSTE';
            }
            return 'LEHTI';
        };

        scope.isInnerNode = function(node) {
            return _.contains(['YKSI', 'KOOSTE', 'SELITE'], scope.getNodeType(node));
        };

        scope.solmunOtsikkoteksti = function(node) {
            if (scope.getNodeType(node) === 'YKSI') {
                return 'Jokin seuraavista';
            }
            if ('laajuus' in node) {
                return node.otsikko + ', ' + node.laajuus;
            }
            return node.otsikko;
        };

        scope.nodeCollapsed = function(node, depth) {
            if (scope.isExplicitCollapsed(node)) {
                return true;
            }
            if (scope.isExplicitExpanded(node)) {
                return false;
            }
            if (scope.getDefaultExpanded(node, depth)) {
                return false;
            }
            return true;
        };

        scope.onNodeClick = function(el, depth, $event) {
            if (scope.nodeCollapsed(el, depth)) {
                el.collapsed = false;
            } else {
                el.collapsed = true;
            }
            $event.stopPropagation();
        };

        scope.glyphiconStyle = function(node, depth) {
            var styles = ['glyphicon'];
            if (scope.nodeCollapsed(node, depth)) {
                styles.push('glyphicon-plus');
            } else {
                styles.push('glyphicon-minus');
            }
            return styles;
        };

        scope.getDefaultExpanded = function(node, depth) {
            if (depth < 2) {
                return true;
            }
            // jos solmu on valintasolmu ja sillä on vain lehtilapsia, avataan se
            if (scope.getNodeType(node) === 'YKSI' &&
                    (_.every(node.osat, function(n) {
                        return scope.getNodeType(n) === 'LEHTI';
                    }
                    ))) {
                return true;
            }
            return false;
        };

        scope.isExplicitCollapsed = function(node) {
            return node.collapsed === true;
        };

        scope.isExplicitExpanded = function(node) {
            return node.collapsed === false;
        };

        scope.getNodeStyles = function(node) {
            var styles = [];
            if (scope.isInnerNode(node)) {
                styles.push('parent_li');
            }
            return styles;
        };
    }

    return {
      templateUrl: 'views/partials/tree.html',
      restrict: 'AE',
      transclude: false,
      scope: {
        rakenne: '=',
        baseurl: '@'
      },
      link: link
    };
  })
  .animation('.tree-branch', function() {
  return {
    beforeAddClass : function(element, className, done) {
      if(className === 'ng-hide') {
        jQuery(element).slideUp('fast', done);
      }
      else {
        done();
      }
    },
    beforeRemoveClass : function(element, className, done) {
      if(className === 'ng-hide') {
        element.css('display', 'none');
      }
      done();
    },
    removeClass :  function(element, className, done) {
      if(className === 'ng-hide') {
        jQuery(element).slideDown('fast', done);
      }
      else {
        done();
      }
    }
  };
});