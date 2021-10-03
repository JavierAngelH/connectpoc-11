package com.edgedx.connectpoc.service;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.IOException;

public interface TriageService {

    void importTriageFile(File sourceFolder);

    void exportTriageDataToFile(File targetFolder) throws IOException;

    void pushTriageFileToServer(File sourceFolder) throws JSchException, SftpException;
}
