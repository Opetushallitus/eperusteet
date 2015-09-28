/// <reference path="../../ts_packages/tsd.d.ts" />

interface EditointiKontrollitCallbacks {
  edit: () => Promise<any>;
  save: (kommentti?: string) => Promise<any>;
  cancel: () => Promise<any>;
  notify?: (mode?) => void;
  validate?: (validator?) => Promise<any>;
}

interface EditointiKontrollitI {
  startEditing: () => Promise<any>;
  saveEditing: (kommentti: string) => Promise<any>;
  cancelEditing: (tilanvaihto?: boolean) => Promise<any>;
  registerCallback: (callback: EditointiKontrollitCallbacks) => void;
  unregisterCallback: () => void;
  editingEnabled: () => boolean;
  registerCallbackListener: (cbListener: any) => void;
  registerEditModeListener: (listener: any) => void;
  getEditModePromise: () => any;
  getEditMode: () => boolean;
  lastModified: any;
};


/**
 * Editointikontrollit
 * required callbacks:
 * - edit Called when starting to edit
 * - save Called when saving
 * - cancel Called when canceling edit mode
 * optional callbacks:
 * - notify Called when edit mode changes with boolean parameter editMode
 * - validate Called before saving, returning true results in save
 * - asyncValidate Called before save/validate, parameter is a function that
 *                 should be called on successful validation (results in save)
 * - canCancel Called before cancel, must return a promise.
 *             If promise is resolved, canceling continues.
 */
angular.module('eperusteApp')
  .factory('Editointikontrollit', function($rootScope, $q, Utils, Notifikaatiot, $timeout): EditointiKontrollitI {
    var scope = $rootScope.$new(true);
    scope.editingCallback = null;
    scope.editMode = false;

    scope.editModeDefer = $q.defer();

    this.lastModified = null;
    var cbListener = _.noop;
    var editmodeListener = null;

    function setEditMode(mode) {
      scope.editMode = mode;
      scope.editModeDefer = $q.defer();
      scope.editModeDefer.resolve(scope.editMode);
      if (scope.editingCallback) {
        scope.editingCallback.notify(mode);
      }
      if (editmodeListener) {
        editmodeListener(mode);
      }
    }

    var result: any = {};

    // Temporary checking solution for programmer sanity
    function validateEditingCallback(reject) {
      if (!scope.editingCallback) {
        console.error('You should provide editing callbacks before calling this');
        reject(_);
        throw {};
      }
    }

    result.startEditing = function() {
      return new Promise((resolve, reject) => {
        validateEditingCallback(reject);
        scope.editingCallback.edit().then(() => {
          setEditMode(true);
          $rootScope.$broadcast('enableEditing');
          resolve(_);
        });
      });
    };

    result.saveEditing = function(kommentti: string) {
      return new Promise((resolve, reject) => {
        validateEditingCallback(reject);
        $rootScope.$broadcast('editointikontrollit:preSave');
        var err;

        function mandatoryFieldValidator(fields, target) {
          err = undefined;
          let fieldsf = _.filter(fields || [], function(field) {
            return field.mandatory;
          });

          if (!target) {
            return false;
          }
          else if (_.isString(target)) {
            return !_.isEmpty(target);
          }
          else if (_.isObject(target) && !_.isEmpty(target) && !_.isEmpty(fieldsf)) {
            return _.all(fieldsf, function(field) {
              var valid = Utils.hasLocalizedText(target[field.path]);
              if (!valid) {
                err = field.mandatoryMessage;
              }
              return valid;
            });
          }
          else {
            return true;
          }
        }

        scope.editingCallback.validate(mandatoryFieldValidator)
          .then(() => {
            $rootScope.$broadcast('notifyCKEditor');
            return scope.editingCallback.save(kommentti)
          })
          .then(() => {
            $rootScope.$broadcast('notifyCKEditor');
            setEditMode(false);
            $rootScope.$broadcast('disableEditing');
          })
          .catch(() => {
            if (err) {
              Notifikaatiot.varoitus(err || 'mandatory-odottamaton-virhe');
            }
          });
      });
    };

    result.cancelEditing = function(tilanvaihto = false) {
      function doCancel() {
        setEditMode(false);
        $rootScope.$broadcast('disableEditing');
        $rootScope.$broadcast('notifyCKEditor');
      }

      return new Promise((resolve, reject) => {
        validateEditingCallback(reject);
        if (tilanvaihto) {
          doCancel();
        }
        else {
          scope.editingCallback.cancel().then(doCancel).catch(_.noop);
        }
      });
    };

    result.registerCallback = function(callback: EditointiKontrollitCallbacks) {
      callback.notify = callback.notify || _.noop;

      editmodeListener = null;
      scope.editingCallback = callback;
      scope.editModeDefer.resolve(scope.editMode);
      cbListener();
    };

    result.unregisterCallback = function() {
      scope.editingCallback = null;
      setEditMode(false);
    };

    result.editingEnabled = function() {
      return !!scope.editingCallback;
    };

    result.registerCallbackListener = function(callbackListener) {
      cbListener = callbackListener;
    };

    result.registerEditModeListener = function (listener) {
      editmodeListener = listener;
    };

    result.getEditModePromise = function() {
      return scope.editModeDefer.promise;
    };

    result.getEditMode = function() {
      return scope.editMode;
    };
    return result;
  });


