/*
 *    Copyright 2016 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
function setBodyEditorMode(bodyEditor) {
    bodyEditor.getSession().setMode("ace/mode/" + bodyEditorMode.val());
}
$(function () {

    var bodyEditor = ace.edit("bodyEditor");
    var body = $("#body");
    var saveButton = $("#saveButton");
    var bodyEditorMode = $("#bodyEditorMode");

    bodyEditor.commands.addCommand({
        name: 'saveCommand',
        bindKey: {
            win: 'Ctrl-S',
            mac: 'Command-S'
        },
        exec: function () {
            saveButton.click();
        }
    });

    saveButton.on("click", function () {
        body.val(bodyEditor.getSession().getValue());
    });

    bodyEditorMode.on("change", function () {
        setBodyEditorMode(bodyEditor);
    });

    setBodyEditorMode();

});