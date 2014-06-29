var TaskWidgetFactoryPrototype = {
    createFor:function(task){
        var widget;
        if (task.type == "COPY"){
            widget = Object.create(CopyTaskWidgetPrototype);
            widget.constructor(task);
        } else {
            throw Error("Unsupported type:"+task.type)
        }

        return widget;
    }
};

var CopyTaskWidgetPrototype = {

    _task:null,
    _content:null,
    _progressbar:null,
    _statusLabel:null,
    _estimationLabel:null,
    _speedLabel:null,

    constructor : function(taskToRef){
        this._task = taskToRef;
    },


    close: function(container){

    },

    show: function(container){
        this._content = $(document.createElement("div"));
        this._content.addClass("task-panel")
        this._content.attr("id","root-task-"+this._task.taskId+"-content");
        this._content.css("display","none");
        container.append(this._content);
        this._render();
        this.update(this._task);
        this._content.fadeIn();
    },

    update: function(task){
        this._task = task;
        this._statusLabel.text(this._task.status);
        this._speedLabel.text("NaN");
        this._estimationLabel.text("NaN");
        this._progressbar.val(Math.round(this._task.progress*100));
        this._progressbar.slider("refresh");
    },

    _render: function () {

        this._content.addClass("ui-corner-all");
        this._content.addClass("ui-body");
        this._content.addClass("ui-body-a");


        var header = $(document.createElement("div")).addClass("ui-bar").addClass("ui-bar-a").addClass("task-header");
        var body = $(document.createElement("div")).addClass("ui-body").addClass("ui-body-a").addClass("task-body");
        var footer = $(document.createElement("div")).addClass("ui-bar").addClass("ui-bar-a").addClass("task-footer");
        var filedset = $(document.createElement("fieldset")).addClass("progress-bar");
        this._progressbar = $(document.createElement("input"))
            .attr("name","slider-2")
            .attr("data-highlight","true")
            .attr("min","0")
            .attr("max","100")
            .attr("value","50")
            .attr("type","range")
            .attr("data-mini","true")
            .attr("disabled","true");
        var caption =  $(document.createElement("label"));
        caption.append("Copy");
        filedset.append(caption);
        filedset.append(this._progressbar);
        header.append(filedset);
        body.append("<span class='raw-data'>"+this._task.details["src"]+"</span> in to <span class='raw-data'>"+this._task.details["dst"]+"</span>");

        var statusLabel = $(document.createElement("label"));
        this._statusLabel = $(document.createElement("span")).addClass("raw-data");
        this._speedLabel = $(document.createElement("span")).addClass("raw-data");
        this._estimationLabel = $(document.createElement("span")).addClass("raw-data");

        footer.append(statusLabel);
        statusLabel.append("Status");
        statusLabel.append(this._statusLabel);
        statusLabel.append("Estimation");
        statusLabel.append(this._estimationLabel);
        statusLabel.append("Speed");
        statusLabel.append(this._speedLabel);

        this._content.append(header);
        this._content.append(body);
        this._content.append(footer);
        this._progressbar.slider();
        this._progressbar.parent().find('.ui-state-disabled').removeClass('ui-state-disabled');
    }

};