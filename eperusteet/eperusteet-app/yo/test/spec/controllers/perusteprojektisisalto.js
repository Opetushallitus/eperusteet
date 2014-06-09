'use strict';

describe('Controller: PerusteprojektisisaltoCtrl', function () {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var PerusteprojektisisaltoCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    
    var mockPerusteprojektiTiedot = {
      query: function() {
        queryDeferred = $q.defer();
        return {$promise: queryDeferred.promise};
      }, 
      getProjekti: function () {return {};},
      getPeruste: function () {return {};},
      getSisalto: function () {return {};}
    };
    spyOn(mockPerusteprojektiTiedot, 'query').andCallThrough();
    
    scope = $rootScope.$new();
    PerusteprojektisisaltoCtrl = $controller('PerusteprojektisisaltoCtrl', {
      $scope: scope,
      perusteprojektiTiedot: mockPerusteprojektiTiedot
    });
  }));

  it('should work', function () {
    //TODO
  });
});
