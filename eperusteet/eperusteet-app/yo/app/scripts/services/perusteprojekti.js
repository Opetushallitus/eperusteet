'use strict';
/*global _*/

angular.module('eperusteApp')
  .factory('PerusteprojektiResource', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteprojektit/:id', {
      id: '@id'
    }, {
      update: {method: 'POST', isArray: false}
    });
  }).service('PerusteProjektiService', function($rootScope) {
    var pp = {};

    function save(obj) {
      obj = obj || {};
      pp = _.merge(_.clone(pp), _.clone(obj));
    }

    function get() {
      return _.clone(pp);
    }

    function clean() {
      pp = {};
    }

    function watcher(scope, kentta) {
      scope.$watch(kentta, function(temp) {
        save(temp);
      }, true);
    }

    function update() {
      $rootScope.$broadcast('update:perusteprojekti');
    }

    return {
      save: save,
      get: get,
      watcher: watcher,
      clean: clean,
      update: update
    };
  });
