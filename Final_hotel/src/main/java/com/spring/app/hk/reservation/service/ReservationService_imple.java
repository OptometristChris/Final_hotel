package com.spring.app.hk.reservation.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.app.hk.reservation.domain.ReservationDTO;
import com.spring.app.hk.reservation.model.ReservationDAO;
import com.spring.app.hk.room.model.RoomStockDAO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService_imple implements ReservationService {

    private final ReservationDAO reservationDAO;
    private final RoomStockDAO roomStockDAO;

    @Override
    @Transactional
    public int createReservation(ReservationDTO dto) {

        LocalDate start = dto.getCheckin_date();
        LocalDate end = dto.getCheckout_date();

        int total_price = 0;

        for(LocalDate date = start;
            date.isBefore(end);
            date = date.plusDays(1)) {

            // 재고 잠금
            int stock = roomStockDAO.selectStockForUpdate(
                    dto.getRoom_type_id(),
                    date
            );

            if(stock <= 0) {
                throw new RuntimeException("재고 부족");
            }

            // 재고 차감
            roomStockDAO.decreaseStock(
                    dto.getRoom_type_id(),
                    date
            );

            total_price += roomStockDAO.selectPrice(
                    dto.getRoom_type_id(),
                    date
            );
        }

        dto.setTotal_price(total_price);
        dto.setReservation_status("CONFIRMED");

        return reservationDAO.insertReservation(dto);
    }
}