package com.edgedx.connectpoc.service;

import com.jcraft.jsch.JSchException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface GenexpertService {

    void decryptGeneXpertFiles(File sourceFolder) throws IOException;

    void exportGeneXpertDataToFile(File targetFolder) throws IOException;

    void pushGeneXpertFileToServer(File sourceFolder) throws JSchException;

}
