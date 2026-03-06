package com.spring.app.jh.ops.user.model;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.spring.app.jh.ops.user.domain.ReservationForShuttleDTO;
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

    ShuttleLegDTO selectLegByBookingAndType(@Param("bookingId") long bookingId,
                                           @Param("legType") String legType);

    int insertLeg(@Param("bookingId") long bookingId,
                  @Param("timetableId") long timetableId,
                  @Param("legType") String legType,
                  @Param("rideDate") Date rideDate,
                  @Param("placeCode") String placeCode,
                  @Param("departTime") String departTime,
                  @Param("ticketQty") int ticketQty);

    int cancelLeg(@Param("legId") long legId);
}