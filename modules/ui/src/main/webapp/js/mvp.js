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
        addFolderTopBtn:$("#add-folder-top-btn"),
        fileBrowserTopBtn:$("#file-browser-top-btn"),
        taskChoosePopup:$("#file-task-popup"),
        copyTaskItem:$("#copy-task-item"),
        renameTaskItem:$("#rename-task-item"),
        deleteTaskItem:$("#delete-task-item"),
        copyDialog:$("#copyDialog"),
        nameDialog:$("#namePopup"),
        nameFormEdit:$("#name-form-input"),
        nameFormOkBtn:$("#name-form-ok-btn"),
        deleteDialog:$("#deletePopup"),
        deleteFileNameLabel:$("#deleteFileName"),
        deleteFileOkBtn:$("#delete-form-ok-btn"),
        copyDialogSrcFileName:$("#srcFileName"),
        copyDialogBrowser:$("#copy-browser-list"),
        copyDialogStorageListBtn:$("#copy-dialog-storage-btn"),
        copyDialogRemoveCheckBox:$("#copyDialogRemoveCheckBox"),
        copyDialogInfoLabel:$("#copy-dialog-info-label"),
        copyDialogCopyBtn:$("#copy-dialog-copy-btn"),
        tasksTabBtn:$("#tasks-tab-btn"),
        filesTabBtn:$("#file-tab-btn"),
        taskBrowserList:$("#task-browser-list"),
        storageBrowserList:$("#storage-browser-list"),
        downloadUrlDetailsFields:$("#download-form-list .ui-field-contain"),
        downloadFileRefreshBtn:$("#download-file-refresh-btn"),
        downloadFileUrlEdit:$("#download-file-url-edit"),
        downloadFileUrlLink:$("#download-file-url-link"),
        downloadFileNameEdit:$("#download-file-name-edit"),
        downloadFileExtEdit:$("#download-file-ext-edit"),
        downloadFileSizeLabel:$("#download-file-size-label"),
        downloadFileBrowserList:$("#download-file-browser-list"),
        downloadFileBrowserPanel:$("#downloads-file-browser-panel"),
        downloadFileBackStorageBtn:$("#download-file-back-storage-btn"),
        downloadFileInfoLabel:$("#download-file-info-label"),
        downloadFileBtn:$("#download-file-btn")
    });

    //TODO: disbale form submission. Place it in presenter
    $("#login-form").submit(function() {
        return false;
    });

    presenter.initial();
}