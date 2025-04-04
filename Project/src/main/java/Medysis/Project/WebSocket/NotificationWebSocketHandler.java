package Medysis.Project.WebSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import Medysis.Project.Model.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocketHandler.class);
    private final Map<Integer, WebSocketSession> patientSessions = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> staffSessions = new ConcurrentHashMap<>(); // Map for staff sessions
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final String instanceId = UUID.randomUUID().toString(); // Add this

    public NotificationWebSocketHandler() {
        logger.info("NotificationWebSocketHandler instance created: {}", instanceId); // Log creation
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String uri = session.getUri().toString();
        Integer userId = getUserIdFromUri(uri, "userId");
        String staffId = getStaffIdFromUri(uri);

        if (userId != null) {
            patientSessions.put(userId, session);
            logger.info("WebSocket connection established for patient ID: {}", userId);
            logger.debug("Current patientSessions: {}", patientSessions.keySet());
            logger.debug("Patient URI: {}", uri); // Log the full URI for patient
            logger.debug("Extracted userId: {}", userId); // Log the extracted userId
            logger.debug("Current patientSessions keys: {}", patientSessions.keySet()); // Log the keys in patientSessions
        } else if (staffId != null) {
            staffSessions.put(staffId, session);
            logger.info("WebSocket connection established for staff ID: {}", staffId);
            logger.debug("Current Sessions: {}", staffSessions.keySet());
            logger.debug("URI: {}", uri); // Log the full URI for staff
            logger.debug("Extracted staffId: {}", staffId); // Log the extracted staffId
            logger.debug("Current staffSessions keys: {}", staffSessions.keySet()); // Log the keys in staffSessions
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Integer userId = getUserIdFromUri(session.getUri().toString(), "userId");
        String staffId = getStaffIdFromUri(session.getUri().toString());

        if (userId != null) {
            patientSessions.remove(userId);
            logger.info("WebSocket connection closed for patient ID: {}", userId);
            logger.debug("Current patientSessions after removal: {}", patientSessions.keySet()); // TROUBLESHOOTING
        } else if (staffId != null) {
            staffSessions.remove(staffId);
            logger.info("WebSocket connection closed for staff ID: {}", staffId);
            logger.debug("Current staffSessions after removal: {}", staffSessions.keySet()); // TROUBLESHOOTING
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
                String staffIdEncoded = uri.substring(index + "staffId=".length());
                int endIndex = staffIdEncoded.indexOf('&');
                if (endIndex > -1) {
                    staffIdEncoded = staffIdEncoded.substring(0, endIndex);
                }
                return URLDecoder.decode(staffIdEncoded, StandardCharsets.UTF_8); // Decode here
            }
        } catch (StringIndexOutOfBoundsException e) {
            logger.warn("staffId parameter not found in WebSocket URI: {}", uri);
        }
        return null;
    }

    public void sendNotificationToPatient(Integer userId, Notifications notification) {
        WebSocketSession session = patientSessions.get(userId);
        logger.debug("Attempting to send to patient ID: {}, Session found: {}, Session open: {}", userId, (session != null), (session != null && session.isOpen())); // TROUBLESHOOTING
        if (session != null && session.isOpen()) {
            try {
                String jsonNotification = objectMapper.writeValueAsString(notification);
                session.sendMessage(new TextMessage(jsonNotification));
                logger.info("Sent real-time notification to patient ID: {} - {}", userId, notification.getMessage());
            } catch (IOException e) {
                logger.error("Error sending WebSocket message to patient ID {}: {}", userId, e.getMessage());
            }
        } else {
            logger.warn("No open WebSocket session found for patient ID: {}", userId);
        }
    }

    public void sendNotificationToStaff(String staffId, Notifications notification) {
        WebSocketSession session = staffSessions.get(staffId);
        logger.debug("Attempting to send to staff ID: {} on instance: {}, Session found: {}, Session open: {}", staffId, instanceId, (session != null), (session != null && session.isOpen()));
        if (session != null && session.isOpen()) {
            try {
                String jsonNotification = objectMapper.writeValueAsString(notification);
                session.sendMessage(new TextMessage(jsonNotification));
                logger.info("Sent real-time notification to staff ID: {} - {}", staffId, notification.getMessage());
            } catch (IOException e) {
                logger.error("Error sending WebSocket message to staff ID {}: {}", staffId, e.getMessage());
            }
        } else {
            logger.warn("No open WebSocket session found for staff ID: {}", staffId);
        }
    }
}