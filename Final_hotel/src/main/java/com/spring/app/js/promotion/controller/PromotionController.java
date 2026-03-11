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
        mav.setViewName("js/promotion/write"); 
        return mav;
    }

    /**
     * [관리자] 프로모션 등록 처리
     */
    @PostMapping("/writeEnd")
    public ModelAndView promotionWriteEnd(ModelAndView mav, 
                                         HttpServletRequest request,
                                         @RequestParam(value="price", defaultValue="0") String price,
                                         @RequestParam(value="discount_rate", defaultValue="0") String discountRate,
                                         @RequestParam(value="discount_amount", defaultValue="0") String discountAmount,
                                         @RequestParam("attach") MultipartFile attach) {
        
        Map<String, String> paraMap = new HashMap<>();
        
        // 1. 일반 파라미터 수집 (request.getParameter 대신 안전하게 수집)
        paraMap.put("fk_hotel_id", request.getParameter("fk_hotel_id"));
        paraMap.put("title", request.getParameter("title"));
        paraMap.put("start_date", request.getParameter("start_date"));
        paraMap.put("end_date", request.getParameter("end_date"));
        paraMap.put("subtitle", request.getParameter("subtitle"));
        paraMap.put("benefits", request.getParameter("benefits"));
        paraMap.put("sort_order", request.getParameter("sort_order"));
        paraMap.put("is_active", request.getParameter("is_active"));
        paraMap.put("banner_type", request.getParameter("banner_type"));

        // 2. 숫자형 데이터 정제 (콤마 제거 및 Null 방지)
        // 자바스크립트에서 이미 제거했지만, 서버에서 한 번 더 처리하여 안전하게 만듭니다.
        paraMap.put("price", price.replaceAll("[^0-9]", "")); 
        paraMap.put("discount_rate", discountRate.isEmpty() ? "0" : discountRate);
        paraMap.put("discount_amount", discountAmount.replaceAll("[^0-9]", ""));

        // 3. 파일 업로드 처리 (이전과 동일)
        if (attach != null && !attach.isEmpty()) {
            String originalFilename = attach.getOriginalFilename();
            paraMap.put("image_url", originalFilename); 

            String projectPath = System.getProperty("user.dir");
            String deployPath = projectPath + File.separator + "file_images" + File.separator + "js";
            String staticPath = projectPath + File.separator + "src" + File.separator + "main" + 
                                File.separator + "resources" + File.separator + "static" + 
                                File.separator + "images" + File.separator + "js";

            try {
                byte[] fileData = attach.getBytes();
                File deployDir = new File(deployPath);
                if(!deployDir.exists()) deployDir.mkdirs();
                FileCopyUtils.copy(fileData, new File(deployPath, originalFilename));

                File staticDir = new File(staticPath);
                if(!staticDir.exists()) staticDir.mkdirs();
                FileCopyUtils.copy(fileData, new File(staticPath, originalFilename));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            paraMap.put("image_url", ""); 
        }

        // 4. DB Insert 및 응답 처리
        int n = promotionService.insertPromotion(paraMap);
        
        if(n == 1) {
            mav.addObject("message", "프로모션이 등록되었습니다.");
            mav.addObject("loc", request.getContextPath() + "/promotion/list?hotelId=" + paraMap.get("fk_hotel_id"));
        } else {
            mav.addObject("message", "등록 실패!");
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