var ModelPrototype = {

    _presenter:null,

    //Production URl
    //_serverUrl : "http://194.29.62.160:8880/remote-control-api/rest",

    //Test URl
    _serverUrl : "http://192.168.0.201:8080/remfly-api/rest",

    _username : "",
    _password : "",

    //Server statistic fields
    //@Deprecated
    awakeMinutes : 0,
    lastStatus : "NaN",
    lastDate : "NaN",
    offlineTillDate : "NaN",

    constructor:function _constructor(){
        $.ajaxSetup({
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
                onSuccess($.parseJSON(response.resultText))
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
                var storages = $.parseJSON(response.resultText);
                var storagesAsFiles = []
                for (var index = 0; index < storages.length; ++index) {
                    storagesAsFiles.push({
                        id:storages[index].refFileId,
                        name:storages[index].label,
                        folder:true
                    })
                }
                onSuccess(storagesAsFiles);
            } else {
                onFailure(response.statusCode);
            }
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