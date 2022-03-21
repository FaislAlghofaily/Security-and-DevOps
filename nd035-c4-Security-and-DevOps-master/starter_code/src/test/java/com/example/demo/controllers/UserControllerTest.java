package com.example.demo.controllers;


import com.example.demo.model.persistence.User;
import com.example.demo.model.requests.CreateUserRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class UserControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<User> userJson;
    @Autowired
    private JacksonTester<CreateUserRequest> createJson;

    @Test
    public void createUserHappyPath() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("Test");
        createUserRequest.setPassword("qertyui");
        createUserRequest.setConfirmPassword("qertyui");
        mvc.perform(
                        MockMvcRequestBuilders.post("/api/user/create").content(createJson.write(createUserRequest).getJson())
                                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username", is("Test"))).andReturn();

    }

    @Test
    public void createUserBadPassword() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("TestBadPassword");
        createUserRequest.setPassword("q");
        createUserRequest.setConfirmPassword("q");
        mvc.perform(
                        MockMvcRequestBuilders.post("/api/user/create").content(createJson.write(createUserRequest).getJson())
                                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void unAuthinticatedRequest() throws Exception {
        User user = createUser("notAllowed", "password");

        mvc.perform(MockMvcRequestBuilders.get("/api/user/id/" + String.valueOf(user.getId()))).andExpect(status().isUnauthorized());

    }

    @Test
    public void getById() throws Exception {
        User user = createUser("getByid", "getByIdPassword");

        String authKey = getAuthKeyForUser(user);

        mvc.perform(MockMvcRequestBuilders.get("/api/user/id/" + String.valueOf(user.getId())).header("Authorization",
                authKey)).andExpect(status().isOk()).andExpect(jsonPath("$.username", is(user.getUsername())));

    }
    @Test
    public void getByUsername() throws Exception {
        User user = createUser("getByUsername", "getByUsernamePassword");

        String authKey = getAuthKeyForUser(user);

        mvc.perform(MockMvcRequestBuilders.get("/api/user/" + user.getUsername()).header("Authorization",
                authKey)).andExpect(status().isOk()).andExpect(jsonPath("$.username", is(user.getUsername())));

    }


    public User createUser(String username, String password) throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername(username);
        createUserRequest.setPassword(password);
        createUserRequest.setConfirmPassword(password);

        MvcResult result = mvc
                .perform(MockMvcRequestBuilders.post("/api/user/create")
                        .content(createJson.write(createUserRequest).getJson()).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username", is(username))).andReturn();
        MockHttpServletResponse response = result.getResponse();

        User user = new User();
        user.setId(Integer.parseInt(response.getContentAsString().replaceAll("[\\D]", "")));
        user.setUsername(username);
        user.setPassword(password);
        return user;

    }

    public String getAuthKeyForUser(User user) throws Exception {
        String body = "{\"username\":\"" + user.getUsername() + "\", \"password\":\"" + user.getPassword() + "\"}";

        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/login").content(body)).andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        return response.getHeader("Authorization").toString();

    }

}
