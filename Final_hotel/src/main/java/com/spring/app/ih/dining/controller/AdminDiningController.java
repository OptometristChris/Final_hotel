package com.spring.app.ih.dining.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.app.ih.dining.model.DiningDTO;
import com.spring.app.ih.dining.model.DiningReservationDTO;
import com.spring.app.ih.dining.model.ShopReservationStatDTO;
import com.spring.app.ih.dining.service.DiningService;

@Controller
@RequestMapping("/admin/dining")
public class AdminDiningController {

    @Autowired
    private DiningService diningservice;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model, @RequestParam Map<String, Object> paraMap) {
   
        int currentPage = Integer.parseInt((String) paraMap.getOrDefault("page", "1"));
        int sizePerPage = 10; 
        
        int startRow = (currentPage - 1) * sizePerPage + 1;
        int endRow = currentPage * sizePerPage;
        
        paraMap.put("startRow", startRow);
        paraMap.put("endRow", endRow);

        Map<String, Object> counts = diningservice.getDashboardCounts();
        model.addAttribute("counts", counts);
        
        List<DiningReservationDTO> resList = diningservice.getAllReservationsAdmin(paraMap);
        model.addAttribute("resList", resList);
        
    	List<ShopReservationStatDTO> shopCounts = diningservice.getTodayShopStats();
        model.addAttribute("shopCounts", shopCounts);
     
        int totalCount = diningservice.getTotalReservationCount(paraMap); 
        int totalPage = (int) Math.ceil((double) totalCount / sizePerPage);
        
        int pageBarSize = 5;
        int startPage = ((currentPage - 1) / pageBarSize) * pageBarSize + 1;
        int endPage = Math.min(startPage + pageBarSize - 1, totalPage);

        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("paraMap", paraMap); 

        return "dining/admin/admin_dashboard"; 
    }

    // AJAX를 이용한 상태 변경 처리
    @PostMapping("/updateStatus")
    @ResponseBody
    public String updateStatus(@RequestParam("resId") Long resId, @RequestParam("status") String status) {
        String upperStatus = status.toUpperCase();
    	
    	int n = diningservice.updateReservationStatusAdmin(resId, upperStatus);
        return (n==1) ? "success" : "fail";
    }
    
    @PostMapping("/registerManual")
    @ResponseBody
    public String registerManual(DiningReservationDTO dto) {

        dto.setStatus("CONFIRMED"); 
        
        int n = diningservice.registerManual(dto);
        return (n == 1) ? "success" : "fail";
    }
    
    // 예약 상세 조회
    @GetMapping("/detail")
    @ResponseBody 
    public DiningReservationDTO getReservationDetail(@RequestParam("resId") Long resId) {
        
        DiningReservationDTO detail = diningservice.getReservationDetail(resId);
        System.out.println(">>> 가져온 데이터: " + detail);
        return detail; 
    }
    
    // 인원 합산
    @ResponseBody
    public int check_backup(@RequestParam Map<String, Object> paraMap) {
        try {
            int total = Integer.parseInt(String.valueOf(paraMap.get("adult_count"))) 
                      + Integer.parseInt(String.valueOf(paraMap.get("child_count")))
                      + Integer.parseInt(String.valueOf(paraMap.get("infant_count")));
            paraMap.put("totalGuests", total);
            return diningservice.checkAvailability(paraMap);
        } catch (Exception e) {
            return 0; 
        }
    }
    
    // 다이닝 예약 관리
    @GetMapping("/setting")
    public String settingPage(Model model) {

        List<Map<String, Object>> blockList = diningservice.getBlockList(); 
        List<Map<String, Object>> diningList = diningservice.getDiningList(); 
        
        model.addAttribute("blockList", blockList);
        model.addAttribute("diningList", diningList);
        
        return "dining/admin/control_setting";
    }
    
    // 차단 등록
    @PostMapping("/block/register")
    @ResponseBody
    public String registerBlock(@RequestParam Map<String, Object> paraMap) {
        try {
            
            System.out.println(">>> 등록 요청 데이터: " + paraMap);

            if(paraMap.get("fkDiningId") == null || paraMap.get("blockDate") == null || paraMap.get("blockTime") == null) {
                return "empty_data";
            }

            int result = diningservice.insertBlock(paraMap);

            if(result > 0) {
                return "success";
            } else {
                return "fail";
            }

        } catch (Exception e) {
            e.printStackTrace(); 
            return "error: " + e.getMessage();
        }
    }

    // 차단 해제
    @PostMapping("/block/delete")
    @ResponseBody
    public String deleteBlock(@RequestParam("blockId") Long blockId) {
    	diningservice.deleteBlock(blockId);
        return "success";
    }
    
    // 차단 시간대 불러오기
    @ResponseBody
    @GetMapping("/getUnavailableSlots")
    public List<String> getUnavailableSlots(@RequestParam Map<String, String> paraMap) {
        List<String> unavailableList = diningservice.getUnavailableTimeList(paraMap);
        return unavailableList;
    }
    
    @PostMapping("/updateMaxCapacity")
    @ResponseBody
    public String updateMaxCapacity(@RequestParam("diningId") String diningId, 
                                    @RequestParam("maxCapacity") int maxCapacity) {
        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("diningId", diningId);
        paraMap.put("maxCapacity", maxCapacity);
        
        int n = diningservice.updateMaxCapacity(paraMap);
        return (n == 1) ? "success" : "fail";
    }
    
    @PostMapping("/updateSlotCapacity")
    @ResponseBody
    public String updateSlotCapacity(@RequestParam("slotId") String slotId, 
                                     @RequestParam("maxSlotCapacity") int maxSlotCapacity) {
        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("slotId", slotId);
        paraMap.put("maxSlotCapacity", maxSlotCapacity);
        
        int n = diningservice.updateSlotCapacity(paraMap);
        return (n == 1) ? "success" : "fail";
    } 
    
    @GetMapping("/getConfig")
    @ResponseBody
    public List<ShopReservationStatDTO> getDiningConfig(@RequestParam("diningId") String diningId) {
        return diningservice.getDiningConfig(diningId); 
    }
    
    @GetMapping("/getTodayShopResList")
    @ResponseBody
    public List<Map<String, Object>> getTodayShopResList(@RequestParam("diningId") String diningId) {
        return diningservice.getTodayShopResList(diningId); 
    }
    
    @PostMapping("/check")
    @ResponseBody
    public int checkAvailability(@RequestParam Map<String, Object> params) {
        System.out.println(">>> [신규 검증] 파라미터: " + params);
        
        try {
            int availableSeat = diningservice.getAvailableSeatCount(params);
            
            int requestedPeople = Integer.parseInt(params.get("adult_count").toString())
                                + Integer.parseInt(params.get("child_count").toString())
                                + Integer.parseInt(params.get("infant_count").toString());

            System.out.println(">>> 잔여석: " + availableSeat + " / 요청인원: " + requestedPeople);

            return availableSeat; 
            
        } catch (Exception e) {
            e.printStackTrace();
            return -1; 
        }
    }
    
    @GetMapping("/list")
    public String adminDiningList(Model model) {
    	Map<String, Object> paraMap = new HashMap<>();
    	List<DiningDTO> diningList = diningservice.getAdminDiningList(paraMap);
        model.addAttribute("diningList", diningList);
        return "dining/admin/list"; 
    }
    
    @GetMapping("/editdetail")
    public String showEditForm(@RequestParam("dining_id") Long dining_id, Model model) {
        DiningDTO dining = diningservice.getDiningDetail(dining_id);
        if (dining == null) { return "redirect:/admin/dining/list";}
        model.addAttribute("dining", dining);
        return "dining/admin/dininginfoedit"; 
    }
    
    @PostMapping("/editdetail")
    public String updateDiningInfo(@ModelAttribute DiningDTO diningDTO, RedirectAttributes redirectAttributes) {
        int result = diningservice.updateDiningDetails(diningDTO);
        
        if(result > 0) {
            redirectAttributes.addFlashAttribute("msg", "매장 정보가 성공적으로 수정되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("msg", "수정에 실패했습니다. 다시 시도해주세요.");
        }
        
        return "redirect:/dining/detail/" + diningDTO.getDining_id();
    }
    
    

}