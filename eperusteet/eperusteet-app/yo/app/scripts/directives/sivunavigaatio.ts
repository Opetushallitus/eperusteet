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
.directive('sivunavigaatio', $compile => {
    return {
        templateUrl: 'views/partials/sivunavi2.html',
        restrict: 'AE',
        scope: {
            items: '=',
            header: '=',
            sections: '=',
            showOne: '=',
            onSectionChange: '=?'
        },
        controller: 'SivuNaviController',
        transclude: true,
        link: (scope: any, element: any) => {
            const transcluded = element.find('#sivunavi-tc').contents();
            scope.hasTransclude = transcluded.length > 0;
        }
    };
})
.controller('SivuNaviController', ($scope, $state, Algoritmit, Utils, $timeout, $stateParams) => {
    $scope.menuCollapsed = true;
    $scope.onSectionChange = _.isFunction($scope.onSectionChange) ? $scope.onSectionChange : angular.noop;

    $scope.search = {
        term: '',
        update: () => {
            let matchCount = 0;
            _.each($scope.items, item => {
                item.$matched = _.isEmpty($scope.search.term) || _.isEmpty(item.label) ? true :
                    Algoritmit.match($scope.search.term, item.label);
                if (item.$matched) {
                    matchCount++;
                    let parent = $scope.items[item.$parent];
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
        let parent = items[item.$parent];
        while (parent) {
            parent.$hidden = false;
            parent = items[parent.$parent];
        }
        // Open down one level
        const index = _.indexOf(items, item);
        if (index > 0) {
            const children = getChildren(items, index);
            _.each(children, child => {
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

    $scope.itemClasses = item => {
        const classes = ['level' + item.depth];
        if (item.$matched && $scope.search.term) {
            classes.push('matched');
        }
        if (item.$active) {
            classes.push('active');
        }
        return classes;
    };

    const doRefresh = items => {
        const levels = {};
        if (items.length && !items[0].root) {
            items.unshift({root: true, depth: -1});
        }
        _.each(items, (item, index) => {
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

    $scope.refresh = () => {
        if (_.isArray($scope.items)) {
            doRefresh($scope.items);
        } else {
            _.each($scope.sections, section => {
                if (section.items) {
                    doRefresh(section.items);
                }
            });
        }
    };

    function getChildren(items, index) {
        const children = [];
        const level = items[index].depth;
        index = index + 1;
        let depth = level + 1;
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
        const item = items[index];
        const children = getChildren(items, index);
        const hidden = [];
        let i;
        for (i = 0; i < children.length; ++i) {
            traverse(items, children[i]);
            hidden.push(items[children[i]].$hidden);
        }
        item.$leaf = hidden.length === 0;
        item.$collapsed = _.all(hidden);
        item.$active = isActive(item);
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
        const item = items[index];
        for (index++; index < items.length &&
        items[index].depth > item.depth; ++index) {
            if (!items[index].$hidden) {
                items[index].$impHidden = true;
            }
        }
    }

    function hideOrphans(items) {
        for (let i = 0; i < items.length; ++i) {
            if (items[i].$collapsed) {
                hideNodeOrphans(items, i);
            }
        }
    }

    function updateModel(items, doUncollapse = true) {
        if (!items) {
            return;
        }
        if (doUncollapse) {
            const active = _.find(items, item => {
                return isActive(item);
            });
            if (active) {
                unCollapse(items, active);
            }
        }
        traverse(items, 0);
        hideOrphans(items);
    }

    $scope.toggle = (items, item, $event, state) => {
        if ($event) {
            $event.preventDefault();
        }
        let index = _.indexOf(items, item);
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

    $scope.toggleSideMenu = () => {
        $scope.menuCollapsed = !$scope.menuCollapsed;
    };

    $scope.orderFn = item => {
        return _.isNumber(item.order) ? item.order : Utils.nameSort(item, 'label');
    };

    $scope.$on('$stateChangeStart', () => {
        $scope.menuCollapsed = true;
    });

    $scope.$on('$stateChangeSuccess', () => {
        Utils.scrollTo('#ylasivuankkuri');
        updateModel($scope.items);
    });

    $scope.$watch('items', () => {
        $scope.refresh();
    }, true);

    $scope.$watch('sections', () => {
        $scope.refresh();
    }, true);
})
.directive('epHighlight', () => {
    let matcher;
    return {
        scope: {
            epHighlight: '='
        },
        restrict: 'A',
        link: (scope: any, element: any) => {
            scope.$watch('epHighlight', value => {
                matcher = new RegExp('(' + value + ')', 'i');
                const text = element.text();
                element.html(text.replace(matcher, '<strong class="ep-match">$1</strong>'));
            });
        }
    };
});
