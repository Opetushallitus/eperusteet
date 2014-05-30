'use strict';

describe('Directive: numberinput', function () {

  // load the directive's module
  beforeEach(module('eperusteApp'));

  var element,
    scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
  }));

  it('should make hidden element visible', inject(function ($compile) {
    element = angular.element('<numberinput></numberinput>');
    element = $compile(element)(scope);
    //expect(element.text()).toBe('this is the numberinput directive');
  }));
});
