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

/**
 * Sivunavigaatioelementti
 * @param items lista menuelementtejä, objekti jolla avaimet:
 *  - label: näkyvä nimi joka ajetaan Kaanna-filterin läpi
 *  - depth: solmun syvyys hierarkiassa, oletuksena 0 (päätaso)
 *  - link: linkin osoite, array: [tilan nimi, tilan parametrit]
 * @param header Otsikko elementille
 * Valinnainen transclude sijoitetaan ensimmäiseksi otsikon alle.
 */
angular.module('eperusteApp')

  .directive('sivunavigaatio2', function () {
    return {
      templateUrl: 'views/partials/sivunavi2.html',
      restrict: 'AE',
      scope: {
        items: '=',
        header: '='
      },
      controller: 'SivuNaviController',
      transclude: true,
      link: function (scope, element) {
        var transcluded = element.find('#sivunavi-tc').contents();
        scope.hasTransclude = transcluded.length > 0;
      }
    };
  })

  .controller('SivuNaviController', function ($scope, $state, Algoritmit, Utils) {
    $scope.menuCollapsed = true;

    $scope.search = {
      term: '',
      update: function() {
        var matchCount = 0;
        _.each($scope.items, function(item) {
          item.$matched = _.isEmpty($scope.search.term) || _.isEmpty(item.label) ? true :
            Algoritmit.match($scope.search.term, item.label);
          if (item.$matched) {
            matchCount++;
            var parent = $scope.items[item.$parent];
            while (parent) {
              parent.$matched = true;
              parent = $scope.items[parent.$parent];
            }
          }
        });
        $scope.hasResults = matchCount > 1; // root matches always
        updateModel();
      }
    };

    $scope.$watch('search.term', $scope.search.update);

    function unCollapse(item) {
      item.$hidden = false;
      // Open up
      var parent = $scope.items[item.$parent];
      while (parent) {
        parent.$hidden = false;
        parent = $scope.items[parent.$parent];
      }
      // Open down one level
      var index = _.indexOf($scope.items, item);
      if (index > 0) {
        var children = getChildren(index);
        _.each(children, function (child) {
          $scope.items[child].$hidden = false;
        });
      }
    }

    function isActive(item) {
      if (_.isFunction(item.isActive)) {
        return item.isActive(item);
      }
      return (!_.isEmpty(item.link) && _.isArray(item.link) &&
        $state.is(item.link[0], _.extend(_.clone($state.params), item.link[1])));
    }

    $scope.itemClasses = function (item) {
      var classes = ['level' + item.depth];
      if (item.$matched && $scope.search.term) {
        classes.push('matched');
      }
      if (isActive(item)) {
        classes.push('active');
      }
      return classes;
    };

    $scope.refresh = function () {
      var levels = {};
      if ($scope.items.length && !$scope.items[0].root) {
        $scope.items.unshift({root: true, depth: -1});
      }
      _.each($scope.items, function (item, index) {
        item.depth = item.depth || 0;
        levels[item.depth] = index;
        if (_.isArray(item.link)) {
          item.href = $state.href.apply($state, item.link);
          if (item.link.length > 1) {
            // State is matched with string parameters
            _.each(item.link[1], function (value, key) {
              item.link[1][key] = value === null ? '' : ('' + value);
            });
          }
        }
        item.$parent = levels[item.depth-1] || null;
        item.$hidden = item.depth > 0;
        item.$matched = true;
      });
      updateModel();
    };

    function getChildren(index) {
      var children = [];
      var level = $scope.items[index].depth;
      index = index + 1;
      var depth = level + 1;
      for (; index < $scope.items.length && depth > level; ++index) {
        depth = $scope.items[index].depth;
        if (depth === level + 1) {
          children.push(index);
        }
      }
      return children;
    }

    function traverse(index) {
      if (index >= $scope.items.length) {
        return;
      }
      var item = $scope.items[index];
      var children = getChildren(index);
      var hidden = [];
      for (var i = 0; i < children.length; ++i) {
        traverse(children[i]);
        hidden.push($scope.items[children[i]].$hidden);
      }
      item.$leaf = hidden.length === 0;
      item.$collapsed = _.all(hidden);
      if (!item.$collapsed) {
        // Reveal all children of uncollapsed node
        for (i = 0; i < children.length; ++i) {
          $scope.items[children[i]].$hidden = false;
        }
      }
      item.$impHidden = false;
    }

    function hideNodeOrphans(index) {
      // If the parent is hidden, then the child is implicitly hidden
      var item = $scope.items[index];
      for (index++; index < $scope.items.length &&
           $scope.items[index].depth > item.depth; ++index) {
        if (!$scope.items[index].$hidden) {
          $scope.items[index].$impHidden = true;
        }
      }
    }

    function hideOrphans() {
      for (var i = 0; i < $scope.items.length; ++i) {
        if ($scope.items[i].$collapsed) {
          hideNodeOrphans(i);
        }
      }
    }

    function updateModel(doUncollapse) {
      doUncollapse = _.isUndefined(doUncollapse) ? true : doUncollapse;
      if (doUncollapse) {
        var active = _.find($scope.items, function (item) {
          return isActive(item);
        });
        if (active) {
          unCollapse(active);
        }
      }
      traverse(0);
      hideOrphans();
    }

    $scope.toggle = function (item, $event, state) {
      if ($event) {
        $event.preventDefault();
      }
      var index = _.indexOf($scope.items, item);
      state = _.isUndefined(state) ? !item.$collapsed : state;
      if (index >= 0 && index < ($scope.items.length - 1)) {
        index = index + 1;
        while(index < $scope.items.length &&
              $scope.items[index].depth > item.depth) {
          if ($scope.items[index].depth === item.depth + 1) {
            $scope.items[index].$hidden = state;
          }
          index++;
        }
      }
      updateModel(false);
    };

    $scope.toggleSideMenu = function () {
      $scope.menuCollapsed = !$scope.menuCollapsed;
    };

    $scope.$on('$stateChangeStart', function() {
      $scope.menuCollapsed = true;
    });

    $scope.$on('$stateChangeSuccess', function() {
      Utils.scrollTo('#ylasivuankkuri');
      updateModel();
    });

    $scope.$watch('items', function () {
      $scope.refresh();
    }, true);
  })

  .directive('epHighlight', function () {
    var matcher;
    return {
      scope: {
        epHighlight: '='
      },
      restrict: 'A',
      link: function (scope, element) {
        scope.$watch('epHighlight', function (value) {
          matcher = new RegExp('('+value+')', 'i');
          var text = element.text();
          element.html(text.replace(matcher, '<strong>$1</strong>'));
        });
      }
    };
  });
