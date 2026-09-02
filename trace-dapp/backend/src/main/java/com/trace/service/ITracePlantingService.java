package com.trace.service;

import com.trace.model.Result;
import com.trace.model.bo.TracePlantingDTO;

public interface ITracePlantingService {
    Result<String> recordPlanting(TracePlantingDTO dto, String username);
}