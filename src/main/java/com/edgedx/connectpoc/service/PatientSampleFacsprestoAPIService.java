/** 
 * CD4PrestoAPIService.java Created: Mar 22, 2018 JavierAngelH
 */

package com.edgedx.connectpoc.service;

/**
 * CD4PrestoAPIService - 
 *
 */
public interface PatientSampleFacsprestoAPIService {
    
    void postIndividualTests();
    
    void postMonthlySummaryByDevice();
    
    String postMonthSummaryByDevice(int month, int year);

}
