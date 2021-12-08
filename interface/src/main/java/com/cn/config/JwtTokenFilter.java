package com.cn.config;

import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.cn.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 校验token过滤器
 * @author ngcly
 * @since 2020/5/13 16:35
 * @version V1.0
 */
@Component
public class JwtTokenFilter extends OncePerRequestFilter {
    private static final Log log = LogFactory.get();

    private final JwtTokenUtil jwtTokenUtil;

    public JwtTokenFilter(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = JwtTokenUtil.getToken(request);
        if(StringUtils.hasLength(token)){
            User user = null;
            LocalDateTime issuedAt = null;
            try {
                JWT jwt = JWTUtil.parseToken(token);
                if(jwtTokenUtil.verify(jwt)){
                    user = jwt.getPayload().getClaimsJson().toBean(User.class);
                    issuedAt = DateUtil.toLocalDateTime(jwt.getPayloads().getDate(JWT.ISSUED_AT));
                }
            } catch (Exception e) {
                log.info("token 无效:{}", e.getMessage());
            }

            if(validUserAuthenticated(user,issuedAt)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                log.info("authenticated user:{}", authenticationToken);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request,response);
    }

    /**
     * 判断是否已授权
     * @param userDetail 用户信息
     * @param issuedAt jwt有效期
     * @return boolean
     */
    public boolean validUserAuthenticated(User userDetail,LocalDateTime issuedAt){
        return Objects.nonNull(userDetail)
                && Objects.isNull(SecurityContextHolder.getContext().getAuthentication())
                && Objects.nonNull(issuedAt)
                && (Objects.isNull(userDetail.getPwdAlt()) || issuedAt.isAfter(userDetail.getPwdAlt()));
    }

}