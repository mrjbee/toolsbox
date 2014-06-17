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
        awakeSleep:$("#awake-flip"),
        awakeMinutesLabel:$("#awake-minutes-label"),
        awakeMinutesSlider:$("#awake-second-slider"),
        lastOnlineDateLabel:$("#last-online-date-label"),
        statusLabel:$("#status-label"),
        offlineTillDateLabel:$("#offline-till-date-label"),
        lockPanel: $("#lockPanel"),
        refreshBtn:$("#refresh-btn")
    });
    //TODO: disbale form submission. Place it in presenter
    $("#login-form").submit(function() {
        return false;
    });

    presenter.initial();
}