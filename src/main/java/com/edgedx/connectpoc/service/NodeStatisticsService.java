package com.edgedx.connectpoc.service;

import java.io.File;
import java.io.IOException;

public interface NodeStatisticsService {

    void importNodeStatFile(File sourceFolder);

    void importTemperatureFile(File sourceFolder);

    void exportNodeStatToFile(File targetFolder) throws IOException;

}
