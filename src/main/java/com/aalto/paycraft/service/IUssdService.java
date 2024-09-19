package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.UssdDTO;

public interface IUssdService {
    String ussdCallback(UssdDTO ussdDTO);
}