package hello.exception.resolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Slf4j
public class MyHandlerExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(
            HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) {
        try{
            log.info("illegal access bad request");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            return new ModelAndView();

        }catch(IOException e){
            log.info("resolve ex", e);
        }
        return null;
    }
}
