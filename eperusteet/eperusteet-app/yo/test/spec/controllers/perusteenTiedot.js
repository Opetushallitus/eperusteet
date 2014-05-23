'use strict';

describe('Controller: PerusteenTiedotCtrl', function () {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var PerusteenTiedotCtrl,
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
    PerusteenTiedotCtrl = $controller('PerusteenTiedotCtrl', {
      $scope: scope,
      perusteprojektiTiedot: mockPerusteprojektiTiedot
    });
  }));

  it('should work', function () {
    //TODO
  });
});
