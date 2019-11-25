package com.mokujin.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.*;
import org.dcm4che3.io.DicomInputHandler;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.TagUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DicomInputHandlerImpl implements DicomInputHandler {

    private static final int DEFAULT_WIDTH = 78;
    private int width = DEFAULT_WIDTH;

    @Override
    public void readValue(DicomInputStream dicomInputStream, Attributes attributes) throws IOException {
        StringBuilder line = new StringBuilder(width + 30);
        this.appendPrefix(dicomInputStream, line);
        this.appendHeader(dicomInputStream, line);
        VR vr = dicomInputStream.vr();
        int vallen = dicomInputStream.length();
        boolean undeflen = vallen == -1;
        int tag = dicomInputStream.tag();
        String privateCreator = attributes.getPrivateCreator(tag);
        if (vr == VR.SQ || undeflen) {
            this.appendKeyword(dicomInputStream, privateCreator, line);
            System.out.println(line);
            dicomInputStream.readValue(dicomInputStream, attributes);
            if (undeflen) {
                line.setLength(0);
                this.appendPrefix(dicomInputStream, line);
                this.appendHeader(dicomInputStream, line);
                this.appendKeyword(dicomInputStream, privateCreator, line);
                System.out.println(line);
            }
            return;
        }
        byte[] b = dicomInputStream.readValue();
        line.append(" [");
        if (vr.prompt(b, dicomInputStream.bigEndian(),
                attributes.getSpecificCharacterSet(),
                width - line.length() - 1, line)) {
            line.append(']');
            appendKeyword(dicomInputStream, privateCreator, line);
        }
        System.out.println(line);
        if (tag == Tag.FileMetaInformationGroupLength)
            dicomInputStream.setFileMetaInformationGroupLength(b);
        else if (tag == Tag.TransferSyntaxUID
                || tag == Tag.SpecificCharacterSet
                || TagUtils.isPrivateCreator(tag))
            attributes.setBytes(tag, vr, b);
    }



    @Override
    public void readValue(DicomInputStream dicomInputStream, Sequence sequence) throws IOException {
        String privateCreator = sequence.getParent().getPrivateCreator(dicomInputStream.tag());
        StringBuilder line = new StringBuilder(width);
        this.appendPrefix(dicomInputStream, line);
        this.appendHeader(dicomInputStream, line);
        this.appendKeyword(dicomInputStream, privateCreator, line);
        this.appendNumber(sequence.size() + 1, line);
        System.out.println(line);
        boolean undeflen = dicomInputStream.length() == -1;
        dicomInputStream.readValue(dicomInputStream, sequence);
        if (undeflen) {
            line.setLength(0);
            appendPrefix(dicomInputStream, line);
            appendHeader(dicomInputStream, line);
            appendKeyword(dicomInputStream, privateCreator, line);
            System.out.println(line);
        }
    }

    @Override
    public void readValue(DicomInputStream dicomInputStream, Fragments fragments) throws IOException {
        StringBuilder line = new StringBuilder(width + 20);
        this.appendPrefix(dicomInputStream, line);
        this.appendHeader(dicomInputStream, line);
        this.appendFragment(line, dicomInputStream, fragments.vr());
        System.out.println(line);
    }

    @Override
    public void startDataset(DicomInputStream dicomInputStream) throws IOException {
        this.promptPreamble(dicomInputStream.getPreamble());
    }

    @Override
    public void endDataset(DicomInputStream dicomInputStream) throws IOException {
    }

    private void appendPrefix(DicomInputStream dicomInputStream, StringBuilder line) {
        line.append(dicomInputStream.getTagPosition()).append(": ");
        int level = dicomInputStream.level();
        while (level-- > 0)
            line.append('>');
    }

    private void appendHeader(DicomInputStream dicomInputStream, StringBuilder line) {
        line.append(TagUtils.toString(dicomInputStream.tag())).append(' ');
        VR vr = dicomInputStream.vr();
        if (vr != null)
            line.append(vr).append(' ');
        line.append('#').append(dicomInputStream.length());
    }

    private void appendKeyword(DicomInputStream dis, String privateCreator, StringBuilder line) {
        if (line.length() < width) {
            line.append(" ");
            line.append(ElementDictionary.keywordOf(dis.tag(), privateCreator));
            if (line.length() > width)
                line.setLength(width);
        }
    }

    private void promptPreamble(byte[] preamble) {
        if (preamble == null)
            return;

        StringBuilder line = new StringBuilder(width);
        line.append("0: [");
        if (VR.OB.prompt(preamble, false, null, width - 5, line))
            line.append(']');
        System.out.println(line);
    }

    private void appendNumber(int number, StringBuilder line) {
        if (line.length() < width) {
            line.append(" #");
            line.append(number);
            if (line.length() > width)
                line.setLength(width);
        }
    }

    private void appendFragment(StringBuilder line, DicomInputStream dis,
                                VR vr) throws IOException {
        byte[] b = dis.readValue();
        line.append(" [");
        if (vr.prompt(b, dis.bigEndian(), null,
                width - line.length() - 1, line)) {
            line.append(']');
            appendKeyword(dis, null, line);
        }
    }
}
