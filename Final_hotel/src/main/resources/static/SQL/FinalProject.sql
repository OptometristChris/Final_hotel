-- final

-- //////////////////////////////////////////////////////// 회원 관련 ////////////////////////////////////////////////////////
/* 1) 호텔(지점) 마스터 */
CREATE TABLE tbl_hotel (
  hotel_id   NUMBER PRIMARY KEY,
  hotel_name VARCHAR2(50) NOT NULL
);

INSERT INTO tbl_hotel VALUES (1, '호텔 시엘');
INSERT INTO tbl_hotel VALUES (2, '르시엘');


/* 2) 회원 등급 마스터 + 정책 */
CREATE TABLE tbl_member_grade (
  grade_code   VARCHAR2(20) PRIMARY KEY,
  grade_name   VARCHAR2(20) NOT NULL,
  sort_order   NUMBER NOT NULL
);

CREATE TABLE tbl_member_grade_policy (
  grade_code                  VARCHAR2(20) PRIMARY KEY,
  annual_stay_nights_min      NUMBER NULL,
  valid_points_min            NUMBER NULL,
  room_point_rate_pct         NUMBER(5,2) NOT NULL,
  rooftop_lounge_pool_free_yn CHAR(1) DEFAULT 'N' NOT NULL,
  breakfast_voucher_per_night NUMBER DEFAULT 0 NOT NULL,

  CONSTRAINT FK_grade_policy_grade
    FOREIGN KEY (grade_code) REFERENCES tbl_member_grade(grade_code),

  CONSTRAINT CK_grade_policy_nonneg CHECK (
    (annual_stay_nights_min IS NULL OR annual_stay_nights_min >= 0)
    AND (valid_points_min IS NULL OR valid_points_min >= 0)
    AND room_point_rate_pct >= 0
    AND breakfast_voucher_per_night >= 0
  ),
  CONSTRAINT CK_grade_policy_yn CHECK (rooftop_lounge_pool_free_yn IN ('Y','N'))
);

/* 등급 데이터 */
INSERT INTO tbl_member_grade VALUES ('CLASSIC','클래식',1);
INSERT INTO tbl_member_grade VALUES ('SILVER','실버',2);
INSERT INTO tbl_member_grade VALUES ('GOLD','골드',3);
INSERT INTO tbl_member_grade VALUES ('PLATINUM','플레티넘',4);

INSERT INTO tbl_member_grade_policy
(grade_code, annual_stay_nights_min, valid_points_min, room_point_rate_pct, rooftop_lounge_pool_free_yn, breakfast_voucher_per_night)
VALUES ('CLASSIC', NULL, NULL, 3.00, 'N', 0);

INSERT INTO tbl_member_grade_policy
VALUES ('SILVER', 5, 1500, 5.00, 'N', 0);

INSERT INTO tbl_member_grade_policy
VALUES ('GOLD', 25, 20000, 7.00, 'Y', 0);

INSERT INTO tbl_member_grade_policy
VALUES ('PLATINUM', 50, 70000, 10.00, 'Y', 1);


/* 3) 회원 테이블(등급 FK 포함) */
CREATE TABLE tbl_member_security(
   memberid               VARCHAR2(50)   NOT NULL,
   passwd                 VARCHAR2(200)  NOT NULL,
   enabled                CHAR(1)        DEFAULT '1' NOT NULL,

   name                   NVARCHAR2(30)  NOT NULL,
   birthday               VARCHAR2(20)   NOT NULL,

   email                  VARCHAR2(200)  NOT NULL,
   mobile                 VARCHAR2(200),

   postcode               VARCHAR2(10),
   address                VARCHAR2(200),
   detail_address         VARCHAR2(200),
   extra_address          VARCHAR2(200),

   point                  NUMBER DEFAULT 0 NOT NULL,
   point_earned_total     NUMBER DEFAULT 0 NOT NULL,

   registerday            DATE DEFAULT SYSDATE,
   passwd_modify_date     DATE DEFAULT SYSDATE,
   last_login_date        DATE DEFAULT SYSDATE,

   grade_code             VARCHAR2(20),

   CONSTRAINT PK_tbl_member_security PRIMARY KEY(memberid),
   CONSTRAINT UQ_tbl_member_security_email UNIQUE(email),
   CONSTRAINT CK_tbl_member_security_enabled CHECK (enabled IN ('0','1')),
   CONSTRAINT CK_tbl_member_security_point_nonneg CHECK (point >= 0 AND point_earned_total >= 0),
   CONSTRAINT FK_member_security_grade
     FOREIGN KEY (grade_code) REFERENCES tbl_member_grade(grade_code)
);


