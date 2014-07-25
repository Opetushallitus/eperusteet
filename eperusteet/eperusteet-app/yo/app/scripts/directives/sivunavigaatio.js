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
 * Perusteprojektin sivunavigaatioelementti
 */
angular.module('eperusteApp')
  .directive('sivunavigaatio', function() {
    return {
      templateUrl: 'views/partials/sivunavigaatio.html',
      restrict: 'E',
      transclude: true,
      controller: 'sivunavigaatioCtrl'
    };
  })

  .directive('subnavi', function ($compile, $location) {
    return {
      templateUrl: 'views/partials/subnavi.html',
      restrict: 'A',
      scope: {
        subnavi: '='
      },
      controller: function ($scope, SubnaviState, $state) {
        $scope.sivunaviopen = SubnaviState.open;
        $scope.toggle = function (id, event) {
          $scope.sivunaviopen[id] = !$scope.sivunaviopen[id];
          event.stopPropagation();
        };
        $scope.openFor = function (id) {
          $scope.sivunaviopen[id] = true;
        };
        $scope.isRouteActive = function (id) {
          // ui-sref-active doesn't work directly in ui-router 0.2.*
          // with optional parameters.
          // Versionless url should be considered same as specific version url.
          var url = $state.href('root.perusteprojekti.suoritustapa.perusteenosa', {
            perusteenOsaId: id,
            versio: null
          }, {inherit:true}).replace(/#/g, '');
          return $location.url().indexOf(url) > -1;
        };
      },
      compile: function(tElement) {
        var contents = tElement.contents().remove();
        var compiledContents;
        return function(scope, iElement) {
          if(!compiledContents) {
            compiledContents = $compile(contents);
          }
          compiledContents(scope, function(clone) {
            iElement.append(clone);
          });
        };
      }
    };
  })
  .service('SubnaviState', function () {
    this.init = function () {
      this.open = {};
    };
    this.init();
  })
  .controller('sivunavigaatioCtrl', function($rootScope, $scope, $stateParams, $state, SivunavigaatioService, PerusteProjektiService) {
    var lastParams = {};
    $scope.menuCollapsed = true;
    $scope.data = {};
    $scope.piilota = true;

    function onStateChange(params) {
      $scope.suoritustapa = $stateParams.suoritustapa;
      function load() {
        $scope.data = SivunavigaatioService.getData();
      }

      if (_.size(params) !== _.size(lastParams)) {
        load();
      }
      else {
        var isSame = _.isObject(params) && _.isObject(lastParams);
        _.forEach(params, function(v, k) {
          if (!lastParams[k] || lastParams[k] !== v) {
            isSame = false;
          }
        });
        if (!isSame) {
          load();
        }
        lastParams = params;
      }
    }
    onStateChange($stateParams);

    $scope.$on('$stateChangeStart', function() {
      $scope.menuCollapsed = true;
    });

    $scope.$on('$stateChangeSuccess', function(_1, _2, params) {
      onStateChange(params);
    });

    $scope.goBackToMain = function () {
      $state.go('root.perusteprojekti.suoritustapa.sisalto', {perusteProjektiId: $scope.projekti.id, suoritustapa: PerusteProjektiService.getSuoritustapa()}, {reload: true});
    };

    $scope.toggleSideMenu = function () {
      $scope.menuCollapsed = !$scope.menuCollapsed;
    };

    $scope.isHidden = function() {
      return $scope.data.piilota;
    };
  })
  .service('SivunavigaatioService', function ($stateParams, PerusteprojektiTiedotService) {
    var data = {
      osiot: false,
      piilota: false,
      projekti: {id: 0},
      service: null
    };

    function setData(afterCb) {
      afterCb = afterCb || angular.noop;

      function load(service) {
        data.projekti = service.getProjekti();
        data.projekti.peruste = service.getPeruste();
        data.projekti.peruste.sisalto = service.getSisalto();
      }

      if (data.service) {
          load(data.service);
          afterCb();
      } else {
        PerusteprojektiTiedotService.then(function(res) {
          data.service = res;
          load(data.service);
          afterCb();
        });
      }
    }

    /**
     * Asettaa sivunavigaation tiettyyn tilaan.
     * Suositeltu käyttöpaikka: $stateProvider.state -> onEnter
     * @param data {Object} Mahdolliset asetukset:
     *     osiot: true näyttää projektin kaikki osiot,
     *            false näyttää vain "takaisin"-linkin
     *     piilota: true piilottaa koko navigaatioelementin
     */
    this.aseta = function(config) {
      setData(function() {
        data.osiot = config.osiot || false;
        data.piilota = !!config.piilota;
      });
    };

    this.update = function () {
      data.service.alustaPerusteenSisalto($stateParams, true).then(function () {
        setData();
      });
    };

    this.getData = function() {
      return data;
    };

    function openParent(el) {
      var parentEl = el.closest('.subnavi-lapset').closest('.list-group-item').children('a');
      if (parentEl.length === 0) {
        return;
      }
      var parentId = parentEl.attr('id').split('-').pop();
      parentEl.scope().openFor(parentId);
      return parentEl;
    }

    /**
     * Open (uncollapse) the subnavi tree for given item.
     * Opens the tree up to the item and shows its children also.
     * @param {type} id Id of item in subnavi tree
     * @returns {undefined}
     */
    this.unCollapseFor = function (id) {
      var visibleEl = angular.element('#subnavi-id-'+id);
      var hiddenEl = angular.element('#subnavi-id-'+id+':hidden');
      if (visibleEl.length === 0 && hiddenEl.length === 0) {
        return;
      }
      if (hiddenEl.length > 0) {
        // Element is hidden, need to open parents
        hiddenEl.scope().openFor(id);
        var parent = openParent(hiddenEl);
        while (parent) {
          parent = openParent(parent);
        }
      } else {
        visibleEl.scope().openFor(id);
      }
    };

    function linktext(id) {
      return angular.element('#subnavi-id-'+id).text();
    }

    /**
     * Sets breadcrumb in ui-view based on given content ids
     */
    this.setCrumb = function (ids) {
      var crumbEl = angular.element('#tekstikappale-crumbs');
      ids.splice(0, 1);
      ids.reverse();
      var crumbs = _.map(ids, function (id) {
        return {name: linktext(id), id: id};
      });
      var scope = crumbEl.scope();
      if (!scope) {
        console.log('Ei pystynyt asettamaan tekstikappaleen murupolkua!');
      } else {
        scope.setCrumbs(crumbs);
      }
    };
  })

  .directive('sivunavigaatio2', function () {
    return {
      templateUrl: 'views/partials/sivunavi2.html',
      restrict: 'AE',
      scope: {
        items: '=',
        header: '@?'
      },
      controller: 'SivuNaviController',
      transclude: true,
      link: function (scope, element) {
        var transcluded = element.find('#sivunavi-tc').contents();
        scope.hasTransclude = transcluded.length > 0;
      }
    };
  })

  .controller('SivuNaviController', function ($scope, $state, Algoritmit) {
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

    $scope.itemClasses = function (item) {
      var classes = ['level' + item.depth];
      if (item.$matched && $scope.search.term) {
        classes.push('matched');
      }
      if (!_.isEmpty(item.link)) {
        if (_.isArray(item.link) &&
            $state.is(item.link[0], _.extend(_.clone($state.params), item.link[1]))) {
          classes.push('active');
        }
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
        }
        item.$parent = levels[item.depth-1] || null;
        item.$hidden = false;
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
      item.$impHidden = false;
    }

    function hideNodeOrphans(index) {
      var item = $scope.items[index];
      for (index++; index < $scope.items.length && $scope.items[index].depth > item.depth; ++index) {
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

    function updateModel() {
      traverse(0);
      hideOrphans();
    }

    $scope.toggle = function (item, state) {
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
      updateModel();
    };

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
