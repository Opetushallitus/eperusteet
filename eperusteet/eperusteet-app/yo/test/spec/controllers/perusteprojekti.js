'use strict';

describe('Controller: PerusteprojektiCtrl', function () {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var PerusteprojektiCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    PerusteprojektiCtrl = $controller('PerusteprojektiCtrl', {
      $scope: scope
    });
  }));

  it('should work', function () {
    //TODO
  });
});
