package com.mokujin.government.service.impl;


import com.mokujin.government.model.exception.extention.FileDeletionFailureException;
import com.mokujin.government.model.exception.extention.FileUploadFailureException;
import com.mokujin.government.service.FileService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static java.util.Optional.of;
import static org.h2.util.IOUtils.copy;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${file.storage.path}")
    private String fileFolderPath;

    @SneakyThrows
    @Override
    public String saveFile(MultipartFile multipartFile) {

        String fileName = generateFileName();

        File file = new File(fileFolderPath + fileName);
        file.createNewFile();

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            copy(multipartFile.getInputStream(), fileOutputStream);
        } catch (Exception e) {
            log.error("Fail to save on disc multipart file, e = " + e);
            file.delete();
            throw new FileUploadFailureException();
        }

        return fileName;
    }

    @Override
    public void deleteFile(String fileName) {
        of(new File(fileFolderPath + fileName))
                .filter(File::delete)
                .orElseThrow(FileDeletionFailureException::new);
    }

    @SneakyThrows
    @Override
    public String getBase64EncodedFile(String fileName) {
        File file = new File(fileFolderPath + fileName);

        FileInputStream fileInputStreamReader = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        fileInputStreamReader.read(bytes);

        return new String(Base64.encodeBase64(bytes), StandardCharsets.UTF_8);
    }

    private String generateFileName() {
        return UUID.randomUUID().toString();
    }

}
