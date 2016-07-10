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
        setBodyEditorMode();
    });

    setBodyEditorMode();

    function setBodyEditorMode() {
        bodyEditor.getSession().setMode("ace/mode/" + bodyEditorMode.val());
    }

});