package com.edgedx.connectpoc.service;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.IOException;

public interface AbbottM2000Service {

    void importAbbottFiles(File sourceFolder);

    void pushAbbottFileToServer(File file) throws JSchException, SftpException;

    void exportAbbottDataToFile(File targetFolder) throws IOException;

}
