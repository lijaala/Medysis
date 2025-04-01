package Medysis.Project.WebSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import Medysis.Project.Model.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocketHandler.class);
    private final Map<Integer, WebSocketSession> patientSessions = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> staffSessions = new ConcurrentHashMap<>(); // Map for staff sessions
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String uri = session.getUri().toString();
        Integer userId = getUserIdFromUri(uri, "userId");
        String staffId = getStaffIdFromUri(uri);

        if (userId != null) {
            patientSessions.put(userId, session);
            logger.info("WebSocket connection established for patient ID: {}", userId);
        } else if (staffId != null) {
            staffSessions.put(staffId, session);
            logger.info("WebSocket connection established for staff ID: {}", staffId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Integer userId = getUserIdFromUri(session.getUri().toString(), "userId");
        String staffId = getStaffIdFromUri(session.getUri().toString());

        if (userId != null) {
            patientSessions.remove(userId);
            logger.info("WebSocket connection closed for patient ID: {}", userId);
        } else if (staffId != null) {
            staffSessions.remove(staffId);
            logger.info("WebSocket connection closed for staff ID: {}", staffId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("Received message from session {}: {}", session.getId(), message.getPayload());
        // Handle client messages if needed
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    private Integer getUserIdFromUri(String uri, String paramName) {
        try {
            int index = uri.indexOf(paramName + "=");
            if (index > -1) {
                String paramValue = uri.substring(index + paramName.length() + 1);
                int endIndex = paramValue.indexOf('&');
                if (endIndex > -1) {
                    paramValue = paramValue.substring(0, endIndex);
                }
                return Integer.parseInt(paramValue);
            }
        } catch (NumberFormatException e) {
            logger.warn("Could not parse {} from WebSocket URI: {}", paramName, uri);
        } catch (StringIndexOutOfBoundsException e) {
            logger.warn("{} parameter not found in WebSocket URI: {}", paramName, uri);
        }
        return null;
    }

    private String getStaffIdFromUri(String uri) {
        try {
            int index = uri.indexOf("staffId=");
            if (index > -1) {
                return uri.substring(index + "staffId=".length());
            }
        } catch (StringIndexOutOfBoundsException e) {
            logger.warn("staffId parameter not found in WebSocket URI: {}", uri);
        }
        return null;
    }

    public void sendNotificationToPatient(Integer userId, Notifications notification) {
        WebSocketSession session = patientSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String jsonNotification = objectMapper.writeValueAsString(notification);
                session.sendMessage(new TextMessage(jsonNotification));
                logger.info("Sent real-time notification to patient ID: {} - {}", userId, notification.getMessage());
            } catch (IOException e) {
                logger.error("Error sending WebSocket message to patient ID {}: {}", userId, e.getMessage());
            }
        }
    }

    public void sendNotificationToStaff(String staffId, Notifications notification) {
        WebSocketSession session = staffSessions.get(staffId);
        if (session != null && session.isOpen()) {
            try {
                String jsonNotification = objectMapper.writeValueAsString(notification);
                session.sendMessage(new TextMessage(jsonNotification));
                logger.info("Sent real-time notification to staff ID: {} - {}", staffId, notification.getMessage());
            } catch (IOException e) {
                logger.error("Error sending WebSocket message to staff ID {}: {}", staffId, e.getMessage());
            }
        }
    }
}