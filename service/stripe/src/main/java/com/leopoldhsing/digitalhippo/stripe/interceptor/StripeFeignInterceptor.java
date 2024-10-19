package com.leopoldhsing.digitalhippo.stripe.interceptor;

import com.leopoldhsing.digitalhippo.common.constants.AuthConstants;
import com.leopoldhsing.digitalhippo.common.utils.RequestUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class StripeFeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Long userId = RequestUtil.getUid();
        requestTemplate.header(AuthConstants.USERID_HEADER_KEY, String.valueOf(userId));
    }
}