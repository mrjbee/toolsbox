
var PresenterPrototype = {

    _lastMinutes: -1,
    _model: null,
    _view: null,

    constructor:function _constructor(model,view){
        this._model = model;
        this._view = view;

        this._view.loginBtn.click(function(){
            this.initiateLogin();
        }.bind(this));

        this._view.refreshBtn.click(function(){
            this._initiateModelUpdate();
        }.bind(this));

        this._view.awakeMinutesSlider.parent().change(function() {
            var minutesValue = this._view.awakeMinutesSlider.val();
            this.updateAwakeSecondsUI(minutesValue);
        }.bind(this));
        this._view.awakeMinutesSlider.slider({
            stop: function( event, ui ) {
                var minutesValue = this._view.awakeMinutesSlider.val();
                this._saveAwakeMinutesSetting(minutesValue);
            }.bind(this)
        });
        this._view.awakeSleep.bind( "change", function(event, ui) {
            if (this._view.awakeSleep.val()=="on"){
                minutesValue = this._view.awakeMinutesSlider.val();
                this._saveAwakeMinutesSetting(minutesValue);
            } else {
                this._saveAwakeMinutesSetting(0);
            }
        }.bind(this));
    },

    updateAwakeSecondsUI : function(minutes){

        if (this._lastMinutes == minutes) return;

        this._lastMinutes = minutes;

        if(minutes == 0){
            this._view.awakeMinutesLabel.text("stay up");
            this._view.awakeMinutesSlider.slider("disable");
            this._view.awakeSleep.val("off");
            this._view.awakeSleep.flipswitch( "refresh" );
        } else {
            hr = Math.floor(minutes/60);
            mins= minutes % 60;
            if (hr == 0){
                this._view.awakeMinutesLabel.text(mins+"min");
            } else {
                this._view.awakeMinutesLabel.text(hr+" hr "+mins+"min");
            }
            this._view.awakeMinutesSlider.slider("enable");
            this._view.awakeMinutesSlider.val(minutes);
            this._view.awakeMinutesSlider.slider("refresh");
            this._view.awakeSleep.val("on");
            this._view.awakeSleep.flipswitch( "refresh" );
        }
    },


    _saveAwakeMinutesSetting: function (minutesValue) {
        this._lockUI(true);
        this._model.saveAwakeSeconds(minutesValue,
            function(minutes){
                this._unlockUI();
                this.updateAwakeSecondsUI(minutes);
            }.bind(this),
            function(statusCode){
                this._unlockUI();
                this._askForReLogin(statusCode);
            }.bind(this)
        );
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
        this._lockUI();
        this._model.updateDetails(
            function(){
                this._unlockUI();
                this._updatedStatistic()
            }.bind(this),
            function(statusCode){
                this._view.lockPanel.fadeOut();
                $.mobile.loading( "hide");
                this._askForReLogin(statusCode)
            }.bind(this)
        );
    },

    _askForReLogin : function(statusCode){
        this._view.infolabel.text("Authorization fails! Try again...")
        if (statusCode != null){
            this._view.infolabel.text("Error ("+statusCode+") ! Please try again later...")
        }
        this._view.authDialog.popup("open");
        this._view.infolabel.slideDown().delay(1000).fadeOut(400);
    },

    _updatedStatistic : function(){
        this.updateAwakeSecondsUI(this._model.awakeMinutes)
        this._view.statusLabel.text(this._model.lastStatus)
        this._view.lastOnlineDateLabel.text(this._model.lastDate)
        this._view.offlineTillDateLabel.text(this._model.offlineTillDate)
    }
}