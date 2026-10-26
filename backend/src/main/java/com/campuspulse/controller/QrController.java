package com.campuspulse.controller;

import com.campuspulse.dto.QrLocationDto;
import com.campuspulse.service.QrService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qr")
public class QrController {

    private final QrService qrService;

    public QrController(QrService qrService) {
        this.qrService = qrService;
    }

    @GetMapping(value = "/image/{locationName}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrImage(
            @PathVariable("locationName") String locationName,
            @RequestParam(value = "size", defaultValue = "300") int size) {
        try {
            byte[] imageBytes = qrService.generateLocationQrImage(locationName, size);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("inline", "qr-" + locationName + ".png");
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/details/{locationName}")
    public ResponseEntity<QrLocationDto> getQrDetails(@PathVariable("locationName") String locationName) {
        try {
            QrLocationDto dto = qrService.getLocationQrDetails(locationName);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<QrLocationDto>> getAllLocationQrs() {
        List<QrLocationDto> list = qrService.getAllLocationQrs();
        return ResponseEntity.ok(list);
    }
}
