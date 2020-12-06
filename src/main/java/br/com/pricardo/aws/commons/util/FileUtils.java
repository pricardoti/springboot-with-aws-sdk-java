package br.com.pricardo.aws.commons.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

    public static final File convertMultiPartToFile(MultipartFile file) throws IOException {
        final File convFile = new File(file.getOriginalFilename());
        fileOutPutStream(file, convFile);
        return convFile;
    }

    public static final void fileOutPutStream(MultipartFile file, File convFile) throws IOException {
        final FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
    }
}
