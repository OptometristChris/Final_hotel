package com.spring.app.js.promotion.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils; // 파일 복사용
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.spring.app.js.promotion.domain.PromotionDTO;
import com.spring.app.js.promotion.service.PromotionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/promotion")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    // 프로모션 목록 페이지
    @GetMapping("/list")
    public String list(@RequestParam(name="hotelId", defaultValue="1") int hotelId, Model model) {
        List<PromotionDTO> list = promotionService.getPromotionList(hotelId);
        model.addAttribute("promoList", list);
        model.addAttribute("hotelId", hotelId);
        model.addAttribute("hotelName", (hotelId == 1) ? "호텔 시엘 (SEOUL)" : "르 시엘 (BUSAN)");
        
        return "js/promotion/list"; 
    }
    
    // 프로모션 상세 페이지
    @GetMapping("/detail/{id}")
    public String promotionDetail(@PathVariable("id") int id, Model model) {
        PromotionDTO promotion = promotionService.getPromotionDetail(id);
        
        if (promotion != null) {
            double discountMultiplier = (100.0 - promotion.getDiscount_rate()) / 100.0;
            int finalPrice = (int) Math.round(promotion.getPrice() * discountMultiplier);
            model.addAttribute("promo", promotion);
            model.addAttribute("finalPrice", finalPrice);
        }
        
        return "js/promotion/detail"; 
    }
    
    /**
     * [관리자] 프로모션 등록 페이지 이동
     */
    @GetMapping("/write")
    public ModelAndView promotionWrite(@RequestParam("hotelId") String hotelId, ModelAndView mav) {
        mav.addObject("hotelId", hotelId);
        // HTML 파일명이 promotionWrite.html인 경우 아래와 같이 설정
        mav.setViewName("js/promotion/write"); 
        return mav;
    }

    /**
     * [관리자] 프로모션 등록 처리 (DB 저장 + 배포 폴더 + static 폴더 동시 저장)
     */
    @PostMapping("/writeEnd")
    public ModelAndView promotionWriteEnd(ModelAndView mav, 
                                         HttpServletRequest request,
                                         HttpSession session,
                                         @RequestParam("attach") MultipartFile attach) {
        
        // 1. 모든 파라미터 수집 (빠진 항목들 추가)
        String hotelId = request.getParameter("fk_hotel_id");
        String title = request.getParameter("title");
        String price = request.getParameter("price");             // [추가]
        String discountRate = request.getParameter("discount_rate");
        String discountAmount = request.getParameter("discount_amount"); // [추가]
        String startDate = request.getParameter("start_date");
        String endDate = request.getParameter("end_date");
        String subtitle = request.getParameter("subtitle");       // [추가]
        String benefits = request.getParameter("benefits");       // [추가]
        String sortOrder = request.getParameter("sort_order");     // [추가]
        String isActive = request.getParameter("is_active");       // [추가]
        String bannerType = request.getParameter("banner_type");   // [추가]

        Map<String, String> paraMap = new HashMap<>();
        paraMap.put("fk_hotel_id", hotelId);
        paraMap.put("title", title);
        paraMap.put("price", (price == null || "".equals(price)) ? "0" : price); // null 방지
        paraMap.put("discount_rate", (discountRate == null || "".equals(discountRate)) ? "0" : discountRate);
        paraMap.put("discount_amount", (discountAmount == null || "".equals(discountAmount)) ? "0" : discountAmount);
        paraMap.put("start_date", startDate);
        paraMap.put("end_date", endDate);
        paraMap.put("subtitle", subtitle);
        paraMap.put("benefits", benefits);
        paraMap.put("sort_order", (sortOrder == null) ? "1" : sortOrder);
        paraMap.put("is_active", isActive);
        paraMap.put("banner_type", bannerType);

     // 2. 파일 업로드 처리
        if (attach != null && !attach.isEmpty()) {
            String originalFilename = attach.getOriginalFilename();
            
            // 파일 이름을 먼저 Map에 넣어줍니다. (이게 빠져서 DB 에러가 난 것입니다!)
            paraMap.put("image_url", originalFilename); 

            String root = session.getServletContext().getRealPath("/");
            String deployPath = root + "file_images" + File.separator + "js";
            
            String projectPath = System.getProperty("user.dir");
            String staticPath = projectPath + File.separator + "src" + File.separator + "main" + 
                                File.separator + "resources" + File.separator + "static" + 
                                File.separator + "images" + File.separator + "js";

            try {
                byte[] fileData = attach.getBytes();

                // 1. 배포 경로 처리
                File deployDir = new File(deployPath);
                if(!deployDir.exists()) deployDir.mkdirs();
                
                File deployFile = new File(deployPath, originalFilename);
                FileCopyUtils.copy(fileData, deployFile);

                // 2. static 경로 처리
                File staticDir = new File(staticPath);
                if(!staticDir.exists()) staticDir.mkdirs();
                
                File staticFile = new File(staticPath, originalFilename);
                FileCopyUtils.copy(fileData, staticFile);

                System.out.println("✅ 파일 저장 및 이름 Map 추가 완료: " + originalFilename);

            } catch (IOException e) {
                System.err.println("❌ 파일 저장 중 오류 발생!");
                e.printStackTrace();
            }
        } else {
            // 파일을 업로드하지 않았을 경우 기본값이나 빈 문자열 처리
            paraMap.put("image_url", ""); 
        }

        // 3. DB Insert
        int n = promotionService.insertPromotion(paraMap);

        if(n == 1) {
            mav.addObject("message", "프로모션이 성공적으로 등록되었습니다.");
            mav.addObject("loc", request.getContextPath() + "/promotion/list?hotelId=" + hotelId);
        } else {
            mav.addObject("message", "DB 등록에 실패했습니다.");
            mav.addObject("loc", "javascript:history.back()");
        }

        mav.setViewName("msg"); 
        return mav;
    }
    
    // 프로모션 삭제
    @PostMapping("/delete")
    public ModelAndView promotionDelete(ModelAndView mav, 
                                         HttpServletRequest request,
                                         @RequestParam("promotion_id") int promotionId,
                                         @RequestParam("hotelId") String hotelId) {
        
        // 1. 서비스 호출 (실제 삭제 로직)
        int n = promotionService.deletePromotion(promotionId);
        
        if(n > 0) {
            mav.addObject("message", "프로모션이 삭제되었습니다.");
            mav.addObject("loc", request.getContextPath() + "/promotion/list?hotelId=" + hotelId);
        } else {
            mav.addObject("message", "삭제 처리에 실패했습니다.");
            mav.addObject("loc", "javascript:history.back()");
        }
        
        mav.setViewName("msg"); // 기존에 사용하던 메시지 출력용 jsp/html
        return mav;
    }
    
}