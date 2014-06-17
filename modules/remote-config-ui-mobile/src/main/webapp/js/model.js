var ModelPrototype = {

    _presenter:null,

    _serverUrl : "http://192.168.0.201:8080/remote-config-1.0-SNAPSHOT/rest",

    _username : "",
    _password : "",

    //Server statistic fields
    awakeMinutes : 0,
    lastStatus : "NaN",
    lastDate : "NaN",
    offlineTillDate : "NaN",

    _settings:null,

    constructor:function _constructor(){
        $.ajaxSetup({
            beforeSend: function (request) {
                request.setRequestHeader("Avoid-WWW-Authenticate", "yes");
                request.setRequestHeader("Authorization", "Basic " + btoa(this._username + ":" + this._password));
            }.bind(this)
        });
        var model = this;
        _settings = {
            awakeMinutes:{
                name:"sleepminutes",
                applyValue: function (resultText) {
                    model.awakeMinutes = parseInt(resultText);
                }
            },
            lastStatus:{
                name:"status",
                applyValue: function (resultText) {
                    model.lastStatus = resultText;
                }
            },
            lastDate:{
                name:"lastDate",
                applyValue: function (resultText) {
                    model.lastDate = resultText;
                }
            },
            offlineTillDate:{
                name:"offlineTillDate",
                applyValue: function (resultText) {
                    model.offlineTillDate = resultText;
                }
            }
        }
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

    updateDetails : function (onSuccess,onFailure) {

        this._fetchSettings([_settings.lastStatus, _settings.awakeMinutes,_settings.lastDate, _settings.offlineTillDate],
            onSuccess, onFailure);

    },

    _fetchSettings:function (settings, onSuccess, onFails){
        var functionToExecute = null;
        var model = this;
        for (var index = 0; index < settings.length; ++index) {
            var setting = settings[index];
            var nextFunctionToExecute = function(){
                model._fetchSetting(nextFunctionToExecute.setting.name,function(successText){
                    console.log("Fetching "+nextFunctionToExecute.setting.name + ' by using:'+ nextFunctionToExecute.a_name);
                    nextFunctionToExecute.setting.applyValue(successText);
                    var nextFunc = nextFunctionToExecute.next;
                    if (nextFunc!=null){
                        nextFunctionToExecute = nextFunc;
                    }
                    //call next... or prev
                    if (nextFunc != null){
                        nextFunc()
                    } else {
                        onSuccess()
                    }
                },onFails)
            };
            nextFunctionToExecute.a_name = "fetchCallback_"+setting.name;
            nextFunctionToExecute.next = functionToExecute;
            nextFunctionToExecute.setting = setting;
            functionToExecute = nextFunctionToExecute;
        }
        functionToExecute()
    },

    _fetchSetting: function (settingName, onSuccess, onFails){
        this._doRequest({
            type: "GET",
            url: this._serverUrl + '/server/moon/'+settingName
        }, function (response) {
            if (response.statusCode == 200) {
                onSuccess(response.resultText, response);
            } else {
                onFails(response.statusCode, response);
            }
        })
    },

    saveAwakeSeconds : function (value, whenSuccess, whenFails) {
        this._doRequest({
            type: "POST",
            url: this._serverUrl + '/server/moon/sleepminutes',
            data: "" + value
        }, function (response) {
            if (response.statusCode == 200) {
                this.awakeMinutes = parseInt(response.resultText);
                whenSuccess(this.awakeMinutes)
            } else {
                whenFails(response.statusCode);
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