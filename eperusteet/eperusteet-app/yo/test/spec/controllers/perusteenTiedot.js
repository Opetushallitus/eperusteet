'use strict';

describe('Controller: PerusteenTiedotCtrl', function () {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var PerusteenTiedotCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    
    scope = $rootScope.$new();
    PerusteenTiedotCtrl = $controller('PerusteenTiedotCtrl', {
      $scope: scope
    });
  }));

  it('should work', function () {
    //TODO
  });
});
