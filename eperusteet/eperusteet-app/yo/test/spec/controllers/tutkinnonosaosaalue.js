'use strict';

describe('Controller: TutkinnonosaosaalueCtrl', function () {

  // load the controller's module
  beforeEach(module('eperusteApp'));

  var TutkinnonosaosaalueCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    TutkinnonosaosaalueCtrl = $controller('TutkinnonosaosaalueCtrl', {
      $scope: scope
    });
  }));

});
