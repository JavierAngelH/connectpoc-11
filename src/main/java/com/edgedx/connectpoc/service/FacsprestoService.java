package com.edgedx.connectpoc.service;

import com.edgedx.connectpoc.utils.Constants;

import java.io.File;
import java.io.IOException;

public interface FacsprestoService {

    void importFacsprestoFiles(File sourceFolder);

    void exportBDPrestoFile(File targetFolder, Constants.PRESTO_CATEGORY category) throws IOException;
}
