var TaskWidgetFactoryPrototype = {
    createFor:function(task,presenter){
        var widget;
        if (task.type == "COPY"){
            widget = Object.create(GenericTaskWidgetPrototype);
            widget.constructor(task,presenter,{
                renderBody:function(body, task){
                    body.append("<b>Copy</b> <span class='raw-data'>"+task.details["src"]+"</span> in to <span class='raw-data'>"+task.details["dst"]+"</span>");
                }
            });
        } else if (task.type == "DOWNLOAD"){
            widget = Object.create(GenericTaskWidgetPrototype);
            widget.constructor(task,presenter,{
                renderBody:function(body, task){
                    body.append("<b>Download</b> <span class='raw-data'>"+task.details["name"]+"</span> in to <span class='raw-data'>"+task.details["dst"]+"</span>");
                }
            });
        } else {
            throw Error("Unsupported type:"+task.type)
        }

        return widget;
    }
};

var GenericTaskWidgetPrototype = {

    _task:null,
    _content:null,
    _progressbar:null,
    _statusLabel:null,
    _estimationLabel:null,
    _speedLabel:null,
    _footer:null,
    _header:null,
    _actionBtn:null,
    _owner:null,
    _renderer:null,

    constructor : function(taskToRef,owner,renderer){
        this._task = taskToRef;
        this._owner=owner;
        this._renderer = renderer;
    },


    close: function(container){
        this._content.slideUp('normal', function() { $(this).remove(); });
    },

    show: function(container){
        this._content = $(document.createElement("div"));
        this._content.addClass("task-panel")
        this._content.attr("id","root-task-"+this._task.taskId+"-content");
        this._content.css("display","none");
        container.append(this._content);
        this._render();
        this.update(this._task, true);
        this._content.fadeIn();
    },

    update: function(task, firstTime){
        this._task = task;
        this._statusLabel.text(this._task.status);
        this._speedLabel.text(this._task.details.speed);
        this._estimationLabel.text(this._task.endDate);
        this._progressbar.val(Math.round(this._task.progress*100));
        this._progressbar.slider("refresh");

        if (this._task.status != "Fails" && this._task.status != "Killed") {
            this._progressbar.parent().find('.ui-state-disabled').removeClass('ui-state-disabled');
        }

        if (this._task.status == "Finished" && !firstTime){
            this._footer.slideUp();
            this._header.slideUp();
        }
       this._updateIcon();
    },

    _updateIcon:function(){
        //Pending, Progress, Finished, Fails, Killed
        this._actionBtn.removeClass("ui-icon-arrow-u-r");
        this._actionBtn.removeClass("ui-icon-delete");

        if (this._task.status == "Progress" || this._task.status == "Pending"){
            this._actionBtn.addClass("ui-icon-delete");
        } else {
            this._actionBtn.addClass("ui-icon-arrow-u-r");
        }
    },


    _render: function () {

        this._content.addClass("ui-corner-all").addClass("ui-body").addClass("ui-body-a");


        this._header = $(document.createElement("div")).addClass("ui-bar").addClass("ui-bar-b").addClass("task-header");
        var body = $(document.createElement("div")).addClass("ui-body").addClass("ui-body-a").addClass("task-body");
        this._footer = $(document.createElement("div")).addClass("ui-bar").addClass("ui-bar-b").addClass("task-footer");
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
        caption.append("");
        this._actionBtn =  $(document.createElement("a"));
        this._actionBtn
            .addClass("ui-btn")
            .addClass("ui-btn-a")
            .addClass("ui-icon-delete")
            .addClass("ui-btn-icon-notext")
            .addClass("ui-corner-all")
            .addClass("ui-btn-inline");
        this._actionBtn.click(function(event){
            this._owner.onTaskActionBtn(this._task,this)
        }.bind(this));

        filedset.append(caption);
        filedset.append(this._progressbar);

        this._header.append(filedset);
        body.append(this._actionBtn);

        this._renderer.renderBody(body,this._task);

        var statusLabel = $(document.createElement("label"));
        this._statusLabel = $(document.createElement("span")).addClass("raw-data");
        this._speedLabel = $(document.createElement("span")).addClass("raw-data");
        this._estimationLabel = $(document.createElement("span")).addClass("raw-data");

        this._footer.append(statusLabel);
        statusLabel.append(" Status");
        statusLabel.append(this._statusLabel);
        statusLabel.append("Estimation");
        statusLabel.append(this._estimationLabel);
        statusLabel.append("Speed");
        statusLabel.append(this._speedLabel);

        this._content.append(this._header);
        this._content.append(body);
        this._content.append(this._footer);
        this._progressbar.slider();
        this._progressbar.parent().find('.ui-state-disabled').removeClass('ui-state-disabled');
    }

};