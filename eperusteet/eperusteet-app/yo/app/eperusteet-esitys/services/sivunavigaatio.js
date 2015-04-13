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
 angular.module('eperusteet.esitys')
  .directive('epSivunavigaatio', function ($window, $document, $timeout, $compile) {
    return {
      templateUrl: 'eperusteet-esitys/directives/sivunavi.html',
      restrict: 'AE',
      scope: {
        items: '=',
        header: '=',
        sections: '=',
        footer: '=',
        showOne: '=',
        onSectionChange: '=?'
      },
      controller: 'epSivuNaviController',
      transclude: true,
      link: function (scope, element, attrs) {
        var transcluded = element.find('#sivunavi-tc').contents();
        scope.hasTransclude = transcluded.length > 0;
        scope.disableRajaus = !_.isEmpty(attrs.disableRajaus);

        function updateFooter() {
          scope.footerContent = scope.footer ? $compile(scope.footer)(scope) : '';
          var el = element.find('#sivunavi-footer-content');
          el.empty().removeClass('has-content');
          if (scope.footer) {
            el.append(scope.footerContent).addClass('has-content');
          }
        }
        scope.$watch('footer', updateFooter);

      }
    };
  })

  .service('epSivunaviUtils', function ($state, $stateParams) {
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

    function isActive(item) {
      if (_.has(item, '$selected')) {
        return item.$selected;
      }
      if (_.isFunction(item.isActive)) {
        return item.isActive(item);
      }
      return (!_.isEmpty(item.link) && _.isArray(item.link) &&
        $state.is(item.link[0], _.extend(_.clone($stateParams), item.link[1])));
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
      item.$active = isActive(item);
      if (item.$active) {
        var parent = items[item.$parent];
        while (parent) {
          parent.$header = true;
          parent = items[parent.$parent];
        }
      }
      if (!item.$collapsed) {
        // Reveal all children of uncollapsed node
        for (i = 0; i < children.length; ++i) {
          items[children[i]].$hidden = false;
        }
      }
      item.$impHidden = false;
    }


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

    this.unCollapse = unCollapse;
    this.getChildren = getChildren;
    this.traverse = traverse;
    this.isActive = isActive;
  })

  .controller('epSivuNaviController', function ($scope, $state, Algoritmit, Utils, epSivunaviUtils,
    epEsitysSettings) {
    $scope.menuCollapsed = true;
    $scope.onSectionChange = _.isFunction($scope.onSectionChange) ? $scope.onSectionChange : angular.noop;

    $scope.search = {
      term: '',
      update: function () {
        var matchCount = 0;
        var items = $scope.items;
        if (_.isUndefined(items)) {
          var section = _.find($scope.sections, '$open');
          if (section) {
            items = section.items;
          }
        }
        _.each(items, function (item) {
          item.$matched = _.isEmpty($scope.search.term) || _.isEmpty(item.label) ? true :
            Algoritmit.match($scope.search.term, item.label);
          if (item.$matched) {
            matchCount++;
            var parent = items[item.$parent];
            while (parent) {
              parent.$matched = true;
              parent = items[parent.$parent];
            }
          }
        });
        $scope.hasResults = matchCount > 1; // root matches always
        updateModel(items);
      }
    };

    $scope.$watch('search.term', _.debounce(function () {
      $scope.$apply(function () {
        $scope.search.update();
      });
    }, 100));


    $scope.itemClasses = function (item) {
      var classes = ['level' + item.depth];
      if (item.$matched && $scope.search.term) {
        classes.push('matched');
      }
      if (item.$active) {
        classes.push('active');
      }
      if (item.$header) {
        classes.push('tekstisisalto-active-header');
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
      if (_.isArray($scope.items)) {
        doRefresh($scope.items);
      } else {
        _.each($scope.sections, function (section) {
          if (section.items) {
            doRefresh(section.items);
          }
          // hack for perusopetus sisallot
          if (section.model && _.isArray(section.model.sections) && section.model.sections.length > 1) {
            doRefresh(section.model.sections[1].items);
          }
        });
      }
    };

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
      _.each(items, function (item) {
        if (item.depth > 0) {
          item.$hidden = true;
        }
        item.$collapsed = true;
        item.$header = false;
      });
      doUncollapse = _.isUndefined(doUncollapse) ? true : doUncollapse;
      if (doUncollapse) {
        var active = _.find(items, function (item) {
          return epSivunaviUtils.isActive(item);
        });
        if (active) {
          epSivunaviUtils.unCollapse(items, active);
        }
      }
      epSivunaviUtils.traverse(items, 0);
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

    $scope.orderFn = function (item) {
      return _.isNumber(item.order) ? item.order : Utils.nameSort(item, 'label');
    };

    $scope.$on('$stateChangeStart', function () {
      $scope.menuCollapsed = true;
    });

    $scope.$on('$stateChangeSuccess', function (event, toState) {
      if (toState.name !== epEsitysSettings.perusopetusState + '.sisallot') {
        Utils.scrollTo('#ylasivuankkuri');
      }
      updateModel($scope.items);
    });

    $scope.$watch('items', function () {
      $scope.refresh();
    }, true);
    $scope.$watch('sections', function () {
      $scope.refresh();
    }, true);
  });
