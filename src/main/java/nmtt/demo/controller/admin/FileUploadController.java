package nmtt.demo.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.service.cloudinary.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("${admin-mapping}/api/files")
@Slf4j
@RequiredArgsConstructor
public class FileUploadController {
    private final CloudinaryService cloudinaryService;

    /**
     * Uploads an image to a specified folder in Cloudinary.
     *
     * @param file       The image file to upload.
     * @param folderName The name of the folder where the image will be stored.
     * @return The result of the upload containing details like the secure URL.
     * @throws IOException If an error occurs during file upload.
     */
    @PostMapping("/upload/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file,
                                         @RequestParam("folder") String folderName) throws IOException {
        return ResponseEntity.ok(cloudinaryService.uploadFile(file, folderName));
    }
}
