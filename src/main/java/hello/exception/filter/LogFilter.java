package hello.exception.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class LogFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("filter initialize");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String requestURI = request.getRequestURI();
        String uuid = UUID.randomUUID().toString();

        try{
            log.info("REQUEST [{}][{}][{}]",
                    uuid, request.getDispatcherType(), requestURI);
            filterChain.doFilter(servletRequest, servletResponse);
        }catch (Exception e){
            throw e;
        }finally {
            log.info("RESPONSE [{}][{}][{}]",
                    uuid, request.getDispatcherType(), requestURI);
        }
    }

    @Override
    public void destroy() {
        log.info("filter destroyed");
    }
}
