package com.edgedx.connectpoc.service;

import java.io.File;
import java.io.IOException;

public interface NodeService {

    void exportNodeConfigToFile(File targetFolder) throws IOException;

    void exportNodeDevicesToFile(File targetFolder) throws IOException;

    void updateProject();
}
