package com.spring.app.jh.ops.user.model;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.spring.app.jh.ops.user.domain.ReservationForShuttleDTO;
import com.spring.app.jh.ops.user.domain.ShuttleConfirmCardDTO;
import com.spring.app.jh.ops.user.domain.ShuttleLegDTO;
import com.spring.app.jh.ops.user.domain.ShuttleTimetableDTO;
import com.spring.app.jh.ops.user.domain.SlotStockDTO;

@Mapper
public interface ShuttleOpsDAO {

    ReservationForShuttleDTO selectReservationForShuttle(@Param("reservationId") long reservationId);

    List<ShuttleTimetableDTO> selectTimetableList(@Param("hotelId") int hotelId,
                                                  @Param("legType") String legType);

    ShuttleTimetableDTO selectTimetableOne(@Param("timetableId") long timetableId);

    SlotStockDTO selectSlotForDisplay(@Param("timetableId") long timetableId,
                                      @Param("rideDate") Date rideDate);

    SlotStockDTO selectSlotForUpdate(@Param("timetableId") long timetableId,
                                     @Param("rideDate") Date rideDate);

    int updateBookedQtyPlus(@Param("timetableId") long timetableId,
                            @Param("rideDate") Date rideDate,
                            @Param("qty") int qty);

    int updateBookedQtyMinus(@Param("timetableId") long timetableId,
                             @Param("rideDate") Date rideDate,
                             @Param("qty") int qty);

    Long selectBookingIdByReservation(@Param("reservationId") long reservationId);

    int insertBookingHeader(@Param("reservationId") long reservationId,
                            @Param("hotelId") int hotelId,
                            @Param("memberNo") int memberNo,
                            @Param("rideDate") Date rideDate);

    int updateHeaderCanceled(@Param("bookingId") long bookingId);

   
    int insertLeg(@Param("bookingId") long bookingId,
                  @Param("timetableId") long timetableId,
                  @Param("legType") String legType,
                  @Param("rideDate") Date rideDate,
                  @Param("placeCode") String placeCode,
                  @Param("departTime") String departTime,
                  @Param("ticketQty") int ticketQty);

    
    // bookingId + timetableId (+ rideDate) 로 레그 1건 찾기
    ShuttleLegDTO selectLegByBookingAndTimetable(@Param("bookingId") long bookingId,
                                                 @Param("timetableId") long timetableId,
                                                 @Param("rideDate") Date rideDate);

    // bookingId + legType 으로 BOOKED 레그 목록 (화면/계산용)
    List<ShuttleLegDTO> selectBookedLegListByBookingAndType(@Param("bookingId") long bookingId,
                                                            @Param("legType") String legType);

    // 레그 수량/상태 BOOKED로 업데이트(부활 포함)
    int updateLegQtyBooked(@Param("legId") long legId,
                           @Param("ticketQty") int ticketQty);

    // 레그 취소
    int cancelLeg(@Param("legId") long legId);
    
    int updateHeaderBooked(long bookingId);
    
    List<ShuttleConfirmCardDTO> selectActiveShuttleBookings(@Param("memberNo") int memberNo);

    List<ShuttleLegDTO> selectBookedLegsByBooking(long bookingId);

    ReservationForShuttleDTO selectReservationForCard(long reservationId);

    String selectMemberName(@Param("memberNo") int memberNo);
    
    Long selectBookingIdByReservationNullable(long reservationId);
}