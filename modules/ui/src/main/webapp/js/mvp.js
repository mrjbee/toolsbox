function initMVP(){
    var model = Object.create(ModelPrototype);
    model.constructor();

    var presenter = Object.create(PresenterPrototype)
    presenter.constructor(model,{
        loginBtn:$("#login-btn"),
        userNameInput:$("#user-input"),
        passInput:$("#pass-input"),
        authDialog:$("#loginDialog"),
        infolabel:$("#info-label"),
        lockPanel: $("#lockPanel"),
        fileBrowserList:$("#file-browser-list"),
        taskChoosePopup:$("#file-task-popup"),
        copyTaskItem:$("#copy-task-item"),
        copyDialog:$("#copyDialog"),
        copyDialogSrcFileName:$("#srcFileName"),
        copyDialogBrowser:$("#copy-browser-list"),
        copyDialogStorageListBtn:$("#copy-dialog-storage-btn")
    });

    //TODO: disbale form submission. Place it in presenter
    $("#login-form").submit(function() {
        return false;
    });

    presenter.initial();
}