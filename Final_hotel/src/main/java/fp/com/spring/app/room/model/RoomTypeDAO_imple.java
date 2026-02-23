package fp.com.spring.app.room.model;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import fp.com.spring.app.room.domain.RoomTypeDTO;

@Repository
public class RoomTypeDAO_imple implements RoomTypeDAO {

    @Autowired
    private SqlSessionTemplate sqlsession;

    // 객실 목록 페이지 (최초 진입)
    @Override
    public List<RoomTypeDTO> selectRoomTypeList() {
        return sqlsession.selectList("room.selectRoomTypeList");
    }

    // 객실 필터 조회
    @Override
    public List<RoomTypeDTO> selectRoomTypeByFilter(Map<String, String> paraMap) {
        return sqlsession.selectList("room.selectRoomTypeByFilter", paraMap);
    }

    // 객실 상세 조회
    @Override
    public RoomTypeDTO selectRoomDetail(Long roomId) {
        return sqlsession.selectOne("room.selectRoomDetail", roomId);
    }

}
