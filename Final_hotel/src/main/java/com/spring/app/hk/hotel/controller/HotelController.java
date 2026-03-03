package com.spring.app.hk.hotel.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.spring.app.common.FileManager;
import com.spring.app.hk.hotel.service.HotelService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/hotel/")
public class HotelController {

    private final HotelService hotelService;
    private final FileManager fileManager;

    @Value("${file.images-dir}")
    private String imagesDir;

    // 등록 페이지 이동
	/* @PreAuthorize("hasRole('ROLE_HQ')") */
    @GetMapping("register")
    public String registerPage() {
        return "hk/admin/hotel/register";
    }

    // 호텔 등록
	/* @PreAuthorize("hasRole('ROLE_HQ')") */
    @PostMapping("register")
    @ResponseBody
    public Map<String, Object> register(
            @RequestParam Map<String, String> map,
            @RequestParam("mainImage") MultipartFile mainImage
          /*  Authentication authentication*/
    ) {

        Map<String, String> paraMap = new HashMap<>(map);

        try {

            // 로그인한 관리자 ID 저장
			/* paraMap.put("created_by", authentication.getName()); */
        	paraMap.put("created_by", "DEV_TEST");

            // 대표 이미지 업로드
            if(!mainImage.isEmpty()) {

                String savedName =
                        fileManager.doFileUpload(
                                mainImage.getBytes(),
                                mainImage.getOriginalFilename(),
                                imagesDir);

                paraMap.put("main_image", savedName);
            }

            // 서비스 호출 (Reservation 구조 동일)
            hotelService.saveHotel(paraMap);

        } catch(Exception e) {
            e.printStackTrace();
            return Map.of("result", 0);
        }

        return Map.of("result", 1);
    }
}