package org.monroe.team.toolsbox.services;

import org.monroe.team.toolsbox.transport.TransportExceptions;

import javax.inject.Named;

@Named
public class IdTranslator {

    public Integer asInt(String id) throws TransportExceptions.InvalidIdException {
        try {
            return  Integer.parseInt(id);
        }catch (Exception e){
            throw new TransportExceptions.InvalidIdException(""+id);
        }
    }
}