/* 4) 관리자 테이블(호텔 FK 포함) */
CREATE TABLE tbl_admin_security(
   adminid               VARCHAR2(50)   NOT NULL,
   passwd                VARCHAR2(200)  NOT NULL,
   enabled               CHAR(1)        DEFAULT '1' NOT NULL,

   name                  NVARCHAR2(30)  NOT NULL,
   email                 VARCHAR2(200)  NOT NULL,
   mobile                VARCHAR2(200),

   admin_type            VARCHAR2(20)   NOT NULL,     -- HQ / BRANCH
   fk_hotel_id           NUMBER NULL,

   registerday           DATE DEFAULT SYSDATE,
   passwd_modify_date    DATE DEFAULT SYSDATE,
   last_login_date       DATE DEFAULT SYSDATE,

   CONSTRAINT PK_tbl_admin_security PRIMARY KEY(adminid),
   CONSTRAINT UQ_tbl_admin_security_email UNIQUE(email),
   CONSTRAINT CK_tbl_admin_security_enabled CHECK (enabled IN ('0','1')),
   CONSTRAINT CK_tbl_admin_security_type CHECK (admin_type IN ('HQ','BRANCH')),
   CONSTRAINT CK_tbl_admin_security_hotel_rule CHECK (
        (admin_type = 'HQ' AND fk_hotel_id IS NULL)
     OR (admin_type = 'BRANCH' AND fk_hotel_id IS NOT NULL)
   ),
   CONSTRAINT FK_tbl_admin_security_hotel
     FOREIGN KEY (fk_hotel_id) REFERENCES tbl_hotel(hotel_id)
);


