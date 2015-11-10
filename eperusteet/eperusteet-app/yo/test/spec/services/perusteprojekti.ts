'use strict';

describe('Service: PerusteprojektiResource', function () {

  // load the service's module
  beforeEach(module('eperusteApp'));

  // instantiate service
  var PerusteprojektiResource;
  beforeEach(inject(function (_PerusteprojektiResource_) {
    PerusteprojektiResource = _PerusteprojektiResource_;
  }));

  it('should do something', function () {
    expect(!!PerusteprojektiResource).toBe(true);
  });

});
