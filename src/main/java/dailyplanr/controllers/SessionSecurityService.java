package dailyplanr.controllers;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Component
public class SessionSecurityService {
    
    public void enhanceSessionSecurity(HttpSession session, HttpServletRequest request) {
        // Store client fingerprint
        String userAgent = request.getHeader("User-Agent");
        String clientIP = getClientIP(request);
        
        session.setAttribute("userAgent", userAgent);
        session.setAttribute("clientIP", clientIP);
    }
    
    public boolean validateSessionIntegrity(HttpSession session, HttpServletRequest request) {
        String storedUserAgent = (String) session.getAttribute("userAgent");
        String currentUserAgent = request.getHeader("User-Agent");
        
        String storedIP = (String) session.getAttribute("clientIP");
        String currentIP = getClientIP(request);
        
        // Basic integrity check
        return storedUserAgent != null && 
               storedUserAgent.equals(currentUserAgent) &&
               storedIP != null && 
               storedIP.equals(currentIP);
    }
    
    private String getClientIP(HttpServletRequest request) {
    	 String ip = request.getHeader("X-Forwarded-For");
    	    if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
    	        return ip.split(",")[0].trim();
    	    }
    	    return request.getRemoteAddr();
    }
}