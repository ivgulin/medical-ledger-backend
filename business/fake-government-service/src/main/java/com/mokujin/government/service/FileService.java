package com.mokujin.government.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String saveFile(MultipartFile file);

    void  deleteFile(String path);

    String getBase64EncodedFile(String path);

}
