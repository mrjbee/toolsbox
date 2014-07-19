package org.monroe.team.toolsbox.transport.rest;

import org.monroe.team.toolsbox.us.ExploreDownloadUrlDefinition;
import org.monroe.team.toolsbox.transport.TransportExceptions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
public class DownloadController {

    @Inject
    public ExploreDownloadUrlDefinition exploreDownloadUrl;

    @RequestMapping(value = "downloads/details",method = RequestMethod.POST)
    public @ResponseBody
    ExploreDownloadUrlDefinition.ExploreDownloadUrlResponse buildDetails(@RequestBody BuildRequestDTO requestDTO){
        try {
            return exploreDownloadUrl.perform(requestDTO.url);
        } catch (ExploreDownloadUrlDefinition.UnreachableUrlException e) {
            throw new TransportExceptions.InvalidIdException(requestDTO.url);
        }
    }

    public static class BuildRequestDTO{
        public String url;
    }

}
