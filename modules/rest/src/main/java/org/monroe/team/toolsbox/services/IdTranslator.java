package org.monroe.team.toolsbox.services;

import org.monroe.team.toolsbox.us.common.Exceptions;

import javax.inject.Named;

@Named
public class IdTranslator {

    public Integer asInt(String id) throws Exceptions.InvalidIdException {
        try {
            return  Integer.parseInt(id);
        }catch (Exception e){
            throw new Exceptions.InvalidIdException(""+id);
        }
    }
}
