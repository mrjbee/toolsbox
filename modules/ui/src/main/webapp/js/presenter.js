
var PresenterPrototype = {

    _model: null,
    _view: null,
    _rootBrowserView: null,
    _copyBrowserView: null,

    constructor:function _constructor(model,view){
        this._model = model;
        this._view = view;

        this._view.loginBtn.click(function(){
            this.initiateLogin();
        }.bind(this));

        this._view.copyDialogStorageListBtn.click(function(){
            this._copyBrowserView.moveToRoot();
        }.bind(this));

        this._view.copyDialogCopyBtn.click(function(){
            if (this._copyBrowserView.getOpenFolder() == null) {
                this._view.copyDialogInfoLabel.text("Please select destination folder.")
                this._view.copyDialogInfoLabel.slideDown().delay(1000).fadeOut(400);
                return;
            }
            this._view.copyDialog.popup("close")
            this._lockUI(true);
            this._model.addTask({
                type:"copy",
                srcFile:this._model.selectedFile.id,
                dstFile:this._copyBrowserView.getOpenFolder().id,
                removeRequired: this._view.copyDialogRemoveCheckBox.attr("checked")
            }, function(){
                this._unlockUI();
            }.bind(this), function(status){
                this._unlockUI();
                //TODO: implement error handling
                alert("Sorry not implemented. Error = "+status);
            }.bind(this));
        }.bind(this));

        this._view.copyTaskItem.click(function(){
            this._closeActionPopup();
            this._lockUI(false);
            setTimeout(function(){
                this._copyBrowserView.moveToRoot(function(){
                    var path = this._rootBrowserView.getSelectedPath()+"/"+this._model.selectedFile.name;
                    this._view.copyDialogSrcFileName.text(path);
                    this._view.copyDialogInfoLabel.text("");
                    this._view.copyDialogRemoveCheckBox.attr("checked",false).checkboxradio("refresh");
                    this._view.copyDialog.popup("open");
                    this._unlockUI();
                }.bind(this));
            }.bind(this), 1000);
        }.bind(this));
        this._rootBrowserView = Object.create(FileBrowserPrototype);
        var me = this;
        this._rootBrowserView.constructor({
            requestLoadingRendering : function () {me._lockUI(true)},
            cancelLoadingRendering:function () {me._unlockUI()},
            rootView:function () {return me._view.fileBrowserList},
            renderHeader:function (selectedFiles) {
                var caption = "Available Storages"
                if (selectedFiles.length != 0){
                    caption = "";
                    for (var i=0;i<selectedFiles.length;i++){
                        caption=caption+"/"+selectedFiles[i].name;
                    }
                }
                var liEl;
                liEl = $(document.createElement("li"));
                liEl.attr("data-role","list-divider");
                liEl.css("direction","rtl");
                liEl.append(caption);
                return liEl;
            },
            renderFolder:function (itFolder, doOnTraverse){
                var liEl,aEl;
                liEl = $(document.createElement("li"));
                aEl = $(document.createElement("a"));
                liEl.append(aEl);
                aEl.append(itFolder.name);
                aEl.click({
                    file:itFolder,
                    browserCallBack:doOnTraverse
                },function(event){
                    event.data.browserCallBack(event.data.file);
                })
                return liEl;
            },
            renderFile : function (itFile) {
                var liEl,aEl;
                liEl = $(document.createElement("li"));
                aEl = $(document.createElement("a"));
                liEl.append(aEl);
                aEl.append(itFile.name);
                aEl.attr("href","#file-task-popup");
                aEl.attr("data-rel","popup");
                aEl.attr("data-transition","pop");
                liEl.attr("data-icon","gear");
                aEl.click({
                        file:itFile
                    },function(event){
                        me._model.selectedFile = event.data.file;
                    }.bind(me));
                return liEl;
            }
        },{
            getRoots : function (doOnDone) {
                return me._model.requestStoragesAsFiles(doOnDone, function(status){
                    alert("Not implemented.Please refresh browser and start again")
                })
            },
            getSubFiles : function (file, doOnDone) {
                return me._model.requestFiles(file, doOnDone, function(status){
                    alert("Not implemented.Please refresh browser and start again")
                })
            }
          })

        this._copyBrowserView = Object.create(FileBrowserPrototype);
        this._copyBrowserView.constructor({
            requestLoadingRendering : function () {me._lockUI(true)},
            cancelLoadingRendering:function () {me._unlockUI()},
            rootView:function () {return me._view.copyDialogBrowser},

            renderHeader:function (selectedFiles) {
                var caption = "Available Storages"
                if (selectedFiles.length != 0){
                    caption = "";
                    for (var i=0;i<selectedFiles.length;i++){
                        caption=caption+"/"+selectedFiles[i].name;
                    }
                }
                var liEl;
                liEl = $(document.createElement("li"));
                liEl.attr("data-role","list-divider");
                liEl.css("direction","rtl");
                liEl.append(caption);
                return liEl;
            },
            renderFolder:function (itFolder, doOnTraverse){
                var liEl,aEl;
                liEl = $(document.createElement("li"));
                aEl = $(document.createElement("a"));
                liEl.append(aEl);
                aEl.append(itFolder.name);
                aEl.click({
                    file:itFolder,
                    browserCallBack:doOnTraverse
                },function(event){
                    event.data.browserCallBack(event.data.file);
                })
                return liEl;
            },
            renderFile : function (itFile) {
                return null;
            }
        },{
            getRoots : function (doOnDone) {
                return me._model.requestStoragesAsFiles(doOnDone, function(status){
                    alert("Not implemented.Please refresh browser and start again")
                })
            },
            getSubFiles : function (file, doOnDone) {
                return me._model.requestFiles(file, doOnDone, function(status){
                    alert("Not implemented.Please refresh browser and start again")
                })
            }
        })
    },

    initial : function(){
        this._view.authDialog.popup("open");
    },

    initiateLogin : function(){
        var userNameTxt = this._view.userNameInput.val();
        var passwordTxt = this._view.passInput.val();
        this._view.authDialog.popup("close");

        this._model.loginUser({
                userName:userNameTxt,
                password:passwordTxt
            },
            function(){
                this._initiateModelUpdate();
            }.bind(this),
            function(){
                this._askForReLogin();
            }.bind(this),
            function(statusCode){
                this._askForReLogin(statusCode);
            }.bind(this)
        );
    },

    _unlockUI: function () {
        this._view.lockPanel.fadeOut("fast");
        $.mobile.loading("hide");
    },

    _lockUI: function (noActualBlock) {
        $.mobile.loading("show", {
            text: "Fetching...",
            textVisible: true,
            theme: "a",
            html: ""
        });
        if (noActualBlock == true) return;
        this._view.lockPanel.fadeIn("fast");
    },

    _initiateModelUpdate : function() {
        this._rootBrowserView.moveToRoot();
    },

    _askForReLogin : function(statusCode){
        this._view.infolabel.text("Authorization fails! Try again...")
        if (statusCode != null){
            this._view.infolabel.text("Error ("+statusCode+") ! Please try again later...")
        }
        this._view.authDialog.popup("open");
        this._view.infolabel.slideDown().delay(1000).fadeOut(400);
    },



    _closeActionPopup : function(){
        this._view.taskChoosePopup.popup("close");
    }
}