package org.monroe.team.toolsbox.transport.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PingController {

    @RequestMapping(value = "/ping", method = RequestMethod.GET, produces="text/plain")
    @ResponseBody
    public String ping(){
        return "Pong [rest is up]";
    }


    @RequestMapping(value = "/secure-ping", method = RequestMethod.GET, produces="text/plain")
    @ResponseBody
    public String securePing(){
        return "Pong [secure rest is up]";
    }

}
