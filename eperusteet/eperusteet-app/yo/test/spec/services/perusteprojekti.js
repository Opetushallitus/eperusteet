'use strict';

describe('Service: Perusteprojekti', function () {

  // load the service's module
  beforeEach(module('eperusteApp'));

  // instantiate service
  var Perusteprojekti;
  beforeEach(inject(function (_Perusteprojekti_) {
    Perusteprojekti = _Perusteprojekti_;
  }));

  it('should do something', function () {
    expect(!!Perusteprojekti).toBe(true);
  });

});
