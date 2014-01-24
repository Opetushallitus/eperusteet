'use strict';

describe('Controller: TutkinnonosaCtrl', function () {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var TutkinnonosaCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    TutkinnonosaCtrl = $controller('TutkinnonosaCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
