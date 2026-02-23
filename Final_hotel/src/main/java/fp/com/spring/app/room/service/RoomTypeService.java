package fp.com.spring.app.room.service;

import java.util.List;
import java.util.Map;

import fp.com.spring.app.room.domain.RoomTypeDTO;

public interface RoomTypeService {

	// 객실 목록 페이지 (최초 진입) 조회
    List<RoomTypeDTO> getRoomList();

    // 객실 필터 조회 (AJAX 필터용 JSON 반환)
    List<RoomTypeDTO> getRoomListByFilter(Map<String,String> paraMap);

    // 객실 상세 페이지 조회
    RoomTypeDTO getRoomDetail(Long roomId);
}