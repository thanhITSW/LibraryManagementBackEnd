package nmtt.demo.service.cloudinary;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {
    public Map uploadFile(MultipartFile file, String folderName) throws IOException;

    public void deleteFile(String publicId) throws IOException;
}
