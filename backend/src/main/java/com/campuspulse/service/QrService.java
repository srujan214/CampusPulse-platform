package com.campuspulse.service;

import com.campuspulse.dto.QrLocationDto;
import com.campuspulse.entity.LocationItem;
import com.campuspulse.qr.QrCodeGenerator;
import com.campuspulse.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class QrService {

    private final QrCodeGenerator qrCodeGenerator;
    private final LocationRepository locationRepository;

    @Value("${campuspulse.app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    public QrService(QrCodeGenerator qrCodeGenerator, LocationRepository locationRepository) {
        this.qrCodeGenerator = qrCodeGenerator;
        this.locationRepository = locationRepository;
    }

    public String buildTargetUrl(String locationName) {
        String base = appBaseUrl.replaceAll("/+$", "");
        return base + "/index.html?location=" + URLEncoder.encode(locationName, StandardCharsets.UTF_8) + "#report";
    }

    public byte[] generateLocationQrImage(String locationName, int size) throws Exception {
        String targetUrl = buildTargetUrl(locationName);
        return qrCodeGenerator.generateQrCodeImage(targetUrl, size, size);
    }

    public QrLocationDto getLocationQrDetails(String locationName) throws Exception {
        LocationItem item = locationRepository.findByNameIgnoreCase(locationName)
                .orElse(new LocationItem(locationName, "Campus Facility", "Ground Floor", "Campus issue reporting spot"));

        String targetUrl = buildTargetUrl(item.getName());
        String qrBase64 = qrCodeGenerator.generateQrCodeBase64(targetUrl, 260, 260);

        return new QrLocationDto(
                item.getName(),
                item.getBuilding() != null ? item.getBuilding() : "Main Campus",
                item.getFloor() != null ? item.getFloor() : "Level 1",
                targetUrl,
                qrBase64
        );
    }

    public List<QrLocationDto> getAllLocationQrs() {
        List<LocationItem> locations = locationRepository.findAll();
        List<QrLocationDto> result = new ArrayList<>();

        for (LocationItem loc : locations) {
            try {
                String targetUrl = buildTargetUrl(loc.getName());
                String qrBase64 = qrCodeGenerator.generateQrCodeBase64(targetUrl, 260, 260);
                result.add(new QrLocationDto(loc.getName(), loc.getBuilding(), loc.getFloor(), targetUrl, qrBase64));
            } catch (Exception e) {
                // Keep resilient
            }
        }
        return result;
    }
}
