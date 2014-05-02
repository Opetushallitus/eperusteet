'use strict';

describe('Controller: PerusteprojektiCtrl', function () {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var PerusteprojektiCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    var mockKoulutusalaService = {
      query: function() {
        queryDeferred = $q.defer();
        return {$promise: queryDeferred.promise};
      }
    };
    spyOn(mockKoulutusalaService, 'query').andCallThrough();
    
    var mockOpintoalaService = {
      query: function() {
        queryDeferred = $q.defer();
        return {$promise: queryDeferred.promise};
      }
    };
    spyOn(mockOpintoalaService, 'query').andCallThrough();

    scope = $rootScope.$new();
    PerusteprojektiCtrl = $controller('PerusteprojektiCtrl', {
      $scope: scope,
      koulutusalaService: mockKoulutusalaService,
      opintoalaService: mockOpintoalaService
    });
  }));

  it('should work', function () {
    //TODO
  });
});
