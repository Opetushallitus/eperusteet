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

// TODO: removeme, working local "fork" of the generictree to avoid height: 900px container div

"use strict";
/* global _, angular */

angular
    .module("eGenericTree", [])
    .directive("genericTreeNode", function($compile, $templateCache, $timeout, $animate) {
        function setContext(node, children) {
            node.$$hasChildren = !_.isEmpty(children);
            _.each(children, function(cnode) {
                cnode.$$depth = node.$$depth + 1;
                cnode.$$nodeParent = node;
            });
        }

        function getTemplate(tp, node) {
            var template = tp.template ? tp.template(node) : undefined;
            return $templateCache.get(template) || template || "<pre>{{ node | json }}</pre>";
        }

        return {
            restrict: "E",
            replace: true,
            template: "",
            scope: {
                node: "=",
                treeProvider: "=",
                uiSortableConfig: "="
            },
            controller: function($scope) {
                $scope.treeProvider.extension($scope.node, $scope);
                $scope.isHidden = function(node) {
                    return $scope.treeProvider.hidden(node);
                };
            },
            link: function(scope, element) {
                $animate.enabled(false, element);
                var node = scope.node;
                const children = scope.treeProvider.children(node);
                element.empty();
                setContext(node, children);
                var template = getTemplate(scope.treeProvider, node);
                if (children) {
                    template +=
                        '<div ui-sortable="uiSortableConfig" class="' +
                        scope.treeProvider.sortableClass(node) +
                        ' recursivetree" ng-model="children">';
                    scope.children = children;
                    scope.parentNode = node;
                    if (!_.isEmpty(children)) {
                        template +=
                            "" +
                            '<div ng-repeat="node in children">' +
                            '    <generic-tree-node node="node" ng-show="!isHidden(node)" ui-sortable-config="uiSortableConfig"' +
                            '                       tree-provider="treeProvider"></generic-tree-node>' +
                            "</div>";
                    }
                    template += "</div>";
                }

                // FIXME: More dirty hacks
                $timeout(() => {
                    element.append(template);
                    $compile(element.contents())(scope);
                });
            }
        };
    })
    .directive("genericTree", function($compile, $log, $timeout, $animate) {
        return {
            restrict: "E",
            replace: true,
            template: "",
            scope: {
                treeProvider: "=", // FIXME: Add interface
                uiSortableConfig: "=?"
            },
            controller: function($scope) {
                function run(provider) {
                    // Sane defaults
                    provider.sortableClass = provider.sortableClass || _.constant("");
                    provider.acceptDrop = provider.acceptDrop || _.constant(true);

                    $scope.tprovider = provider;
                    provider
                        .root()
                        .then(function(root) {
                            $scope.root = root;
                            return provider.children(root);
                        })
                        .then(function(children) {
                            $scope.children = children;
                        })
                        .catch(function(err) {
                            $log.error(err);
                        });
                }

                $scope.treeProvider.then(run).catch(function(err) {
                    $log.error(err);
                });
            },
            link: function(scope, element) {
                $animate.enabled(false, element);
                let isRefreshing = false;
                function refresh(tree) {
                    if (!tree || isRefreshing) {
                        return;
                    }

                    isRefreshing = true;

                    _.each(scope.children, function(child) {
                        child.$$nodeParent = undefined;
                        child.$$depth = 0;
                    });

                    scope.sortableConfig = _.merge(
                        {
                            connectWith: ".recursivetree",
                            handle: ".treehandle",
                            cursorAt: { top: 2, left: 2 },
                            helper: "clone",
                            option: "x",
                            cursor: "move",
                            delay: 100,
                            disabled: scope.tprovider.useUiSortable(),
                            tolerance: "pointer",
                            // start: function() {
                            //     setupMinHeightForAllGenericTrees();
                            // },
                            // stop: function() {
                            //     restoreParentHeightForAllGenericTrees();
                            // },
                            update: function(e, ui) {
                                // if (scope.tprovider.acceptDrop) {
                                //     const dropTarget = ui.item.sortable.droptarget;
                                //     if (dropTarget) {
                                //         const listItem = dropTarget.closest('.recursivetree');
                                //         const parentScope = listItem ? listItem.scope() : null;
                                //         const parentNode = parentScope && parentScope.node ? parentScope.node : scope.treeRoot;
                                //         if (!parentNode || !scope.tprovider.acceptDrop(ui.item.sortable.model, parentNode, e, ui)) {
                                //             ui.item.sortable.cancel();
                                //         }
                                //     }
                                // }
                            }
                            // cancel: '.ui-state-disabled'
                        },
                        scope.uiSortableConfig || {}
                    );

                    const templateEl = angular.element(
                        '<div ui-sortable="sortableConfig" class="' +
                            scope.tprovider.sortableClass(scope.root) +
                            ' recursivetree" ng-model="children">' +
                            '    <div ng-repeat="node in children">' +
                            '       <generic-tree-node node="node" ui-sortable-config="sortableConfig" tree-provider="tprovider"></generic-tree-node>' +
                            "    </div>" +
                            "</div>"
                    );
                    $compile(templateEl)(scope);
                    if (element.children().length) {
                        angular.element(element.children()[0]).replaceWith(templateEl);
                    } else {
                        element.append(templateEl);
                    }
                    // FIXME: Dirty hack
                    $timeout(() => (isRefreshing = false));
                }

                scope.$on("genericTree:refresh", function() {
                    refresh(scope.children);
                });
                scope.$watch("children", refresh);
            }
        };
    });
