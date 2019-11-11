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

import * as angular from "angular";
import _ from "lodash";

// FIXME
angular
    .module("eperusteApp")
    .service("EpImageService", function($q, Upload, SERVICE_LOC, PerusteprojektiTiedotService, PerusteenKuvat) {
        let pts = null;
        PerusteprojektiTiedotService.then(function(resolved) {
            pts = resolved;
        });

        this.getAll = function() {
            return PerusteenKuvat.query({ perusteId: pts.getPeruste().id }).$promise;
        };

        this.save = function(image) {
            const deferred = $q.defer();
            const url = SERVICE_LOC + "/perusteet/" + pts.getPeruste().id + "/kuvat";
            const data = {
                url: url,
                fields: {
                    nimi: image.name
                },
                file: image
            };

            Upload.upload(data)
                .success(deferred.resolve)
                .error(deferred.reject);
            return deferred.promise;
        };

        this.getUrl = function(image) {
            console.log('getUrl', image);
            let extension = "";
            if (image.mime) {
                const mimeParts = image.mime.split("/");
                extension = "." + (mimeParts.length === 2 ? mimeParts[1] : '');
            }

            return(SERVICE_LOC + "/perusteet/:perusteId/kuvat")
              .replace(":perusteId", "" + pts.getPeruste().id)
              + "/" + image.id + extension;
        };
    })
    .controller("EpImagePluginController", function($scope, EpImageService, Kaanna, Algoritmit, $timeout) {
        $scope.service = EpImageService;
        $scope.filtered = [];
        $scope.images = [];
        $scope.showPreview = false;
        $scope.model = {
            files: [],
            rejected: [],
            chosen: null
        };

        $scope.file = null;

        $scope.$watch("model.files[0]", function() {
            if (_.isArray($scope.model.files) && $scope.model.files.length > 0) {
                $scope.showPreview = true;
            }
        });
        $scope.$watch("model.chosen", function() {
            $scope.showPreview = false;
        });

        var callback = angular.noop;
        var setDeferred = null;

        function setChosenValue(value) {
            var found = _.find($scope.images, function(image: any) {
                return image.id === value;
            });
            $scope.model.chosen = found || null;
        }

        function doSort(items) {
            return _.sortBy(items, function(item: any) {
                return Kaanna.kaanna(item.nimi).toLowerCase();
            });
        }

        $scope.urlForImage = function(image) {
            console.log('urlForImage', image);
            return $scope.service.getUrl(image);
        };

        $scope.init = function() {
            $scope.service.getAll().then(function(res) {
                $scope.images = res;
                $scope.filtered = doSort(res);
                if (setDeferred) {
                    setChosenValue(_.cloneDeep(setDeferred));
                    setDeferred = null;
                }
            });
        };

        $scope.filterImages = function(value) {
            $scope.filtered = _.filter(doSort($scope.images), function(item: any) {
                return Algoritmit.match(value, item.nimi);
            });
        };

        // data from angular model to plugin
        $scope.registerListener = function(cb) {
            callback = cb;
        };
        $scope.$watch("model.chosen", function(value) {
            callback(value);
        });

        // data from plugin to angular model
        $scope.setValue = function(value) {
            console.log('setValue', value);
            $scope.$apply(function() {
                if (_.isEmpty($scope.images)) {
                    setDeferred = value;
                } else {
                    setChosenValue(value);
                }
            });
        };

        $scope.closeMessage = function() {
            $scope.message = null;
        };

        $scope.saveNew = function() {
            var image = $scope.model.files[0];
            $scope.service.save(image).then(
                function(res) {
                    console.log('save', res);
                    $scope.message = "epimage-plugin-tallennettu";
                    $scope.model.files = [];
                    $timeout(function() {
                        $scope.closeMessage();
                    }, 8000);
                    setDeferred = _.clone(res);
                    $scope.init();
                },
                function(res) {
                    $scope.message = res.syy || "epimage-plugin-tallennusvirhe";
                    $scope.model.files = [];
                    $timeout(function() {
                        $scope.closeMessage();
                    }, 8000);
                }
            );
        };
    })
    .filter("kuvalinkit", () => {
        return text => {
            if (_.isUndefined(text) || _.isNull(text)) {
                return "";
            }

            const tmp = angular.element("<div>" + text + "</div>");
            tmp.find("img").each(function() {
                let el = angular.element(this);
                el.wrap("<figure></figure>");
                if (el.attr("alt")) {
                    el.parent().append("<figcaption style=\"text-align: center;\">" + el.attr("alt") + "</figcaption>");
                }
            });

            return tmp.html();
        };
    });
