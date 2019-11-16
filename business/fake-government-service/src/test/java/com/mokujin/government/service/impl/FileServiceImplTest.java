package com.mokujin.government.service.impl;

import com.mokujin.government.model.exception.extention.FileDeletionFailureException;
import com.mokujin.government.model.exception.extention.FileUploadFailureException;
import com.mokujin.government.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceImplTest {

    private final FileService fileService = new FileServiceImpl();

    private final String RESOURCES_PATH = "src/test/resources/";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileService, "fileFolderPath", RESOURCES_PATH);
    }

    @Test
    void saveFile_fileIsOk_fileNameIsReturned() {
        String fileName = fileService.saveFile(new MockMultipartFile("successful_save", new byte[]{}));
        assertNotNull(fileName);

        File file = new File(RESOURCES_PATH + fileName);
        assertTrue(file.exists());
        assertTrue(file.delete());
    }

    @Test
    void saveFile_fileIsNull_exceptionIsThrown() {
        assertThrows(FileUploadFailureException.class, () -> fileService.saveFile(null));
    }

    @Test
    void deleteFile_fileIsOk_fileNameIsReturned() {
        String fileName = fileService.saveFile(new MockMultipartFile("successful_delete", new byte[]{}));
        assertNotNull(fileName);

        fileService.deleteFile(fileName);
        File file = new File(RESOURCES_PATH + fileName);
        assertFalse(file.exists());
    }

    @Test
    void deleteFile_fileIsNotFound_exceptionIsThrown() {
        assertThrows(FileDeletionFailureException.class, () -> fileService.deleteFile("failed_delete"));
    }

    @Test
    void getBase64EncodedFile_fileNameIsOk_encodedFileIsReturned() {
        String base64EncodedFile = fileService.getBase64EncodedFile("test.png");
        assertNotNull(base64EncodedFile);
    }

    @Test
    void getBase64EncodedFile_fileIsNotFound_exceptionIsThrown() {
        assertThrows(FileNotFoundException.class, () -> fileService.getBase64EncodedFile("failed_get"));
    }

}