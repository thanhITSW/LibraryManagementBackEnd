package nmtt.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import nmtt.demo.service.cloudinary.CloudinaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource("/test.properties")
public class CloudinaryServiceTest {
    @Autowired
    private CloudinaryService cloudinaryService;

    @MockBean
    private Cloudinary cloudinary;

    @MockBean
    private Uploader uploader;

    @Test
    public void testUploadFile() throws IOException {
        // Arrange: Set up the mock behavior
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3});
        String folderName = "test-folder";

        // Mocking the Cloudinary uploader behavior
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("url", "http://example.com/test.jpg");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(expectedResponse);

        // Act: Call the method under test
        Map<String, Object> result = cloudinaryService.uploadFile(file, folderName);

        // Assert: Verify the result and interactions
        assertNotNull(result);
        assertEquals("http://example.com/test.jpg", result.get("url"));
        verify(cloudinary, times(1)).uploader();
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    @Test
    public void testDeleteFile() throws IOException {
        // Arrange: Set up the mock behavior
        String publicId = "test_public_id";

        when(cloudinary.uploader()).thenReturn(uploader);

        // Mocking the Cloudinary uploader's destroy behavior
        when(uploader.destroy(eq(publicId), eq(ObjectUtils.emptyMap()))).thenReturn(new HashMap<>()); // Return a mock map

        // Act: Call the method under test
        cloudinaryService.deleteFile(publicId);

        // Assert: Verify that the destroy method was called with the correct publicId
        verify(cloudinary, times(1)).uploader();
        verify(uploader, times(1)).destroy(eq(publicId), eq(ObjectUtils.emptyMap()));
    }

    @Test
    public void testDeleteFileWhenPublicIdIsNull() throws IOException {
        cloudinaryService.deleteFile(null);

        verify(cloudinary, never()).uploader();
    }

    @Test
    public void testDeleteFileWhenPublicIdIsEmpty() throws IOException {
        cloudinaryService.deleteFile("");

        verify(cloudinary, never()).uploader();
    }

}
