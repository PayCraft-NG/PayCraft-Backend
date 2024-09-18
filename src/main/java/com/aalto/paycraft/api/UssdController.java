package com.aalto.paycraft.api;

import com.aalto.paycraft.dto.UssdDTO;
import com.aalto.paycraft.service.IUssdService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UssdController {
    private final IUssdService iUssdService;

    @RequestMapping(value = "/ussd", method = {RequestMethod.POST, RequestMethod.GET})
    public String ussdCallback(
            @RequestParam(value = "sessionId", required = false) String sessionId,
            @RequestParam(value = "serviceCode", required = false) String serviceCode,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "text", required = false) String text) {
        return iUssdService.ussdCallback(new UssdDTO(sessionId, serviceCode, phoneNumber, text));
    }
}