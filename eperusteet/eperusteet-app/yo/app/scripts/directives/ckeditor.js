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
/*global CKEDITOR,$,_*/

angular.module('eperusteApp')
  .run(function() {
    CKEDITOR.disableAutoInline = true;
  })
  .constant('editorLayouts', {
    minimal:
      [
        { name: 'clipboard', items : [ 'Cut','Copy','-','Undo','Redo' ] },
        { name: 'tools', items : [ 'About' ] }
      ],
    simplified:
      [
        { name: 'clipboard', items : [ 'Cut','Copy','Paste','-','Undo','Redo' ] },
        { name: 'basicstyles', items : [ 'Bold','Italic','Underline','Strike','-','RemoveFormat' ] },
        { name: 'paragraph', items : [ 'NumberedList','BulletedList','-','Outdent','Indent'] },
        { name: 'styles', items : [ 'Format' ] },
        { name: 'tools', items : [ 'About' ] }
      ],
    normal:
      [
        { name: 'clipboard', items : [ 'Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo' ] },
        { name: 'basicstyles', items : [ 'Bold','Italic','Underline','Strike','-','RemoveFormat' ] },
        '/',
        { name: 'paragraph', items : [ 'NumberedList','BulletedList','-','Outdent','Indent','-','Blockquote' ] },
        { name: 'insert', items : [ 'Table','HorizontalRule','SpecialChar' ] },
        { name: 'styles', items : [ 'Format' ] },
        { name: 'tools', items : [ 'About' ] }
      ]
  })
  .directive('ckeditor', function($q, $filter, $rootScope, editorLayouts) {
    return {
      priority: 10,
      restrict: 'A',
      require: 'ngModel',
      scope: {
        editorPlaceholder: '@?',
        editMode: '@?editingEnabled'
      },
      link: function(scope, element, attrs, ctrl) {
        var placeholderText = null;

        var editingEnabled = (scope.editMode || 'true') === 'true';

        if(editingEnabled) {
          element.addClass('edit-mode');
        }
        element.attr('contenteditable', 'true');

        function getPlaceholder() {
          if(scope.editorPlaceholder) {
            return $filter('translate')(scope.editorPlaceholder);
          } else {
            return '';
          }
        }

        var editor = CKEDITOR.instances[attrs.id];
        if (editor) {
          console.log('editor exist');
          return;
        }

        var toolbarLayout;
        if(!_.isEmpty(attrs.layout) && !_.isEmpty(editorLayouts[attrs.layout])) {
          toolbarLayout = editorLayouts[attrs.layout];
        } else {
          if(element.is('div')) {
            toolbarLayout = editorLayouts.normal;
          } else {
            toolbarLayout = editorLayouts.minimal;
          }
        }

        editor = CKEDITOR.inline(element[0], {
          toolbar: toolbarLayout,
          removePlugins: 'resize,elementspath,scayt,wsc',
          extraPlugins: 'divarea,sharedspace',
          extraAllowedContent: '*[contenteditable]',
          language: 'fi',
          'entities_latin': false,
          sharedSpaces: {
            top: 'ck-toolbar-top'
          },
          readOnly: !editingEnabled
        });

        // poistetaan enterin käyttö, jos kyseessä on yhden rivin syöttö
        if(!element.is('div')) {
          editor.on('key', function(event) {
            if(event.data.keyCode === 13) {
              event.cancel();
            }
          });
        }

        $rootScope.$on('$translateChangeSuccess', function() {
          placeholderText = getPlaceholder();
          ctrl.$render();
        });

        scope.$on('enableEditing', function() {
          editingEnabled = true;
          editor.setReadOnly(!editingEnabled);
          element.addClass('edit-mode');
        });

        scope.$on('disableEditing', function() {
          editingEnabled = false;
          editor.setReadOnly(!editingEnabled);
          element.removeClass('edit-mode');
        });

        scope.$on('$destroy', function() {
          if (editor) {
            editor.destroy(false);
          }
        });

        editor.on('focus', function() {
          console.log('focus');
          if (editingEnabled) {
            element.removeClass('has-placeholder');
            $('#toolbar').show();
            if(_.isEmpty(ctrl.$viewValue)) {
              editor.setData('');
            }
          }
        });

        var dataSavedOnNotification = false;
        $rootScope.$on('notifyCKEditor', function() {
          console.log('notifyCKEditor');
          if(editor.checkDirty()) {
            dataSavedOnNotification = true;
            var data = editor.getData();
            ctrl.$setViewValue(data);
          }
          $('#toolbar').hide();
        });

        editor.on('blur', function() {
          console.log('blur');
          if (dataSavedOnNotification) {
            dataSavedOnNotification = false;
            return;
          }
          if (editor.checkDirty()) {
            var data = editor.getData();
            scope.$apply(function() {
              ctrl.$setViewValue(data);
              scope.$broadcast('edited');
            });
            if(_.isEmpty(data)) {
              element.addClass('has-placeholder');
              editor.setData(placeholderText);
            }
          }
          $('#toolbar').hide();
        });

        // model -> view

        ctrl.$render = function() {
          //console.log('render: ' + ctrl.$viewValue);
          if (editor) {
            if(angular.isUndefined(ctrl.$viewValue) || (angular.isString(ctrl.$viewValue) && _.isEmpty(ctrl.$viewValue) && placeholderText)) {
              element.addClass('has-placeholder');
              editor.setData(placeholderText);
              editor.resetDirty();
            } else {
              element.removeClass('has-placeholder');
              editor.setData(ctrl.$viewValue);
            }
          }
        };

        placeholderText = getPlaceholder();
        ctrl.$render();
      }
    };
  });
