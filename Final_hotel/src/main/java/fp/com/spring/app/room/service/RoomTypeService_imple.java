package fp.com.spring.app.room.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import fp.com.spring.app.room.domain.RoomTypeDTO;
import fp.com.spring.app.room.model.RoomTypeDAO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomTypeService_imple implements RoomTypeService {

    private final RoomTypeDAO roomdao;

    // 객실 목록 페이지 (최초 진입) 조회
    @Override
    public List<RoomTypeDTO> getRoomList() {
        return roomdao.selectRoomTypeList();
    }

    // 객실 필터 조회 (AJAX 필터용 JSON 반환)
    @Override
    public List<RoomTypeDTO> getRoomListByFilter(Map<String,String> paraMap) {
        return roomdao.selectRoomTypeByFilter(paraMap);
    }

    // 객실 상세 페이지 조회
    @Override
    public RoomTypeDTO getRoomDetail(Long roomId) {
        return roomdao.selectRoomDetail(roomId);
    }
}