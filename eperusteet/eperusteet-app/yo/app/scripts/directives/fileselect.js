'use strict';
/* global _ */

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
      template: '<div>' +
                '  <input id="fileDirectiveSelectInput" type="file">' +
                '  <div ng-show="scope.err.length > 0" class="error">{{ err }}</div>' +
                '</div>',
      restrict: 'E',
      link: function($scope, el, attrs) {
        el.bind('change', function(e) {
          var onSelect = $scope.$eval(attrs.onSelect);
          var onProgress = $scope.$eval(attrs.onProgress);

          if (_.isFunction(onSelect)) {
            var promise = fileReader.readFile(e.target.files[0], attrs.readType);
            promise.then(function(file) {
              onSelect(null, file);
            }, function(err) {
              console.log(err);
              onSelect(err, null);
            }, function(notify) {
              if (_.isFunction(onProgress)) {
                onProgress(notify);
              }
            });
          }
        });
      }
    };
  });

