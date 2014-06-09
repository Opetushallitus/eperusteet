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
/* global $ */

angular.module('eperusteApp')
  .service('fileReader', function($q) {
    function createReader(deferred) {
      var reader = new FileReader();
      reader.onload = function() { deferred.resolve(reader.result); };
      reader.onerror = function() { deferred.reject(reader.error); };
      reader.onprogress = function() { deferred.notify(reader.readyState); };
      return reader;
    }

    function readFile(fileurl, type) {
      var deferred = $q.defer();
      var reader = createReader(deferred);
      if (type === 'binary') {
        reader.readAsBinaryString(fileurl);
      } else {
        reader.readAsText(fileurl);
      }
      return deferred.promise;
    }

    return {
      readFile: readFile
    };
  })
  .directive('fileSelect', function(fileReader) {
    return {
      templateUrl: 'views/partials/fileselect.html',
      restrict: 'E',
      link: function($scope, el, attrs) {
        $scope.flabel = attrs.flabel || 'lataa';
        function loadFile(file) {
          if (!file) {
            return;
          }

          var isRunning = true;
          var beforeSelect = $scope.$eval(attrs.beforeSelect);
          var onSelect = $scope.$eval(attrs.onSelect);

          $scope.$apply(function() {
            // Puhdistaa tiedostokentän
            var doc = document.getElementById('fileDirectiveSelectInput');
            doc.outerHTML = doc.outerHTML;
            $scope.file = file;
          });

          if (_.isFunction(onSelect)) {
            beforeSelect();
            var promise = fileReader.readFile(file, attrs.readType);
            promise.then(function(file) {
              onSelect(null, file);
            }, function(err) {
              onSelect(err, null);
            }, function() {
              isRunning = false;
            });
          }
        }

        el.bind('click', function(e) {
          if (e.target.id === 'reloadFile') {
            loadFile($scope.file);
          } else if (e.target.id === 'addFile') {
            $('#fileDirectiveSelectInput', el).click();
          }
        });

        el.bind('change', function(e) {
          loadFile(e.target.files[0]);
        });
      }
    };
  });