/* 5) 회원 권한 + 시퀀스 */
CREATE TABLE tbl_member_authorities (
   num        NUMBER NOT NULL,
   memberid   VARCHAR2(50) NOT NULL,
   authority  VARCHAR2(50) NOT NULL,

   CONSTRAINT PK_tbl_member_authorities PRIMARY KEY(num),
   CONSTRAINT UQ_tbl_member_authorities UNIQUE(memberid, authority),
   CONSTRAINT FK_tbl_member_authorities_memberid
     FOREIGN KEY(memberid) REFERENCES tbl_member_security(memberid) ON DELETE CASCADE,
   CONSTRAINT CK_tbl_member_authorities_prefix
     CHECK (authority LIKE 'ROLE\_%' ESCAPE '\')
);

CREATE SEQUENCE seq_tbl_member_authorities
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


/* 6) 관리자 권한 + 시퀀스 */
CREATE TABLE tbl_admin_authorities (
   num        NUMBER NOT NULL,
   adminid    VARCHAR2(50) NOT NULL,
   authority  VARCHAR2(50) NOT NULL,

   CONSTRAINT PK_tbl_admin_authorities PRIMARY KEY(num),
   CONSTRAINT UQ_tbl_admin_authorities UNIQUE(adminid, authority),
   CONSTRAINT FK_tbl_admin_authorities_adminid
     FOREIGN KEY(adminid) REFERENCES tbl_admin_security(adminid) ON DELETE CASCADE,
   CONSTRAINT CK_tbl_admin_authorities_prefix
     CHECK (authority LIKE 'ROLE\_%' ESCAPE '\')
);

CREATE SEQUENCE seq_tbl_admin_authorities
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


/* 7) 로그인 히스토리(회원만) + 시퀀스 */
CREATE TABLE tbl_loginhistory
(
  historyno   NUMBER NOT NULL,
  memberid    VARCHAR2(50) NOT NULL,
  logindate   DATE DEFAULT SYSDATE NOT NULL,
  clientip    VARCHAR2(45) NOT NULL,

  CONSTRAINT PK_tbl_loginhistory PRIMARY KEY(historyno),
  CONSTRAINT FK_tbl_loginhistory_memberid
    FOREIGN KEY(memberid) REFERENCES tbl_member_security(memberid)
);

CREATE SEQUENCE seq_historyno
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;











-- //////////////////////////////////////////////////////// 셔틀 관련 ////////////////////////////////////////////////////////

/* =========================================================
   SHUTTLE DB (NO SEAT, TICKET-QUANTITY ONLY)
   - Oracle 12c+ (IDENTITY)
   - FK: tbl_hotel(hotel_id), tbl_member_security(memberid)
   - FK: tbl_reservation(reservation_id) (존재한다고 가정)
   ========================================================= */


/* =========================================================
   0) (선택) 기존 객체 Drop (필요시 주석 해제)
   =========================================================
DROP VIEW vw_my_shuttle_reservation_card;

DROP TABLE tbl_shuttle_slot_stock CASCADE CONSTRAINTS;
DROP TABLE tbl_shuttle_booking_leg CASCADE CONSTRAINTS;
DROP TABLE tbl_shuttle_booking CASCADE CONSTRAINTS;
DROP TABLE tbl_shuttle_timetable CASCADE CONSTRAINTS;
DROP TABLE tbl_shuttle_place CASCADE CONSTRAINTS;
*/


/* =========================================================
   1) 출발지(픽업장소) 마스터
   - place_code 로 표준화
   ========================================================= */
CREATE TABLE tbl_shuttle_place (
  place_code   VARCHAR2(30) PRIMARY KEY,     -- SEOUL_STATION / GIMPO / INCHEON ...
  place_name   NVARCHAR2(50) NOT NULL,       -- 서울역 / 김포공항 / 인천공항 ...
  active_yn    CHAR(1) DEFAULT 'Y' NOT NULL,

  CONSTRAINT CK_shuttle_place_active_yn
    CHECK (active_yn IN ('Y','N'))
);

INSERT INTO tbl_shuttle_place(place_code, place_name) VALUES ('SEOUL_STATION', N'서울역');
INSERT INTO tbl_shuttle_place(place_code, place_name) VALUES ('GIMPO',        N'김포공항');
INSERT INTO tbl_shuttle_place(place_code, place_name) VALUES ('INCHEON',      N'인천공항');
COMMIT;


/* =========================================================
   2) 셔틀 시간표(템플릿)
   - "호텔/방향/장소/출발시각" 단위로 정원(capacity)을 정의
   - 날짜는 없음(날짜별 재고는 slot_stock에서 관리)
   ========================================================= */
CREATE TABLE tbl_shuttle_timetable (
  timetable_id   NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,

  fk_hotel_id    NUMBER NOT NULL,           -- tbl_hotel.hotel_id
  leg_type       VARCHAR2(20) NOT NULL,     -- TO_HOTEL / FROM_HOTEL
  place_code     VARCHAR2(30) NOT NULL,     -- tbl_shuttle_place.place_code
  depart_time    VARCHAR2(5)  NOT NULL,     -- '09:00'
  capacity       NUMBER NOT NULL,           -- 해당 시간대 총 티켓 수
  active_yn      CHAR(1) DEFAULT 'Y' NOT NULL,

  created_at     TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

  CONSTRAINT FK_shuttle_timetable_hotel
    FOREIGN KEY (fk_hotel_id) REFERENCES tbl_hotel(hotel_id),

  CONSTRAINT FK_shuttle_timetable_place
    FOREIGN KEY (place_code) REFERENCES tbl_shuttle_place(place_code),

  CONSTRAINT CK_shuttle_timetable_leg_type
    CHECK (leg_type IN ('TO_HOTEL','FROM_HOTEL')),

  CONSTRAINT CK_shuttle_timetable_depart_time
    CHECK (REGEXP_LIKE(depart_time, '^[0-2][0-9]:[0-5][0-9]$')),

  CONSTRAINT CK_shuttle_timetable_capacity
    CHECK (capacity > 0),

  CONSTRAINT CK_shuttle_timetable_active_yn
    CHECK (active_yn IN ('Y','N'))
);

-- 같은 호텔/방향/장소/시간표가 중복 생성되지 않도록
CREATE UNIQUE INDEX UQ_shuttle_timetable_key
ON tbl_shuttle_timetable(fk_hotel_id, leg_type, place_code, depart_time);

-- 조회 성능용(선택)
CREATE INDEX IX_shuttle_timetable_hotel
ON tbl_shuttle_timetable(fk_hotel_id, leg_type);


/* =========================================================
   3) 날짜별 시간대 재고(정원/예약수) 테이블
   - 동시성/중복 예매 방지의 "DB 단" 핵심
   - UNIQUE(fk_timetable_id, ride_date)
   - booked_qty <= capacity 체크
   ========================================================= */
CREATE TABLE tbl_shuttle_slot_stock (
  stock_id        NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,

  fk_timetable_id NUMBER NOT NULL,     -- tbl_shuttle_timetable.timetable_id
  ride_date       DATE NOT NULL,       -- 운행일(기준일)

  capacity        NUMBER NOT NULL,     -- 생성 시점의 정원 스냅샷(감사/정책 변경 대비)
  booked_qty      NUMBER DEFAULT 0 NOT NULL,

  updated_at      TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

  CONSTRAINT FK_shuttle_stock_timetable
    FOREIGN KEY (fk_timetable_id) REFERENCES tbl_shuttle_timetable(timetable_id),

  CONSTRAINT CK_shuttle_stock_capacity
    CHECK (capacity > 0),

  CONSTRAINT CK_shuttle_stock_booked_qty
    CHECK (booked_qty >= 0 AND booked_qty <= capacity)
);

-- 날짜별/시간대별 재고는 1행만 존재
CREATE UNIQUE INDEX UQ_shuttle_stock_key
ON tbl_shuttle_slot_stock(fk_timetable_id, ride_date);

-- 조회 성능용(선택)
CREATE INDEX IX_shuttle_stock_date
ON tbl_shuttle_slot_stock(ride_date);


/* =========================================================
   4) 셔틀 예약 헤더
   - 숙박예약(fk_reservation_id)에 종속(1:0~1로 운영 가능)
   - 호텔 지점: fk_hotel_id 로 변경
   ========================================================= */
CREATE TABLE tbl_shuttle_booking (
  shuttle_booking_id  NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,

  fk_reservation_id   NUMBER NOT NULL,        -- tbl_reservation.reservation_id (추후 조정 가능)
  fk_hotel_id         NUMBER NOT NULL,        -- tbl_hotel.hotel_id
  fk_memberid         VARCHAR2(50) NOT NULL,  -- tbl_member_security.memberid

  ride_date           DATE NOT NULL,          -- 셔틀 기준일(정책에 따라 체크인/아웃 기준일 등)
  status              VARCHAR2(20) DEFAULT 'BOOKED' NOT NULL, -- BOOKED/CANCELED

  created_at          TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
  canceled_at         TIMESTAMP NULL,

  CONSTRAINT FK_shuttle_booking_member
    FOREIGN KEY (fk_memberid) REFERENCES tbl_member_security(memberid),

  CONSTRAINT FK_shuttle_booking_hotel
    FOREIGN KEY (fk_hotel_id) REFERENCES tbl_hotel(hotel_id),

  CONSTRAINT FK_shuttle_booking_reservation
    FOREIGN KEY (fk_reservation_id) REFERENCES tbl_reservation(reservation_id),

  CONSTRAINT CK_shuttle_booking_status
    CHECK (status IN ('BOOKED','CANCELED'))
);

-- 숙박예약 1건당 셔틀 예약 헤더 1건(중복 생성 방지)
CREATE UNIQUE INDEX UQ_shuttle_booking_reservation
ON tbl_shuttle_booking(fk_reservation_id);

CREATE INDEX IX_shuttle_booking_member_date
ON tbl_shuttle_booking(fk_memberid, ride_date);


/* =========================================================
   5) 셔틀 예약 상세(구간/편도)
   - 좌석번호 없음
   - timetable을 참조하여 "어느 시간대 티켓을 몇 장 샀는지"만 관리
   ========================================================= */
CREATE TABLE tbl_shuttle_booking_leg (
  shuttle_leg_id       NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,

  fk_shuttle_booking_id NUMBER NOT NULL,  -- 헤더 참조
  fk_timetable_id       NUMBER NOT NULL,  -- 시간표 참조(호텔/방향/장소/시간)

  leg_type             VARCHAR2(20) NOT NULL, -- TO_HOTEL / FROM_HOTEL (timetable과 동일해야 함)
  place_code           VARCHAR2(30) NOT NULL, -- timetable과 동일해야 함
  depart_time          VARCHAR2(5)  NOT NULL, -- timetable과 동일해야 함

  ticket_qty           NUMBER NOT NULL,       -- ✅ 좌석 대신 "매수"
  leg_status           VARCHAR2(20) DEFAULT 'BOOKED' NOT NULL, -- BOOKED/CANCELED

  created_at           TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
  canceled_at          TIMESTAMP NULL,

  CONSTRAINT FK_shuttle_leg_booking
    FOREIGN KEY (fk_shuttle_booking_id)
    REFERENCES tbl_shuttle_booking(shuttle_booking_id)
    ON DELETE CASCADE,

  CONSTRAINT FK_shuttle_leg_timetable
    FOREIGN KEY (fk_timetable_id)
    REFERENCES tbl_shuttle_timetable(timetable_id),

  CONSTRAINT FK_shuttle_leg_place
    FOREIGN KEY (place_code)
    REFERENCES tbl_shuttle_place(place_code),

  CONSTRAINT CK_shuttle_leg_type
    CHECK (leg_type IN ('TO_HOTEL','FROM_HOTEL')),

  CONSTRAINT CK_shuttle_leg_depart_time
    CHECK (REGEXP_LIKE(depart_time, '^[0-2][0-9]:[0-5][0-9]$')),

  CONSTRAINT CK_shuttle_leg_ticket_qty
    CHECK (ticket_qty > 0),

  CONSTRAINT CK_shuttle_leg_status
    CHECK (leg_status IN ('BOOKED','CANCELED'))
);

-- 한 booking에 TO_HOTEL / FROM_HOTEL 각 1개씩만(왕복형 UI를 유지할 때)
CREATE UNIQUE INDEX UQ_shuttle_leg_per_booking
ON tbl_shuttle_booking_leg(fk_shuttle_booking_id, leg_type);

-- 조회 성능용(선택)
CREATE INDEX IX_shuttle_leg_timetable
ON tbl_shuttle_booking_leg(fk_timetable_id);



/* =========================================================
   6) VIEW: 내 셔틀 예약내역(마이페이지 카드용, 1행=예약세트)
   - 기존 seat_no 컬럼 제거, ticket_qty로 대체
   ========================================================= */
CREATE OR REPLACE VIEW vw_my_shuttle_reservation_card AS
SELECT
    b.shuttle_booking_id,
    b.fk_reservation_id,
    b.fk_hotel_id,
    b.fk_memberid,
    b.ride_date,
    b.status               AS booking_status,
    b.created_at,
    b.canceled_at,

    -- 호텔행(TO_HOTEL)
    l1.place_code          AS to_place_code,
    l1.depart_time         AS to_depart_time,
    l1.ticket_qty          AS to_ticket_qty,
    l1.leg_status          AS to_leg_status,

    -- 귀가행(FROM_HOTEL)
    l2.place_code          AS from_place_code,
    l2.depart_time         AS from_depart_time,
    l2.ticket_qty          AS from_ticket_qty,
    l2.leg_status          AS from_leg_status

FROM tbl_shuttle_booking b
LEFT JOIN tbl_shuttle_booking_leg l1
       ON l1.fk_shuttle_booking_id = b.shuttle_booking_id
      AND l1.leg_type = 'TO_HOTEL'
LEFT JOIN tbl_shuttle_booking_leg l2
       ON l2.fk_shuttle_booking_id = b.shuttle_booking_id
      AND l2.leg_type = 'FROM_HOTEL';


select * from tab;

select * from tbl_hotel;

commit; 

CREATE TABLE OUTLETS (
    outlet_id       NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    hotel_id        NUMBER NOT NULL, -- 호텔 담당자 테이블의 PK와 연결될 FK
    name            VARCHAR2(100) NOT NULL, -- 식당 이름 (예: 라세느, 모모야마)
    outlet_type     VARCHAR2(20) NOT NULL,  -- 식당 종류
    min_age_limit   NUMBER DEFAULT 0,       -- 노키즈존 여부 확인용 (0이면 제한 없음)
    is_adult_only   NUMBER(1) DEFAULT 0,    -- 성인 전용 여부 (0: 미제한, 1: 성인전용)
    description     CLOB,                   -- 식당 상세 설명 (긴 텍스트)
    
    -- Oracle 식 ENUM 구현: 타입 제한
    CONSTRAINT chk_outlet_type CHECK (outlet_type IN ('RESTAURANT', 'BAR', 'LOUNGE')),
    -- Oracle 식 BOOLEAN 구현: 0 또는 1만 허용
    CONSTRAINT chk_adult_only CHECK (is_adult_only IN (0, 1)),
    -- 호텔 테이블과의 연관 관계 (호텔 테이블 이름이 HOTELS라고 가정)
    CONSTRAINT fk_outlet_hotel FOREIGN KEY (hotel_id) REFERENCES tbl_hotel(hotel_id)
);

-- 인덱스 추가 (특정 호텔의 식당을 빠르게 조회하기 위함)
CREATE INDEX idx_outlet_hotel_id ON OUTLETS(hotel_id);


commit;


select * from dining_tables;

DESC DINING_TABLES;





CREATE TABLE tbl_dining (
    dining_id   NUMBER PRIMARY KEY,         -- 식당 고유 ID
    fk_hotel_id NUMBER NOT NULL,            -- 소속 호텔 ID
    name        VARCHAR2(100) NOT NULL,     -- 식당 이름 (라연, STAY 등)
    d_type      VARCHAR2(20),               -- 업종 (DINING, BAR)
    tel         VARCHAR2(20),               -- 식당 연락처
    floor       VARCHAR2(10)                -- 위치한 층수
);


-- 기존 테이블에 외래키 컬럼 추가 (이미 있다면 생략 가능)
ALTER TABLE dining_tables ADD (fk_dining_id NUMBER);

-- 외래키 제약조건 설정 (부모 식당이 삭제되면 같이 관리되게끔)
ALTER TABLE dining_tables 
ADD CONSTRAINT fk_dining_connection 
FOREIGN KEY (fk_dining_id) REFERENCES tbl_dining(dining_id);

commit;

-- [1. 부모 데이터: 식당/업장 등록]
INSERT INTO tbl_dining VALUES (1, 1, '라연', 'DINING', '02-2230-3367', '23F');
INSERT INTO tbl_dining VALUES (2, 1, '더 라이브러리', 'BAR', '02-2230-3388', '1F');
INSERT INTO tbl_dining VALUES (3, 2, 'STAY', 'DINING', '02-3213-1231', '81F');
INSERT INTO tbl_dining VALUES (4, 2, '바 81', 'BAR', '02-3213-1281', '81F');

-- [2. 자식 데이터: dining_tables에 데이터 연결]
-- 1. [신라] 라연(ID: 1) 소속 테이블
INSERT INTO dining_tables (TABLE_ID, FK_HOTEL_ID, TABLE_NUMBER, MAX_CAPACITY, MIN_CAPACITY, ZONE_NAME, IS_SPECIFIABLE, ACTIVE_YN)
VALUES (101, 1, 'R1', 4, 2, 'Main Hall', 'Y', 'Y');

-- 2. [신라] 더 라이브러리(ID: 2) 소속 테이블
INSERT INTO dining_tables (TABLE_ID, FK_HOTEL_ID, TABLE_NUMBER, MAX_CAPACITY, MIN_CAPACITY, ZONE_NAME, IS_SPECIFIABLE, ACTIVE_YN)
VALUES (102, 2, 'B1', 2, 1, 'Window Seat', 'N', 'Y');

-- 3. [시그니엘] STAY(ID: 3) 소속 테이블
INSERT INTO dining_tables (TABLE_ID, FK_HOTEL_ID, TABLE_NUMBER, MAX_CAPACITY, MIN_CAPACITY, ZONE_NAME, IS_SPECIFIABLE, ACTIVE_YN)
VALUES (201, 2, 'S1', 6, 4, 'Premium Zone', 'Y', 'Y');

-- 4. [시그니엘] 바 81(ID: 4) 소속 테이블
INSERT INTO dining_tables (TABLE_ID, FK_HOTEL_ID, TABLE_NUMBER, MAX_CAPACITY, MIN_CAPACITY, ZONE_NAME, IS_SPECIFIABLE, ACTIVE_YN)
VALUES (202, 2, 'SB1', 2, 1, 'Bar Counter', 'N', 'Y');

COMMIT;

COMMIT;


select * from tbl_dining;

-- 특정 테이블 ID만 지우고 싶을 때
DELETE FROM dining_tables WHERE TABLE_ID IN (101, 201);

-- 만약 부모 데이터(tbl_dining)도 잘못 넣었다면
DELETE FROM tbl_dining WHERE dining_id IN (1, 2, 3, 4);

-- 지운 후에는 꼭 확정(Commit) 해주세요!
COMMIT;

-- 호텔 1번에 소속된 식당 2개
INSERT INTO tbl_dining (dining_id, fk_hotel_id, name, d_type, tel, floor)
VALUES (1, 1, '라연', 'DINING', '02-2230-3367', '23F');

INSERT INTO tbl_dining (dining_id, fk_hotel_id, name, d_type, tel, floor)
VALUES (2, 1, '더 라이브러리', 'BAR', '02-2230-3388', '1F');

-- 호텔 2번에 소속된 식당 2개
INSERT INTO tbl_dining (dining_id, fk_hotel_id, name, d_type, tel, floor)
VALUES (3, 2, 'STAY', 'DINING', '02-3213-1231', '81F');

INSERT INTO tbl_dining (dining_id, fk_hotel_id, name, d_type, tel, floor)
VALUES (4, 2, '바 81', 'BAR', '02-3213-1281', '81F');

-- 1. [호텔 1번] 라연 소속 테이블
INSERT INTO dining_tables (TABLE_ID, FK_HOTEL_ID, TABLE_NUMBER, MAX_CAPACITY, MIN_CAPACITY, ZONE_NAME, IS_SPECIFIABLE, ACTIVE_YN)
VALUES (101, 1, 'R1', 4, 2, 'Main Hall', 'Y', 'Y');

-- 2. [호텔 1번] 더 라이브러리 소속 테이블
INSERT INTO dining_tables (TABLE_ID, FK_HOTEL_ID, TABLE_NUMBER, MAX_CAPACITY, MIN_CAPACITY, ZONE_NAME, IS_SPECIFIABLE, ACTIVE_YN)
VALUES (201, 1, 'B1', 2, 1, 'Window Seat', 'N', 'Y');

-- 3. [호텔 2번] STAY 소속 테이블
INSERT INTO dining_tables (TABLE_ID, FK_HOTEL_ID, TABLE_NUMBER, MAX_CAPACITY, MIN_CAPACITY, ZONE_NAME, IS_SPECIFIABLE, ACTIVE_YN)
VALUES (301, 2, 'S1', 6, 4, 'Premium Zone', 'Y', 'Y');

-- 4. [호텔 2번] 바 81 소속 테이블
INSERT INTO dining_tables (TABLE_ID, FK_HOTEL_ID, TABLE_NUMBER, MAX_CAPACITY, MIN_CAPACITY, ZONE_NAME, IS_SPECIFIABLE, ACTIVE_YN)
VALUES (401, 2, 'SB1', 2, 1, 'Bar Counter', 'N', 'Y');

COMMIT;

SELECT 
    D.NAME AS 식당명, 
    D.D_TYPE AS 타입, 
    T.TABLE_NUMBER AS 번호, 
    T.MAX_CAPACITY AS 최대인원
FROM tbl_dining D
JOIN dining_tables T ON D.FK_HOTEL_ID = T.FK_HOTEL_ID;

SELECT 
    D.NAME, 
    T.TABLE_NUMBER
FROM tbl_dining D
JOIN dining_tables T ON D.FK_HOTEL_ID = T.FK_HOTEL_ID
WHERE (D.NAME = '라연' AND T.ZONE_NAME = 'Main Hall')
   OR (D.NAME = '더 라이브러리' AND T.ZONE_NAME = 'Window Seat')
   -- 이런 식으로 일일이 지정해야 하는데, 이건 비효율적이죠.