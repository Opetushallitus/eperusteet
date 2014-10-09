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
 * @param footer Sisältö lisätään menun alapuolelle
 * Valinnainen transclude sijoitetaan ensimmäiseksi otsikon alle.
 */
angular.module('eperusteApp')

  .directive('sivunavigaatio2', function ($window, $document, $timeout, $compile) {
    var SCREEN_MD_MAX = 1200;
    return {
      templateUrl: 'views/partials/sivunavi2.html',
      restrict: 'AE',
      scope: {
        items: '=',
        header: '=',
        sections: '=',
        footer: '='
      },
      controller: 'SivuNaviController',
      transclude: true,
      link: function (scope, element) {
        var window = angular.element($window);
        var transcluded = element.find('#sivunavi-tc').contents();
        scope.hasTransclude = transcluded.length > 0;
        scope.oneAtATime = true;
        scope.footerContent = scope.footer ? $compile(scope.footer)(scope) : '';
        if (scope.footer) {
          element.find('#sivunavi-footer-content').append(scope.footerContent).addClass('has-content');
        }

        /**
         * All this just to get a divider line to expand to the bottom of the page
         */
        var refreshView = function () {
          var el = angular.element('.sivunavi-navigaatio');
          if (el.length === 0) {
            return;
          }
          var hiddenOrCollapsed = (angular.element('.sivunavi-hidden').length > 0) || window.width() < SCREEN_MD_MAX;
          var sisalto = angular.element('.ep-sisalto-inner');
          if (hiddenOrCollapsed) {
            el.height('auto').css('border-right', '0');
            sisalto.hide().show(0); // Webkit bug: force redraw
            el.hide().show(0);
          } else {
            var sisaltoHeight = sisalto.outerHeight();
            var naviElement = angular.element('.sivunavi-box');
            var windowHeight = window.innerHeight();
            var longSisalto = sisaltoHeight > windowHeight;
            var naviHeight = naviElement.height();
            // Page height might change dynamically, check if navi is too long
            var naviOverflow = naviHeight > sisaltoHeight && naviHeight > windowHeight;
            if (longSisalto && !naviOverflow) {
              el.height(sisaltoHeight - 20);
            } else if ((naviHeight > windowHeight) || (longSisalto && naviOverflow)) {
              el.height('auto');
            } else {
              el.height(Math.max(windowHeight - el.offset().top - 20, naviHeight));
            }
            el.css('border-right', '1px solid #ddd');
          }
        };
        scope.refreshView = refreshView;

        window.on('scroll resize', refreshView);
        scope.$on('update:kommentit', function () {
          $timeout(refreshView(), 1500);
        });
        scope.$on('$destroy', function () {
          window.off('scroll resize', refreshView);
        });
        $timeout(refreshView);
      }
    };
  })

  .controller('SivuNaviController', function ($scope, $state, Algoritmit, Utils, $timeout, $stateParams) {
    $scope.menuCollapsed = true;

    $scope.search = {
      term: '',
      update: function () {
        var matchCount = 0;
        _.each($scope.items, function (item) {
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
        updateModel($scope.items);
      }
    };

    $scope.$watch('search.term', $scope.search.update);

    function unCollapse(items, item) {
      item.$hidden = false;
      // Open up
      var parent = items[item.$parent];
      while (parent) {
        parent.$hidden = false;
        parent = items[parent.$parent];
      }
      // Open down one level
      var index = _.indexOf(items, item);
      if (index > 0) {
        var children = getChildren(items, index);
        _.each(children, function (child) {
          items[child].$hidden = false;
        });
      }
    }

    function isActive(item) {
      if (_.isFunction(item.isActive)) {
        return item.isActive(item);
      }
      return (!_.isEmpty(item.link) && _.isArray(item.link) &&
        $state.is(item.link[0], _.extend(_.clone($stateParams), item.link[1])));
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

    var doRefresh = function (items) {
      var levels = {};
      if (items.length && !items[0].root) {
        items.unshift({root: true, depth: -1});
      }
      _.each(items, function (item, index) {
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
        item.$parent = levels[item.depth - 1] || null;
        item.$hidden = item.depth > 0;
        item.$matched = true;
      });
      updateModel(items);
    };

    $scope.refresh = function () {
      if (!_.isUndefined($scope.items)) {
        doRefresh($scope.items);
      } else {
        _.each($scope.sections, function (section) {
          if (section.items) {
            doRefresh(section.items);
          }
        });
      }
    };

    function getChildren(items, index) {
      var children = [];
      var level = items[index].depth;
      index = index + 1;
      var depth = level + 1;
      for (; index < items.length && depth > level; ++index) {
        depth = items[index].depth;
        if (depth === level + 1) {
          children.push(index);
        }
      }
      return children;
    }

    function traverse(items, index) {
      if (index >= items.length) {
        return;
      }
      var item = items[index];
      var children = getChildren(items, index);
      var hidden = [];
      for (var i = 0; i < children.length; ++i) {
        traverse(items, children[i]);
        hidden.push(items[children[i]].$hidden);
      }
      item.$leaf = hidden.length === 0;
      item.$collapsed = _.all(hidden);
      if (!item.$collapsed) {
        // Reveal all children of uncollapsed node
        for (i = 0; i < children.length; ++i) {
          items[children[i]].$hidden = false;
        }
      }
      item.$impHidden = false;
    }

    function hideNodeOrphans(items, index) {
      // If the parent is hidden, then the child is implicitly hidden
      var item = items[index];
      for (index++; index < items.length &&
        items[index].depth > item.depth; ++index) {
        if (!items[index].$hidden) {
          items[index].$impHidden = true;
        }
      }
    }

    function hideOrphans(items) {
      for (var i = 0; i < items.length; ++i) {
        if (items[i].$collapsed) {
          hideNodeOrphans(items, i);
        }
      }
    }

    function updateModel(items, doUncollapse) {
      if (!items) {
        return;
      }
      doUncollapse = _.isUndefined(doUncollapse) ? true : doUncollapse;
      if (doUncollapse) {
        var active = _.find(items, function (item) {
          return isActive(item);
        });
        if (active) {
          unCollapse(items, active);
        }
      }
      traverse(items, 0);
      hideOrphans(items);
    }

    $scope.toggle = function (items, item, $event, state) {
      if ($event) {
        $event.preventDefault();
      }
      var index = _.indexOf(items, item);
      state = _.isUndefined(state) ? !item.$collapsed : state;
      if (index >= 0 && index < (items.length - 1)) {
        index = index + 1;
        while (index < items.length &&
          items[index].depth > item.depth) {
          if (items[index].depth === item.depth + 1) {
            items[index].$hidden = state;
          }
          index++;
        }
      }
      updateModel(items, false);
    };

    $scope.toggleSideMenu = function () {
      $scope.menuCollapsed = !$scope.menuCollapsed;
    };

    $scope.$on('$stateChangeStart', function () {
      $scope.menuCollapsed = true;
    });

    function doRefreshView() {
      $timeout(function () {
        $scope.refreshView();
      });
    }

    $scope.$on('$stateChangeSuccess', function () {
      Utils.scrollTo('#ylasivuankkuri');
      updateModel($scope.items);
      doRefreshView();
    });

    $scope.$on('enableEditing', doRefreshView);
    $scope.$on('disableEditing', doRefreshView);

    $scope.$watch('items', function () {
      $scope.refresh();
    }, true);
    $scope.$watch('sections', function () {
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
          matcher = new RegExp('(' + value + ')', 'i');
          var text = element.text();
          element.html(text.replace(matcher, '<strong>$1</strong>'));
        });
      }
    };
  });
