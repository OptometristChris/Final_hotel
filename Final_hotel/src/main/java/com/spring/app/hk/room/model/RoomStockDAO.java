package com.spring.app.hk.room.model;

import java.time.LocalDate;

public interface RoomStockDAO {

    // 재고 잠금 (FOR UPDATE)
    int selectStockForUpdate(int room_type_id, LocalDate stay_date);

    // 재고 차감
    int decreaseStock(int room_type_id, LocalDate stay_date);

    // 날짜별 가격 조회 (총금액 계산용)
    int selectPrice(int room_type_id, LocalDate stay_date);
}