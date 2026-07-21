package com.classroom.products;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

	private static final Pattern TOKEN_PATTERN = Pattern.compile("\"accessToken\"\\s*:\\s*\"([^\"]+)\"");

	@Autowired
	private MockMvc mockMvc;

	private String login(String username, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
				.andExpect(status().isOk())
				.andReturn();

		Matcher matcher = TOKEN_PATTERN.matcher(result.getResponse().getContentAsString());
		if (!matcher.find()) {
			throw new IllegalStateException("No accessToken in login response");
		}
		return matcher.group(1);
	}

	@Test
	void login_withValidCredentials_returnsBearerToken() throws Exception {
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"admin\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.accessToken").value(notNullValue()));
	}

	@Test
	void login_withInvalidCredentials_returnsUnauthorized() throws Exception {
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void login_withBlankFields_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"\",\"password\":\"\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void protectedEndpoint_withoutToken_returnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/products"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void protectedEndpoint_withValidToken_returnsOk() throws Exception {
		String token = login("user", "password");
		mockMvc.perform(get("/api/products")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
	}

	@Test
	void protectedEndpoint_withInvalidToken_returnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/products")
				.header("Authorization", "Bearer not-a-real-token"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void writeEndpoint_asUserRole_returnsForbidden() throws Exception {
		String token = login("user", "password");
		mockMvc.perform(post("/api/products")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"category\":\"Mobile\",\"productName\":\"Phone\",\"brand\":\"Acme\",\"price\":99.0}")
				.with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	void writeEndpoint_asAdminRole_returnsCreated() throws Exception {
		String token = login("admin", "admin");
		mockMvc.perform(post("/api/products")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"category\":\"Mobile\",\"productName\":\"Phone\",\"brand\":\"Acme\",\"price\":99.0}")
				.with(csrf()))
				.andExpect(status().isCreated());
	}
}
