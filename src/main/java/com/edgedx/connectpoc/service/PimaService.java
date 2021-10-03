package com.edgedx.connectpoc.service;

import java.io.File;
import java.io.IOException;

public interface PimaService {

    void importPimaFile(File sourcePath);

    void exportPimaToFile(File targetFolder) throws IOException;


}
