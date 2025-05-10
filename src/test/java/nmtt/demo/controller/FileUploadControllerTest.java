package nmtt.demo.controller;

import nmtt.demo.service.cloudinary.CloudinaryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class FileUploadControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CloudinaryService cloudinaryService;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUploadImage_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "image-content".getBytes());

        String folderName = "test-folder";
        Map<String, Object> uploadedResponse = Map.of(
                "url", "https://example.com/test.jpg"
        );

        when(cloudinaryService.uploadFile(any(MultipartFile.class), eq(folderName)))
                .thenReturn(uploadedResponse);

        mockMvc.perform(multipart("/api/files/upload/image")
                        .file(file)
                        .param("folder", folderName)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://example.com/test.jpg"));

        Mockito.verify(cloudinaryService, Mockito.times(1)).uploadFile(any(MultipartFile.class), eq(folderName));
    }
}
