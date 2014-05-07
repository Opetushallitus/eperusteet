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
  .service('Editointicatcher', function() {
    var f = function(/*osa*/) {};

    return {
      register: function(cb) {
        f = cb;
      },
      give: function(osa) {
        f(osa);
        f = function() {};
      }
    };
  })
  .factory('Editointikontrollit', function($rootScope, $q) {
    var scope = $rootScope.$new(true);
    scope.editingCallback = null;
    scope.editMode = false;

    scope.editModeDefer = $q.defer();

    this.lastModified = null;
    var additionalCallbacks = {save: [], start: [], cancel: []};

    function setEditMode(mode) {
      scope.editMode = mode;
      scope.editModeDefer = $q.defer();
      scope.editModeDefer.resolve(scope.editMode);
    }

    return {
      startEditing: function() {
        if(scope.editingCallback) {
          scope.editingCallback.edit();
          setEditMode(true);
        }
      },
      saveEditing: function() {
        if(scope.editingCallback) {
          scope.editingCallback.save();
          angular.forEach(additionalCallbacks.save, function(callback) {
            // Kutsutaan kaikkia callback listenereitä ja annetaan parametrina
            // viimeisin muutettu objecti ja tieto siitä, onko editointikontrollit ylipäätänsä
            // pääällä
            // callback(self.lastModified, scope.editingCallback !== null);
            callback(undefined, scope.editingCallback !== null);
          });
          setEditMode(false);
        }
      },
      cancelEditing: function() {
        if(scope.editingCallback) {
          scope.editingCallback.cancel();
          setEditMode(false);
        }
      },
      registerCallback: function(callback) {
        if(!callback ||
            !angular.isFunction(callback.edit) ||
            !angular.isFunction(callback.save) ||
            !angular.isFunction(callback.cancel)) {
          console.error('callback-function invalid');
          throw 'editCallback-function invalid';
        }
        scope.editingCallback = callback;
        scope.editModeDefer.resolve(scope.editMode);

//        scope.$watch('editingCallback', function() {
//          angular.forEach(callbackListeners, function(listener) {
//            // Kutsutaan kaikkia callback listenereitä ja annetaan parametrina
//            // viimeisin muutettu objecti ja tieto siitä, onko editointikontrollit ylipäätänsä
//            // pääällä
//            listener(self.lastModified, scope.editingCallback !== null);
//          });
//        });
      },
      unregisterCallback: function() {
        scope.editingCallback = null;
        setEditMode(false);

        additionalCallbacks = {save: [], start: [], cancel: []};
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
//        callbackListeners.push(callbackListener);
      },
      getEditModePromise: function() {
        return scope.editModeDefer.promise;
      },
      registerAdditionalSaveCallback: function(callback) {
        additionalCallbacks.save.push(callback);
      }
    };
});
