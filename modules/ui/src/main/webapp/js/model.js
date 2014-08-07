var ModelPrototype = {

    _presenter:null,

    //Production URl
    _serverUrl : "http://193.151.106.119:8080/remfly-api/rest",

    //Test URl
    //_serverUrl : "http://localhost:8080/remfly-api/rest",

    _username : "",
    _password : "",

    _taskUpdateTid:null,
    
    constructor:function _constructor(){
        this._serverUrl = "http://"+location.host+"/remfly-api/rest";
        $.ajaxSetup({
            contentType: "application/json; charset=UTF-8",
            beforeSend: function (request) {
                request.setRequestHeader("Avoid-WWW-Authenticate", "yes");
                request.setRequestHeader("Authorization", "Basic " + btoa(this._username + ":" + this._password));
            }.bind(this)
        });

    },

    loginUser : function (loginRequestModel, whenSuccess, whenFails, whenError) {
        this._username = loginRequestModel.userName;
        this._password = loginRequestModel.password;
        this._doRequest({
            type: "GET",
            url: this._serverUrl + '/secure-ping'
        }, function (response) {
            if (response.statusCode == 401) {
                whenFails();
            } else if (response.statusCode == 200) {
                whenSuccess();
            } else {
                whenError(response.statusCode);
            }
        }.bind(this))
    },

    initialize : function (onSuccess, onFailure) {
    },

    requestFiles : function (rootFile, onSuccess, onFailure) {
        this._doRequest({
            type: "GET",
            url: this._serverUrl + '/file/'+rootFile.id+"/children"
        }, function (response) {
            if (response.statusCode == 200) {
                onSuccess((response.resultText))
            } else {
                onFailure(response.statusCode);
            }
        }.bind(this))
    },

    requestStoragesAsFiles : function (onSuccess, onFailure) {
        this._doRequest({
            type: "GET",
            url: this._serverUrl + '/storages'
        }, function (response) {
            if (response.statusCode == 200) {
                var storages = (response.resultText);
                var storagesAsFiles = []
                for (var index = 0; index < storages.length; ++index) {
                    storagesAsFiles.push({
                        id:storages[index].refFileId,
                        name:storages[index].label,
                        folder:true,
                        storage:true
                    })
                }
                onSuccess(storagesAsFiles);
            } else {
                onFailure(response.statusCode);
            }
        }.bind(this))
    },

    addTask: function(task, whenSuccess, whenFails){
        this._doRequest({
            type: "POST",
            url: this._serverUrl + '/task',
            data: JSON.stringify(task)
        }, function (response) {
            if (response.statusCode == 200) {
                whenSuccess()
            } else {
                whenFails(response.statusCode);
            }
        }.bind(this))
    },

    renameFileTo: function (newName, id, callback) {
        this._doRequest({
            type: "PUT",
            url: this._serverUrl + '/file/'+id,
            data: JSON.stringify({
                name:newName
            })
        }, function (response) {
            callback(response.statusCode == 200 || response.statusCode == 202);
        }.bind(this))
    },

    createFolder: function (newName, id, callback) {
        this._doRequest({
            type: "POST",
            url: this._serverUrl + '/file',
            data: JSON.stringify({
                parentRef:id,
                name:newName
            })
        }, function (response) {
            callback(response.statusCode == 200 || response.statusCode == 202);
        }.bind(this))
    },

    deleteFile: function (id, callback) {
        this._doRequest({
            type: "DELETE",
            url: this._serverUrl + '/file/'+id
        }, function (response) {
            callback(response.statusCode == 200 || response.statusCode == 202);
        }.bind(this))
    },
    requestPeriodicalTaskUpdate:function(onTasks){
        this.disablePeriodicalTaskUpdate();
        this._requestTasksDetails(onTasks);
        this._taskUpdateTid = setInterval(function(){
            this._requestTasksDetails(onTasks)
        }.bind(this), 1000);
    },

    disablePeriodicalTaskUpdate: function(){
        if (this._taskUpdateTid){
            clearInterval(this._taskUpdateTid);
        }
    },

    _requestTasksDetails : function (onSuccess, onFailure) {
        this._doRequest({
            type: "GET",
            url: this._serverUrl + '/tasks'
        }, function (response) {
            if (response.statusCode == 200) {
                var tasks = (response.resultText);
                onSuccess(tasks);
            } else {
                //onFailure(response.statusCode);
            }
        }.bind(this))
    },


    requestPeriodicalStorageUpdate:function(onStorage){
        this._requestStorageDetails(onStorage);
        setInterval(function(){
            this._requestStorageDetails(onStorage)
        }.bind(this), 5000);
    },

    _requestStorageDetails : function (onSuccess, onFailure) {
        this._doRequest({
            type: "GET",
            url: this._serverUrl + '/storages'
        }, function (response) {
            if (response.statusCode == 200) {
                var storages = (response.resultText);
                onSuccess(storages);
            } else {
                if(onFailure) onFailure(response.statusCode);
            }
        }.bind(this))
    },

    killTask: function (taskId, callback) {
        this._doRequest({
            type: "DELETE",
            url: this._serverUrl + '/task/'+taskId+"/execution"
        }, function (response) {
            callback(taskId, (response.statusCode == 200 || response.statusCode == 202));
        }.bind(this))
    },


    cleanTask: function (taskId, callback) {
        this._doRequest({
            type: "DELETE",
            url: this._serverUrl + '/task/'+taskId
        }, function (response) {
           callback(taskId, (response.statusCode == 200 || response.statusCode == 202));
        }.bind(this))
    },

    fetchUrlDetails: function (url, callback){
        //function(success, urlDetails, statusCode)
        this._doRequest({
            type: "POST",
            url: this._serverUrl + '/downloads/details',
            data: JSON.stringify({url:url})
        }, function (response) {
            callback(response.statusCode == 200,response.resultText,response.statusCode);
        }.bind(this))
    },

    _doRequest: function __doRequest(ajaxDetails, callback) {
        $.ajax(ajaxDetails).always(function (dataorJQXHR, textStatus, jqXHRorErrorThrown) {
            if (textStatus == "success") {
                callback({
                    statusCode: jqXHRorErrorThrown.status,
                    resultText: dataorJQXHR
                })
            } else {
                callback({
                    statusCode: dataorJQXHR.status
                })
            }
        });
    }

};
