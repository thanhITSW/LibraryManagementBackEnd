package nmtt.demo.service.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService{
    private final Cloudinary cloudinary;

    /**
     * Uploads an image file to Cloudinary.
     * This method accepts a file and a folder name, then uploads the file to the specified folder on Cloudinary.
     * Only images with the allowed formats (jpg, png, jpeg) are accepted.
     * The uploaded file will overwrite any existing file with the same name.
     *
     * @param file       The image file to upload.
     * @param folderName The folder name on Cloudinary where the file will be uploaded.
     * @return A map containing details about the uploaded file, including URL and other metadata.
     * @throws IOException If an error occurs during the file upload process.
     */
    @Override
    public Map uploadFile(MultipartFile file, String folderName) throws IOException {
        return cloudinary
                .uploader()
                .upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folderName,
                        "resource_type", "image",
                        "allowed_formats", new String[]{"jpg", "png", "jpeg"},
                        "overwrite", true
                ));
    }

    /**
     * Deletes a file from Cloudinary using its public ID.
     * This method checks if the public ID is not null or empty before attempting to delete the file.
     * If the file exists, it is destroyed from Cloudinary using the provided public ID.
     *
     * @param publicId The public ID of the file to be deleted.
     * @throws IOException If an error occurs during the file deletion process.
     */
    @Override
    public void deleteFile(String publicId) throws IOException {
        if (publicId != null && !publicId.isEmpty()) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }
}
