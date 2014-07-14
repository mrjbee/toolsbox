package org.monroe.team.toolsbox.services;

import org.monroe.team.toolsbox.us.common.BusinessExceptions;

import javax.inject.Named;

@Named
public class IdTranslator {

    public Integer asInt(String id) throws BusinessExceptions.InvalidIdException {
        try {
            return  Integer.parseInt(id);
        }catch (Exception e){
            throw new BusinessExceptions.InvalidIdException(""+id);
        }
    }
}
