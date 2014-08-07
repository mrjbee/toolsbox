/*

UIDelegate {
     requestLoadingRendering : function () {},
     cancelLoadingRendering:function () {},
     renderHeader:function (selectedFiles) {},
     rootView:function () {},
     renderFolder:function (itFolder, doOnTraverse){},
     renderFile : function (itFile) {}
 }

DataProvider{
     getRoots : function (doOnDone) {},
     getSubFiles : function (file, doOnDone) {}
 }
*/

var FileBrowserPrototype = {

    _uiDelegate : null,
    _dataProvider : null,
    _selectedFiles:[],
    fileList:[],

    constructor:function _constructor(ui, dataProvider){
        this._uiDelegate = ui;
        this._dataProvider = dataProvider;

    },

    moveToRoot: function(onReady){
        this._selectedFiles = [];
        this._uiDelegate.requestLoadingRendering();
        this._dataProvider.getRoots(function(files){
            this._updateUI(files);
            this._uiDelegate.cancelLoadingRendering();
            if(onReady!=null) onReady();
        }.bind(this));
    },

    moveToStorage:function(itStorage){
        var storageAsFile = {
            id:itStorage.refFileId,
            name:itStorage.label,
            folder:true
        };

        this._selectedFiles = [];
        this._selectedFiles.push(storageAsFile);
        this._moveToFile(storageAsFile);
    },

    refresh:function(){
        if(this.getOpenFolder()==null){
            this.moveToRoot()
        } else{
            this._moveToFile(this.getOpenFolder());
        }
    },

    _moveToFile: function(file){
        this._uiDelegate.requestLoadingRendering();
        this._dataProvider.getSubFiles(file, function(files){
            this._updateUI(files);
            this._uiDelegate.cancelLoadingRendering();
        }.bind(this));
    },

    getOpenFolder: function (){
        if (this._selectedFiles.length == 0){
            return null;
        }
        return this._selectedFiles[this._selectedFiles.length -1];
    },

    getSelectedPath: function () {
        var caption = ""
        if (this._selectedFiles.length != 0){
            for (var i=0;i<this._selectedFiles.length;i++){
                caption=caption+"/"+this._selectedFiles[i].name;
            }
        }
        return caption;
    },

    _updateUI:function(files){
        this.fileList = files;
        this._uiDelegate.rootView().empty();
        var header = this._uiDelegate.renderHeader(this._selectedFiles);
        if (header != null)
            this._uiDelegate.rootView().append(header);
        var itFile, itRow;
        for (var index = 0; index < files.length; ++index) {
            itFile = files[index];

            if (itFile.folder){
                itRow = this._uiDelegate.renderFolder(itFile, function(file){
                    if (file.name == ".."){
                        this._selectedFiles.pop();
                    } else {
                        this._selectedFiles.push(file);
                    }
                    this._moveToFile(file);
                }.bind(this))
            } else {
                itRow = this._uiDelegate.renderFile(itFile);
            }

            if (itRow != null) {
                this._uiDelegate.rootView().append(itRow);
            }
        }
        this._uiDelegate.rootView().listview( "refresh" );
    }
};