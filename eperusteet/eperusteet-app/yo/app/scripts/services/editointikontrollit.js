/*
* Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
* 
* This program is free software: Licensed under the EUPL, Version 1.1 or - as
* soon as they will be approved by the European Commission - subsequent versions
* of the EUPL (the "Licence");
* 
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* European Union Public Licence for more details.
*/

'use strict';

angular.module('eperusteApp')
  .factory('Editointikontrollit', function($rootScope, $q) {
    var scope = $rootScope.$new(true);
    scope.editingCallback = null;
    scope.editMode = false;
    
    scope.editModeDefer = $q.defer();
    
    return {
      startEditing: function() {
        if(scope.editingCallback) {
          scope.editingCallback.edit();
          scope.editMode = true;
          scope.editModeDefer = $q.defer();
          scope.editModeDefer.resolve(scope.editMode);
        }
      },
      saveEditing: function() {
        if(scope.editingCallback) {
          scope.editingCallback.save();
          scope.editMode = false;
          scope.editModeDefer = $q.defer();
          scope.editModeDefer.resolve(scope.editMode);
        }
      },
      cancelEditing: function() {
        if(scope.editingCallback) {
          scope.editingCallback.cancel();
          scope.editMode = false;
          scope.editModeDefer = $q.defer();
          scope.editModeDefer.resolve(scope.editMode);
        }
      },
      registerCallback: function(callback) {
        if(!callback ||
            !callback.edit ||
            !angular.isFunction(callback.edit) ||
            !callback.save ||
            !angular.isFunction(callback.save) ||
            !callback.cancel ||
            !angular.isFunction(callback.cancel)) {
          console.error('callback-function invalid');
          throw 'editCallback-function invalid';
        }
        scope.editingCallback = callback;
        scope.editModeDefer.resolve(scope.editMode);
      },
      unregisterCallback: function() {
        scope.editingCallback = null;
        scope.editMode = false;
        scope.editModeDefer = $q.defer();
        scope.editModeDefer.resolve(scope.editMode);
      },
      editingEnabled: function() {
        if(scope.editingCallback) {
          return true;
        } else {
          return false;
        }
      },
      registerCallbackListener: function(callbackListener) {
        scope.$watch('editingCallback', callbackListener);
      },
      getEditModePromise: function() {
        return scope.editModeDefer.promise;
      }
    };
});
