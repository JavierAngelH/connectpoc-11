package com.edgedx.connectpoc.service;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.IOException;

public interface MgitService {

    void importMgitFiles(File sourceFolder);

    void exportMgitDataToFile(File targetFolder) throws IOException;

    void pushMgitFilesToServer(File sourceFolder) throws JSchException, SftpException;
}
