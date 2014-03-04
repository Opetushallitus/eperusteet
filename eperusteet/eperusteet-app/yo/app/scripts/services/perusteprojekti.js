'use strict';
/*global _*/

angular.module('eperusteApp')
  .factory('PerusteprojektiResource', function($resource, SERVICE_LOC) {
    return $resource(SERVICE_LOC + '/perusteprojekti');
  }).service('PerusteProjektiService', function() {
    
    var pp = {};
    
    function save (obj) {
      obj=obj||{};
      pp = _.merge(pp, obj);
    }
    
    function get () {
      return _.clone(pp);
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
      watcher: watcher
    };
  });
