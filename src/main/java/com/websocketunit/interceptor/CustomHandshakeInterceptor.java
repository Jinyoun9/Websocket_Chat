package com.websocketunit.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component // Spring 빈으로 등록
public class CustomHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // URI에서 쿼리 파라미터 추출
        String query = request.getURI().getQuery();
        if (query != null) {
            // UriComponentsBuilder를 사용해 쿼리 파라미터를 분석
            Map<String, String> queryParams = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().toSingleValueMap();
            String roomId = queryParams.get("roomId");

            if (roomId != null) {
                attributes.put("roomId", roomId); // roomId를 WebSocket 세션에 저장
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // afterHandshake가 필요한 경우 여기에 구현
    }
}
