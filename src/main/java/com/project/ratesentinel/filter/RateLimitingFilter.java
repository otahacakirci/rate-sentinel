package com.project.ratesentinel.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.project.ratesentinel.service.RateLimiterService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

	private static final String CLIENT_ID_HEADER = "X-Client-Id";

	private final RateLimiterService rateLimiterService;

	public RateLimitingFilter(RateLimiterService rateLimiterService) {
		this.rateLimiterService = rateLimiterService;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String clientId = request.getHeader(CLIENT_ID_HEADER);

		if (clientId == null || clientId.isBlank()) {
			writeError(response, HttpStatus.BAD_REQUEST.value(), "Missing X-Client-Id header");
			return;
		}

		if (!rateLimiterService.isAllowed(clientId, request.getRequestURI())) {
			writeError(
				response,
				HttpStatus.TOO_MANY_REQUESTS.value(),
				"Too Many Requests - Rate limit exceeded");
			return;
		}

		filterChain.doFilter(request, response);
	}

	private void writeError(HttpServletResponse response, int status, String message)
			throws IOException {
		response.setStatus(status);
		response.setContentType(MediaType.TEXT_PLAIN_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write(message);
	}
}
