package com.example.demo.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {
    public static final String ERROR_VIEW = "error";
    Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(value = {Exception.class})
    public Object errorHandler(HttpServletRequest request, HttpServletResponse response, Exception e) throws Exception {
        logger.error(e.getMessage());
        if (isAjax(request)) {
            return JSONResult.errorException(e.getMessage());
        } else {
            ModelAndView mav = new ModelAndView();
            mav.addObject("exception", e);
            mav.addObject("url", request.getRequestURL());
            mav.setViewName(ERROR_VIEW);
            return mav;
        }
    }

    private boolean isAjax(HttpServletRequest request) {
        final String ajaxHeaderKey = "X-Requested-With";
        return request.getHeader(ajaxHeaderKey) != null && "XMLHttpRequest".equals(request.getHeader(ajaxHeaderKey));
    }
}
