package com.jetam6.Archeus;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetam6.ArcheusModel.ArcheusUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


@SpringBootTest // Spustí celú Spring Boot aplikáciu
@AutoConfigureMockMvc // Aktivuje a nakonfiguruje MockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

//    @Test
//    public void testRegisterUser() throws Exception {
//        ArcheusUser newUser = ArcheusUser.builder()
//                .username("novyusertest")
//                .email("testuser@example.com")
//                .password("testpassword123")
//                .role(ArcheusUser.Role.ROLE_USER)
//                .build();
//
//        mockMvc.perform(post("/api/users/register") // ✅ uprav, ak máš inú URL
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(newUser)))
//                .andExpect(status().isOk()); // alebo isCreated(), podľa tvojej odpovede
//    }
}
