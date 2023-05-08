package com.task.needmoretask.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.needmoretask.core.auth.jwt.MyJwtProvider;
import com.task.needmoretask.dto.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutSuccessHandler {
    private final MyJwtProvider myJwtProvider;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String token = MyJwtProvider.resolveToken(request);
        response.setContentType("application/json; charset=utf-8");
        if(token == null) throw new IOException("토큰이 없습니다");
        else if(MyJwtProvider.validateToken(token)) myJwtProvider.invalidateToken(token);
        ResponseDTO<?> responseDto = new ResponseDTO<>();
        ObjectMapper om = new ObjectMapper();
        String responseBody = om.writeValueAsString(responseDto);
        response.getWriter().println(responseBody);
    }
}
