'use strict';

angular.module('eperusteApp')
  .service('Muokkaustila', function Muokkaustila($rootScope) {
    
    var editoimassa = false;
    
    $rootScope.$on('enableEditing', function () {
      editoimassa = true;
    });
    
    $rootScope.$on('disableEditing', function () {
      editoimassa = false;
    });
    
    this.isEditoimassa = function () {
      return editoimassa;
    };

  });
