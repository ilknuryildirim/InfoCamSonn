package com.infocam.lib.service;

import com.infocam.lib.marker.InitialMarkerData;
/**
 * Android Interface Definition Language for contact between services in different threads,
 * In this case: The IDataHandlerService connects the infocam core with the datahandlers of the plugins.
 */
interface IDataHandlerService {
    /** Request the process ID of this service. */
    int getPid();
    
 	String build();
 	
 	String getPluginName();
 	
 	String[] getUrlMatch(String processorName);
 	
 	String[] getDataMatch(String processorName);
 	
 	List<InitialMarkerData> load(String processorName, String rawData, int taskId, int colour);
}