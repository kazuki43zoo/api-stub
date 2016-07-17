$(function () {

    var toggleCheckboxes = $("#toggleCheckboxes");
    var ids = $("input[name='ids']");
    var deleteButton = $("button[name='delete']");
    var checkedCount = 0;

    toggleCheckboxes.on("change", function () {
        ids.prop("checked", toggleCheckboxes.prop("checked"));
        checkedCount = toggleCheckboxes.prop("checked") ? ids.length : 0;
        deleteButton.prop("disabled", checkedCount == 0);
    });

    ids.on("change", function (event) {
        checkedCount += ($(event.target).prop("checked") ? 1 : -1);
        deleteButton.prop("disabled", checkedCount == 0);
        toggleCheckboxes.prop("checked", checkedCount == ids.length);
    });

});