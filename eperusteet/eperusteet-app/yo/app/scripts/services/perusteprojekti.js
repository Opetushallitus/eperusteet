'use strict';
/*global _*/

angular.module('eperusteApp')
  .factory('PerusteprojektiResource', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteprojektit/:id', {
      id: '@id'
    },
      {
        update: {method: 'PUT', isArray: false}
      });
  }).service('PerusteProjektiService', function() {
    
    var pp = {};
    
    function save (obj) {
      obj=obj||{};
      pp = _.merge(pp, obj);
    }
    
    function get () {
      return _.clone(pp);
    }
    
    function clean () {
      pp = {};
    }
    
    function watcher(scope, kentta) {
      scope.$watchCollection(kentta, function(temp) {
        save(temp);
        console.log('projekti', get());
      });
    }
    
    return {
      save: save,
      get: get,
      watcher: watcher,
      clean: clean
    };
  });
