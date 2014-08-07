
var PresenterPrototype = {

    _model: null,
    _view: null,
    _rootBrowserView: null,
    _copyBrowserView: null,
    _downloadBrowserView: null,
    _taskWidgetFactory: null,
    _taskWidgets:{},
    _scheduledNameFunction:null,
    _downloadDetailsStack:[],
    _selectedFiles:[],

    _showDeletePopup: function () {
        this._lockUI(false);
        setTimeout(function () {
            this._unlockUI();
            this._view.deleteFileList.empty();
            var liEl;
            for (var i = 0; i < this._selectedFiles.length; i++) {
                liEl = $(document.createElement("li"));
                liEl.append(this._selectedFiles[i].name);
                this._view.deleteFileList.append(liEl);
            }
            this._view.deleteDialog.popup("open");
        }.bind(this), 1000);
    },

    _collectSelectedFiles: function () {
        var checkboxPerFileItems = this._view.multiSelectFieldSet.contentItems;
        this._selectedFiles = [];
        for (var i = 0; i < checkboxPerFileItems.length; i++) {
            if (checkboxPerFileItems[i].checkbox.is(':checked')) {
                this._selectedFiles.push(checkboxPerFileItems[i].file);
            }
        }
    },

    _showCopyDialog: function () {
        this._lockUI(false);
        setTimeout(function () {
            this._copyBrowserView.moveToRoot(function () {
                this._view.copyDialogFileList.empty();
                var liEl;
                for (var i = 0; i < this._selectedFiles.length; i++) {
                    liEl = $(document.createElement("li"));
                    liEl.append(this._selectedFiles[i].name);
                    this._view.copyDialogFileList.append(liEl);
                }


                this._view.copyDialogInfoLabel.text("");
                this._view.copyDialogRemoveCheckBox.attr("checked", false).checkboxradio("refresh");
                this._view.copyDialog.popup("open");
                this._unlockUI();
            }.bind(this));
        }.bind(this), 1000);
    }, constructor:function _constructor(model,view){
        var me = this;
        this._model = model;
        this._view = view;
        this._taskWidgetFactory = Object.create(TaskWidgetFactoryPrototype);

        this._view.loginBtn.click(function(){
            this.initiateLogin();
        }.bind(this));

        this._view.fileBrowserTopBtn.click(function(){
            this._rootBrowserView.moveToRoot();
        }.bind(this));

        this._view.copyDialogStorageListBtn.click(function(){
            this._copyBrowserView.moveToRoot();
        }.bind(this));

        this._view.downloadFileBackStorageBtn.click(function(){
            this._downloadBrowserView.moveToRoot();
        }.bind(this));

        this._view.filesTabBtn.on('click', function(){
           me._model.disablePeriodicalTaskUpdate();
        });

        this._view.tasksTabBtn.on('click', function () {
            me._model.requestPeriodicalTaskUpdate(function(tasks){
                var itTask, itWidget, taskMap={};
                for(var i =0;i<tasks.length; i++){
                    itTask = tasks[i];
                    if (this._taskWidgets["task"+itTask.taskId]){
                        itWidget = this._taskWidgets["task"+itTask.taskId];
                        itWidget.update(itTask);
                    } else {
                        itWidget = this._taskWidgetFactory.createFor(itTask,this);
                        this._taskWidgets["task"+itTask.taskId] = itWidget;
                        itWidget.show(this._view.taskBrowserList);
                    }
                    taskMap["task"+itTask.taskId] = true;
                }
                for(var itWidgetName in this._taskWidgets){
                    if(taskMap[itWidgetName] == null){
                        itWidget = this._taskWidgets[itWidgetName];
                        delete this._taskWidgets[itWidgetName];
                        itWidget.close(this._view.taskBrowserList);
                    }
                }
            }.bind(me));
        });


        this._view.copyDialogCopyBtn.click(function(){
            if (this._copyBrowserView.getOpenFolder() == null) {
                this._view.copyDialogInfoLabel.text("Please select destination folder.")
                this._view.copyDialogInfoLabel.slideDown().delay(1000).fadeOut(400);
                return;
            }
            this._view.copyDialog.popup("close")
            var unsuccessfulCallback = function(status){
                this._unlockUI();
                //TODO: implement error handling
                alert("Sorry not implemented. Error = "+status);
            }.bind(this);
            this._lockUI(true);


            var callbackFunction = function(index){
                if(index<0){
                    this._unlockUI();
                    return;
                }
                this._model.addTask({
                    type:"copy",
                    srcFile:this._selectedFiles[index].id,
                    dstFile:this._copyBrowserView.getOpenFolder().id,
                    removeRequired: this._view.copyDialogRemoveCheckBox.is(':checked')
                },callbackFunction.bind(this,(index-1)),unsuccessfulCallback);
            };

            this._model.addTask({
                type:"copy",
                srcFile:this._selectedFiles[this._selectedFiles.length-1].id,
                dstFile:this._copyBrowserView.getOpenFolder().id,
                removeRequired: this._view.copyDialogRemoveCheckBox.is(':checked')
            },callbackFunction.bind(this,this._selectedFiles.length-2),unsuccessfulCallback);

        }.bind(this));

        this._view.copyTaskItem.click(function(){
            this._closeActionPopup();
            this._showCopyDialog();
        }.bind(this));

        this._view.renameTaskItem.click(function(){
            this._closeActionPopup();
            this._lockUI(false);
            setTimeout(function(){
                this._unlockUI();
                this._scheduledNameFunction = function(newName){
                    this._lockUI(false);
                    this._model.renameFileTo(newName,this._model.selectedFile.id, function(result){
                        if (!result){
                            alert("Ooops! No luck with renaming.")
                        }
                        this._unlockUI();
                        this._rootBrowserView.refresh();
                    }.bind(this));
                }.bind(this);
                this._view.nameFormEdit.val(this._model.selectedFile.name);
                this._view.nameDialog.popup("open");
            }.bind(this), 1000);
        }.bind(this));

        this._view.nameFormOkBtn.on("click", function(){
            this._view.nameDialog.popup("close");
            this._scheduledNameFunction(this._view.nameFormEdit.val());
        }.bind(this));

        this._view.addFolderTopBtn.click(function(){
            this._scheduledNameFunction = function(newName){
                this._lockUI(false);
                this._model.createFolder(newName,this._rootBrowserView.getOpenFolder().id, function(result){
                    if (!result){
                        alert("Ooops! No luck with new folder creation.")
                    }
                    this._unlockUI();
                    this._rootBrowserView.refresh();
                }.bind(this));
            }.bind(this);
            this._view.nameFormEdit.val("New Folder");
            this._view.nameDialog.popup("open");
        }.bind(this));

        this._view.multiSelectTopBtn.click(function(){
            var files = this._rootBrowserView.fileList;
            var itCheckBox,itLabel;
            var itId,itName;
            var itemPairs = [];
            this._view.multiSelectFieldSet.empty();
            for (var i=0; i< files.length; i++){
                if (!files[i].folder){
                    itId = files[i].id+"_checkbox";
                    itName = files[i].name;
                    itCheckBox = $(document.createElement("input"))
                        .attr("name",itId)
                        .attr("id",itId)
                        .attr("type","checkbox");
                    itCheckBox.file=files[i];
                    itLabel = $(document.createElement("label"))
                        .attr("for",itId);
                    itLabel.append(itName);
                    this._view.multiSelectFieldSet.append(itCheckBox);
                    this._view.multiSelectFieldSet.append(itLabel);
                    itCheckBox.checkboxradio();
                    itemPairs.push({
                        checkbox:itCheckBox,
                        file:files[i]
                    });
                }
            }
            this._view.multiSelectFieldSet.contentItems = itemPairs;
            this._view.multiSelectFieldSet.controlgroup( "refresh" );
            this._view.multiSelectDialog.popup("open");
        }.bind(this));

        this._view.multiSelectCopyBtn.click(function(){
            this._collectSelectedFiles();
            if (this._selectedFiles.length == 0){
                this._view.multiSelectInfoLabel.text("Please select at least one file.");
                this._view.multiSelectInfoLabel.slideDown().delay(1000).fadeOut(400);
                return;
            }
            this._view.multiSelectDialog.popup("close");
            this._showCopyDialog();
        }.bind(this));

        this._view.multiSelectDeleteBtn.click(function(){
            this._collectSelectedFiles();
            if (this._selectedFiles.length == 0){
                this._view.multiSelectInfoLabel.text("Please select at least one file.");
                this._view.multiSelectInfoLabel.slideDown().delay(1000).fadeOut(400);
                return;
            }
            this._view.multiSelectDialog.popup("close");
            this._showDeletePopup();
        }.bind(this));

        this._view.deleteTaskItem.click(function(){
            this._closeActionPopup();
            this._showDeletePopup();
        }.bind(this));

        this._view.deleteFileOkBtn.on("click",function(){
            this._view.deleteDialog.popup("close");
            this._lockUI(false);
            var callbackFunction = function(index, result){
                if (!result){
                    alert("Ooops! No luck with deletion.");
                    this._unlockUI();
                    this._rootBrowserView.refresh();
                    return;
                }
                if (index < 0){
                    this._unlockUI();
                    this._rootBrowserView.refresh();
                } else {
                    this._model.deleteFile(this._selectedFiles[index].id, callbackFunction.bind(this, (index-1)));
                }
            };
            this._model.deleteFile(this._selectedFiles[this._selectedFiles.length-1].id,
                callbackFunction.bind(this, (this._selectedFiles.length-2)));
        }.bind(this));

        this._rootBrowserView = Object.create(FileBrowserPrototype);
        this._rootBrowserView.constructor({
            requestLoadingRendering : function () {me._lockUI(true)},
            cancelLoadingRendering:function () {me._unlockUI()},
            rootView:function () {return me._view.fileBrowserList},
            renderHeader:function (selectedFiles) {
                var liEl;
                var caption = "Available Storages"
                if (selectedFiles.length != 0){
                    caption = "";
                    for (var i=0;i<selectedFiles.length;i++){
                        caption=caption+"/"+selectedFiles[i].name;
                    }
                }
                liEl = $(document.createElement("li"));
                liEl.attr("data-role","list-divider");
                liEl.css("direction","rtl");
                liEl.append(caption);
                if (selectedFiles.length != 0){
                    me._view.addFolderTopBtn.fadeIn();
                    me._view.multiSelectTopBtn.fadeIn();
                } else {
                    me._view.addFolderTopBtn.fadeOut();
                    me._view.multiSelectTopBtn.fadeOut();
                }
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
                });
                if (itFolder.name!=".." && !itFolder.storage){
                    var actionEl = $(document.createElement("a"));
                    actionEl.attr("data-split-icon","gear");
                    liEl.append(actionEl);
                    liEl.attr("data-icon","gear");
                    actionEl.click({
                        file:itFolder
                    },function(event){
                        me._model.selectedFile = event.data.file;
                        me._selectedFiles = [event.data.file];
                        me._view.copyTaskItem.slideUp();
                        me._view.taskChoosePopup.popup("open",{
                            x:event.clientX,
                            y:event.clientY,
                            positionTo: "origin",
                            transition: "slideup"
                        });
                    }.bind(me));

                } else {
                    if (!itFolder.storage){
                        liEl.attr("data-icon","back");
                    }
                }
                return liEl;
            },
            renderFile : function (itFile) {
                var liEl,aEl;
                liEl = $(document.createElement("li"));
                aEl = $(document.createElement("a"));
                liEl.append(aEl);
                aEl.append(itFile.name);
                aEl.attr("href","#");
                liEl.attr("data-icon","gear");
                aEl.click({
                        file:itFile
                    },function(event){
                        me._model.selectedFile = event.data.file;
                        me._selectedFiles = [event.data.file];
                        me._view.copyTaskItem.slideDown();
                        me._view.taskChoosePopup.popup("open",{
                            x:event.clientX,
                            y:event.clientY,
                            positionTo: "origin",
                            transition: "slideup"
                        });
                    }.bind(me));
                liEl.append('<p class="ui-li-aside">'+itFile.size+'</p>');
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
          });

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
                var liEl;
                liEl = $(document.createElement("li"));
                liEl.append(itFile.name);
                liEl.append('<p class="ui-li-aside">'+itFile.size+'</p>');
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
        });

        this._downloadBrowserView = Object.create(FileBrowserPrototype);
        this._downloadBrowserView.constructor({
            requestLoadingRendering : function () {me._lockUI(true)},
            cancelLoadingRendering:function () {me._unlockUI()},
            rootView:function () {return me._view.downloadFileBrowserList},

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
                var liEl;
                liEl = $(document.createElement("li"));
                liEl.append(itFile.name);
                liEl.append('<p class="ui-li-aside">'+itFile.size+'</p>');
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
        });

        this._view.downloadFileRefreshBtn.on('click', function () {
            //Prevent clicking while editing url
            if (me._view.downloadFileUrlEdit.is(":focus")) return;
            this._downloadDetailsStack = [];
            var url=me._view.downloadFileUrlEdit.val().trim();
            if(url != ""){
                this._lockUI();
                for(var i=0; i < this._view.downloadUrlDetailsFields.length;i++){
                    this._view.downloadUrlDetailsFields.fadeOut();
                }
                this._view.downloadFileBrowserPanel.fadeOut();
                this._model.fetchUrlDetails(url, function(success, urlDetails, statusCode){
                    if (success) {
                        if (urlDetails.metadata){
                            this._view.downloadMetaDataCaptionLabel.text(urlDetails.metadata.title);
                            this._view.downloadMetaDataDescriptionLabel.text(urlDetails.metadata.description);
                            this._view.downloadMetaDataImg.attr("src",urlDetails.metadata.imageUrl);
                            this._view.downloadMetaDataPanel.fadeIn();
                        }else{
                            this._view.downloadMetaDataPanel.fadeOut();
                        }
                        this._updateDownloadDetailsView(urlDetails);
                    }else{
                        alert("Ooops! Something bad. ("+statusCode+")");
                    }
                    this._unlockUI();
                }.bind(this));
            }
        }.bind(me));


        this._view.downloadFileBtn.on('click', function () {

            var fileName = me._view.downloadFileNameEdit.val().trim() + "." + me._view.downloadFileExtEdit.val().trim();
            var distFolder = me._downloadBrowserView.getOpenFolder();
            var link = this._view.downloadFileUrlLink.attr("href");

            if (distFolder==null) {
                this._view.downloadFileInfoLabel.text("Please select destination folder.");
                this._view.downloadFileInfoLabel.slideDown().delay(1000).fadeOut(400);
                return;
            }

            if (fileName==".") {
                this._view.downloadFileInfoLabel.text("Please select file name.");
                this._view.downloadFileInfoLabel.slideDown().delay(1000).fadeOut(400);
                return;
            }
            this._lockUI();
            this._model.addTask({
                type:"download",
                url:link,
                dst:distFolder.id,
                name:fileName
            }, function(){
                this._unlockUI();
                for(var i=0; i < this._view.downloadUrlDetailsFields.length;i++){
                    this._view.downloadUrlDetailsFields.slideUp();
                }
                this._view.downloadFileBrowserPanel.slideUp();
            }.bind(this), function(){
                alert("Ooops. Something bad...");
                this._unlockUI();
            }.bind(this));
        }.bind(me));

        this._view.downloadChoiceBackBtn.on('click', function () {
            var urlDetails = this._downloadDetailsStack.pop();
            urlDetails = this._downloadDetailsStack.pop();
            this._updateDownloadDetailsView(urlDetails);
        }.bind(me));

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

    onTaskActionBtn:function(task, taskWidget) {
        this._lockUI(true)
        if (task.status == "Progress") {
            //request execution cancellation
            this._model.killTask(task.taskId, function(taskId, result){
                this._unlockUI();
                if (!result){
                    alert("Ooops, fails. Try that again!");
                }
            }.bind(this));
        } else {
            //request task cleanup
            this._model.cleanTask(task.taskId,function(taskId, result){
                if (result){
                    //remove widget
                    var itWidget = this._taskWidgets["task"+taskId];
                    if (itWidget){
                        delete this._taskWidgets["task"+taskId];
                        itWidget.close(this._view.taskBrowserList);
                    }
                } else {
                    alert("Ooops, fails. Try that again!");
                }
                this._unlockUI();
            }.bind(this));
        }
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
        this._downloadBrowserView.moveToRoot();
        this._model.requestPeriodicalStorageUpdate(function(storages){
            //render storage
            this._view.storageBrowserList.empty();
            var liEl,aEl,itStorage;
            for(var i=0;i<storages.length;i++){
                itStorage=storages[i];
                liEl = $(document.createElement("li"));
                aEl = $(document.createElement("a"));
                liEl.append(aEl);
                aEl.append(itStorage.label);
                aEl.attr("href","#");
                //liEl.attr("data-icon","gear");
                aEl.click({
                    file:itStorage
                },function(event){
                    this._view.filesTabBtn.click();
                   this._rootBrowserView.moveToStorage(event.data.file);
                }.bind(this));
                aEl.append('<p>Free space <strong>'+itStorage.freeSpace+'</strong></p>');
                aEl.append('<p>Total space <strong>'+itStorage.space+'</strong></p>');
                this._view.storageBrowserList.append(liEl);
            }
            this._view.storageBrowserList.listview( "refresh" );
        }.bind(this))
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
    },

    _updateDownloadDetailsView:function(urlDetails){
        this._downloadDetailsStack.push(urlDetails);
        if (urlDetails.downloadUrlDetails) {
            urlDetails = urlDetails.downloadUrlDetails;
            this._view.downloadFileUrlLink.text("Download Link");
            this._view.downloadFileUrlLink.attr("href", urlDetails.url);
            this._view.downloadFileNameEdit.val(urlDetails.fileName);
            this._view.downloadFileExtEdit.val(urlDetails.ext);
            this._view.downloadFileSizeLabel.text(urlDetails.size);
            this._showUpDownloadDetails();
        } else {
            this._view.downloadChoiceBrowserList.empty();
            var liEl,aEl,pEl,p2El;
            var itChoice;
            for (var i=0;i<urlDetails.downloadUrlChoices.length;i++){
                itChoice = urlDetails.downloadUrlChoices[i];
                liEl = $(document.createElement("li"));
                aEl = $(document.createElement("a"));
                pEl = $(document.createElement("p"));
                p2El = $(document.createElement("p"));
                liEl.append(aEl);
                if (itChoice.ref.indexOf("plugin:")==-1){
                    liEl.attr("data-icon","cloud");
                }
                p2El.append(itChoice.name);
                pEl.append(itChoice.description);
                aEl.append(p2El);
                aEl.append(pEl);
                aEl.click({
                    ref:itChoice.ref
                },function(event){
                    this._lockUI();
                    this._model.fetchUrlDetails(event.data.ref, function(success, urlDetails, statusCode){
                        if (success) {
                            this._updateDownloadDetailsView(urlDetails);
                        }else{
                            alert("Ooops! Something bad. ("+statusCode+")");
                        }
                        this._unlockUI();
                    }.bind(this));
                }.bind(this));
                this._view.downloadChoiceBrowserList.append(liEl)
            }
            this._view.downloadChoiceBrowserList.listview( "refresh" );
            this._showUpDownloadChoicesDetails();
        }
    },

    _showUpDownloadChoicesDetails:function(){
        for (var i = 0; i < this._view.downloadUrlDetailsFields.length; i++) {
            this._view.downloadUrlDetailsFields.fadeOut();
        }
        this._view.downloadFileBrowserPanel.fadeOut();
        if (this._downloadDetailsStack.length > 1){
            this._view.downloadChoiceBackBtn.fadeIn();
        }else{
            this._view.downloadChoiceBackBtn.fadeOut();
        }
        this._view.downloadChoiceDialog.popup("open");
    },

    _showUpDownloadDetails:function(){
        for (var i = 0; i < this._view.downloadUrlDetailsFields.length; i++) {
            this._view.downloadUrlDetailsFields.fadeIn();
        }
        this._view.downloadFileBrowserPanel.fadeIn();
        this._view.downloadChoiceDialog.popup("close");
    }

}