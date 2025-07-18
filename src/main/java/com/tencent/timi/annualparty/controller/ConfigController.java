package com.tencent.timi.annualparty.controller;

import com.tencent.timi.annualparty.model.config.ConfigKey;
import com.tencent.timi.annualparty.model.config.ConfigValue;
import com.tencent.timi.annualparty.model.config.GetConfigReq;
import com.tencent.timi.annualparty.model.config.GetConfigRes;
import com.tencent.timi.annualparty.rainbow.PropertyReader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author haoyangwei
 */
@RestController
@RequestMapping("/config")
public class ConfigController {

    // @CrossOrigin(origins = {"http://192.168.255.10:8080", "http://30.19.56.41:8080", "https://timish.woa.com/#/"}, allowCredentials = "true")
    @PostMapping("/kv")
    public ResponseEntity<GetConfigRes> getKeyValueConfig(@RequestBody GetConfigReq request) {
        if (request == null || request.getKeys().isEmpty()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        List<ConfigValue> values = new ArrayList<>(request.getKeys().size());
        for (ConfigKey key : request.getKeys()) {
            String value = PropertyReader.getValue(key.getTable(), key.getKey(), "");
            values.add(new ConfigValue(key.getTable(), key.getKey(), value));
        }
        GetConfigRes response = new GetConfigRes();
        response.setValues(values);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
