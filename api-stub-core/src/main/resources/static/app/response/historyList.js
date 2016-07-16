$(function () {

    var toggleCheckboxes = $("#toggleCheckboxes");
    var subIds = $("input[name='subIds']");
    var deleteButton = $("button[name='delete']");
    var checkedCount = 0;

    toggleCheckboxes.on("change", function () {
        subIds.prop("checked", toggleCheckboxes.prop("checked"));
        checkedCount = toggleCheckboxes.prop("checked") ? subIds.length : 0;
        deleteButton.prop("disabled", checkedCount == 0);
    });

    subIds.on("change", function (event) {
        checkedCount += ($(event.target).prop("checked") ? 1 : -1);
        deleteButton.prop("disabled", checkedCount == 0);
        if (checkedCount != subIds.length) {
            toggleCheckboxes.prop("checked", false);
        }
    });

});