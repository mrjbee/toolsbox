package org.monroe.team.toolsbox.security;


import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    public CustomBasicAuthenticationEntryPoint(String realmName) {
        setRealmName(realmName);
    }

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response,
                         final AuthenticationException authException) throws IOException, ServletException {

        String avoidHeader = request.getHeader("Avoid-WWW-Authenticate");
        if (avoidHeader == null){
            avoidHeader = request.getHeader("Access-Control-Request-Headers");
            if (avoidHeader != null){
                avoidHeader = avoidHeader.toLowerCase().contains("Avoid-WWW-Authenticate".toLowerCase())? "yes":"no";
            }
        }

        if (avoidHeader == null || !"yes".equals(avoidHeader.toLowerCase())){
            super.commence(request,response,authException);
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        }
    }
}
