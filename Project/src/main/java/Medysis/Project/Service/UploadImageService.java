package Medysis.Project.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
@Service
public class UploadImageService {
    private static final String IMAGE_FOLDER = "src/main/resources/static/image";

    public String saveImage(MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            return null;
        }
        File directory = new File(IMAGE_FOLDER);
        if (!directory.exists()) {
            directory.mkdirs();

        }
        try {
            String originalFileName = photo.getOriginalFilename();
            String newfileName = originalFileName.replaceAll("\\s+", "");

            String extension = "";
            int dotIndex = newfileName.lastIndexOf(".");
            if (dotIndex >= 0) {
                extension = newfileName.substring(dotIndex);
                newfileName = newfileName.substring(0, dotIndex);
            }
            String dateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String saveFileName = newfileName + "_" + dateTime + extension;

            Path path = Paths.get(IMAGE_FOLDER, saveFileName);

            byte[] bytes = photo.getBytes();
            Files.write(path, bytes);

            return saveFileName;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
