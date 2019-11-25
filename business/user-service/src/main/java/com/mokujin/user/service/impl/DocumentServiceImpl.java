package com.mokujin.user.service.impl;

import com.mokujin.user.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.io.DicomInputHandler;
import org.dcm4che3.io.DicomInputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final RestTemplate restTemplate;
    private final DicomInputHandler dicomInputHandler;

    @Override
    @SneakyThrows
    public void sendDicom(String publicKey, String privateKey, MultipartFile document, String patientNumber) {

        DicomInputStream dicomInputStream = new DicomInputStream(document.getInputStream());
        dicomInputStream.setDicomInputHandler(dicomInputHandler);
        dicomInputStream.readDataset(-1, -1);

    }
}
