package com.spring.app.js.index.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String redirectToIndex() {
        // 사용자가 '/'로 들어오면 '/index' 경로로 리다이렉트 시킵니다.
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String indexPage(Model model) {
        // 여기서 각 브랜드(시엘 또는 르시엘)에 맞는 메인 배너와 데이터를 불러옵니다.
        // brand_id에 따라 배너, 공지사항 등을 필터링하여 모델에 담습니다.
    	return "js/index/index";
    }
}
