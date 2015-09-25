/// <reference path="../../ts_packages/tsd.d.ts" />

interface EditointiKontrollitCallbacks {
  edit: () => Promise<boolean>;
  save: any;
  cancel: any;
  notify: any;
  // validate: (validator: any) => boolean;
  validate: (validator: any) => Promise<boolean>;
}

interface EditointiKontrollitI {
  startEditing: () => void;
  saveEditing: (kommentti: string) => void;
  cancelEditing: (tilanvaihto: boolean) => void;
  registerCallback: (callback: EditointiKontrollitCallbacks) => void;
  unregisterCallback: () => void;
  editingEnabled: () => boolean;
  registerCallbackListener: (cbListener: any) => void;
  registerEditModeListener: (listener: any) => void;
  getEditModePromise: () => any;
  getEditMode: () => boolean;
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

    result.startEditing = function() {
      if (scope.editingCallback) {
        scope.editingCallback.edit();
        setEditMode(true);
      }
      $rootScope.$broadcast('enableEditing');
    };

    result.saveEditing = function(kommentti: string) {
      $rootScope.$broadcast('editointikontrollit:preSave');
      var err;

      function mandatoryFieldValidator(fields, target) {
        err = undefined;
        var fieldsf = _.filter(fields || [], function(field) { return field.mandatory; });

        if (!target) { return false; }
        else if (_.isString(target)) { return !_.isEmpty(target); }
        else if (_.isObject(target) && !_.isEmpty(target) && !_.isEmpty(fieldsf)) {
          return _.all(fieldsf, function(field) {
            var valid = Utils.hasLocalizedText(target[field.path]);
            if (!valid) { err = field.mandatoryMessage; }
            return valid;
          });
        }
        else { return true; }
      }

      function afterSave() {
        setEditMode(false);
        $rootScope.$broadcast('disableEditing');
      }

      function after() {
        if (!scope.editingCallback.validate || scope.editingCallback.validate(mandatoryFieldValidator)) {
          if (scope.editingCallback.asyncSave) {
            scope.editingCallback.asyncSave(kommentti, afterSave);
          }
          else {
            scope.editingCallback.save(kommentti);
            afterSave();
          }
        }
        else {
          Notifikaatiot.varoitus(err || 'mandatory-odottamaton-virhe');
        }
      }

      if (scope.editingCallback) {
        if (_.isFunction(scope.editingCallback.asyncValidate)) {
          scope.editingCallback.asyncValidate(after);
        }
        else {
          after();
        }
      }

      $rootScope.$broadcast('notifyCKEditor');
    };

    result.cancelEditing = function(tilanvaihto) {
      function doCancel() {
        setEditMode(false);
        if (scope.editingCallback) {
          scope.editingCallback.cancel();
        }
        $rootScope.$broadcast('disableEditing');
        $rootScope.$broadcast('notifyCKEditor');
      }
      if (scope.editingCallback) {
        if (_.isFunction(scope.editingCallback.canCancel) && !tilanvaihto) {
          scope.editingCallback.canCancel().then(doCancel);
        } else {
          doCancel();
        }
      }
    };

    result.registerCallback = function(callback: EditointiKontrollitCallbacks) {
      // if (!callback ||
      //     !_.isFunction(callback.edit) ||
      //     (!_.isFunction(callback.save) && !_.isFunction(callback.asyncSave)) ||
      //     !_.isFunction(callback.cancel)) {
      //   console.error('callback-function invalid');
      //   throw 'editCallback-function invalid';
      // }

      if (!_.isFunction(callback.notify)) {
        callback.notify = _.noop;
      }

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
