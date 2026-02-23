-- =====================================================================
-- FINAL (통합본 / Oracle)  + 관계 이해용 주석 포함
-- =====================================================================

------------------------------------------------------------
-- 1) 호텔(지점) 마스터
-- - 호텔/지점의 최상위 기준 테이블
-- - 대부분의 도메인(객실/다이닝/셔틀/공지/프로모션)이 fk_hotel_id 로 참조
------------------------------------------------------------
CREATE TABLE tbl_hotel (
  hotel_id        NUMBER PRIMARY KEY,             -- PK: 호텔 식별자
  hotel_name      VARCHAR2(50) NOT NULL,          -- 호텔명

  address         VARCHAR2(200),
  latitude        NUMBER(10,7),
  longitude       NUMBER(10,7),
  contact         VARCHAR2(50),
  hotel_desc      VARCHAR2(1000),

  approve_status  VARCHAR2(20) DEFAULT 'PENDING' NOT NULL, -- 승인 상태
  reject_reason   VARCHAR2(500),                            -- 반려 사유(상태=REJECTED 시)

  active_yn       CHAR(1) DEFAULT 'Y' NOT NULL,            -- 사용 여부
  created_by      VARCHAR2(50),                            -- 등록자(관리자ID 등)
  created_at      DATE DEFAULT SYSDATE NOT NULL,           -- 생성일

  CONSTRAINT CK_tbl_hotel_active_yn
    CHECK (active_yn IN ('Y','N')),
  CONSTRAINT CK_tbl_hotel_approve_status
    CHECK (approve_status IN ('PENDING','APPROVED','REJECTED'))
);

COMMENT ON TABLE tbl_hotel IS '호텔/지점 마스터. 객실/다이닝/셔틀/운영 데이터가 fk_hotel_id로 참조';
COMMENT ON COLUMN tbl_hotel.hotel_id IS '호텔 PK';
COMMENT ON COLUMN tbl_hotel.approve_status IS '승인상태(PENDING/APPROVED/REJECTED)';
COMMENT ON COLUMN tbl_hotel.active_yn IS '활성 여부(Y/N)';

-- 샘플 데이터
INSERT INTO tbl_hotel (hotel_id, hotel_name) VALUES (1, '호텔 시엘');
INSERT INTO tbl_hotel (hotel_id, hotel_name) VALUES (2, '르시엘');


------------------------------------------------------------
-- 2) 회원 등급 마스터 + 정책
-- - 등급 마스터(코드/명/정렬) + 등급별 혜택/적립율 정책(1:1)
------------------------------------------------------------
CREATE TABLE tbl_member_grade (
  grade_code   VARCHAR2(20) PRIMARY KEY,  -- PK: 등급 코드
  grade_name   VARCHAR2(20) NOT NULL,     -- 등급명
  sort_order   NUMBER NOT NULL            -- 표시/정렬 우선순위
);

COMMENT ON TABLE tbl_member_grade IS '회원 등급 마스터';
COMMENT ON COLUMN tbl_member_grade.grade_code IS '등급 코드(PK). 회원(tbl_member_security)이 참조';

CREATE TABLE tbl_member_grade_policy (
  grade_code                  VARCHAR2(20) PRIMARY KEY, -- PK/FK: 등급 코드(마스터와 1:1)
  annual_stay_nights_min      NUMBER NULL,              -- 연간 숙박일 최소(조건)
  valid_points_min            NUMBER NULL,              -- 유효포인트 최소(조건)
  room_point_rate_pct         NUMBER(5,2) NOT NULL,     -- 객실 적립율(%)
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

COMMENT ON TABLE tbl_member_grade_policy IS '회원 등급별 혜택/적립 정책(등급마스터와 1:1)';
COMMENT ON COLUMN tbl_member_grade_policy.grade_code IS '등급코드(PK/FK)';

INSERT INTO tbl_member_grade VALUES ('CLASSIC','클래식',1);
INSERT INTO tbl_member_grade VALUES ('SILVER','실버',2);
INSERT INTO tbl_member_grade VALUES ('GOLD','골드',3);
INSERT INTO tbl_member_grade VALUES ('PLATINUM','플레티넘',4);

INSERT INTO tbl_member_grade_policy
(grade_code, annual_stay_nights_min, valid_points_min, room_point_rate_pct, rooftop_lounge_pool_free_yn, breakfast_voucher_per_night)
VALUES ('CLASSIC', NULL, NULL, 3.00, 'N', 0);

INSERT INTO tbl_member_grade_policy VALUES ('SILVER', 5, 1500, 5.00, 'N', 0);
INSERT INTO tbl_member_grade_policy VALUES ('GOLD', 25, 20000, 7.00, 'Y', 0);
INSERT INTO tbl_member_grade_policy VALUES ('PLATINUM', 50, 70000, 10.00, 'Y', 1);


------------------------------------------------------------
-- 3) 회원 테이블 (PK=member_no, memberid는 UNIQUE)
-- - 로그인 계정(아이디/비번/활성) + 회원 프로필/포인트 + 등급
------------------------------------------------------------
CREATE TABLE tbl_member_security(
   member_no              NUMBER NOT NULL,        -- PK: 회원 번호(내부 식별자)
   memberid               VARCHAR2(50)   NOT NULL,-- UNIQUE: 로그인 ID
   passwd                 VARCHAR2(200)  NOT NULL,-- 해시 비밀번호
   enabled                CHAR(1)        DEFAULT '1' NOT NULL, -- 1=사용, 0=중지

   name                   NVARCHAR2(30)  NOT NULL,
   birthday               VARCHAR2(20)   NOT NULL,

   email                  VARCHAR2(200)  NOT NULL, -- UNIQUE(암호문 저장 가정)
   mobile                 VARCHAR2(200),

   postcode               VARCHAR2(10),
   address                VARCHAR2(200),
   detail_address         VARCHAR2(200),
   extra_address          VARCHAR2(200),

   point                  NUMBER DEFAULT 0 NOT NULL, -- 현재 포인트 잔액
   point_earned_total     NUMBER DEFAULT 0 NOT NULL, -- 누적 유효적립

   registerday            DATE DEFAULT SYSDATE,
   passwd_modify_date     DATE DEFAULT SYSDATE,
   last_login_date        DATE DEFAULT SYSDATE,

   grade_code             VARCHAR2(20), -- FK: 회원등급

   CONSTRAINT PK_tbl_member_security PRIMARY KEY(member_no),
   CONSTRAINT UQ_tbl_member_security_memberid UNIQUE(memberid),
   CONSTRAINT UQ_tbl_member_security_email UNIQUE(email),
   CONSTRAINT CK_tbl_member_security_enabled CHECK (enabled IN ('0','1')),
   CONSTRAINT CK_tbl_member_security_point_nonneg CHECK (point >= 0 AND point_earned_total >= 0),
   CONSTRAINT FK_member_security_grade
     FOREIGN KEY (grade_code) REFERENCES tbl_member_grade(grade_code)
);

COMMENT ON TABLE tbl_member_security IS '회원 계정/프로필 테이블. member_no가 권한/예약/로그인히스토리 등에서 FK로 참조';
COMMENT ON COLUMN tbl_member_security.member_no IS '회원 PK';
COMMENT ON COLUMN tbl_member_security.memberid IS '로그인 ID(UNIQUE)';
COMMENT ON COLUMN tbl_member_security.grade_code IS '회원등급 FK(tbl_member_grade.grade_code)';

CREATE SEQUENCE seq_tbl_member_security
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


------------------------------------------------------------
-- 4) 관리자 테이블 (PK=admin_no, adminid는 UNIQUE)
-- - HQ(총괄) / BRANCH(지점) 구분
-- - BRANCH는 반드시 fk_hotel_id를 가져야 함
------------------------------------------------------------
CREATE TABLE tbl_admin_security(
   admin_no              NUMBER NOT NULL,         -- PK: 관리자 번호
   adminid               VARCHAR2(50)   NOT NULL, -- UNIQUE: 관리자 로그인 ID
   passwd                VARCHAR2(200)  NOT NULL,
   enabled               CHAR(1)        DEFAULT '1' NOT NULL,

   name                  NVARCHAR2(30)  NOT NULL,
   email                 VARCHAR2(200)  NOT NULL,
   mobile                VARCHAR2(200),

   admin_type            VARCHAR2(20)   NOT NULL, -- HQ / BRANCH
   fk_hotel_id           NUMBER NULL,             -- BRANCH일 때 담당 호텔

   registerday           DATE DEFAULT SYSDATE,
   passwd_modify_date    DATE DEFAULT SYSDATE,
   last_login_date       DATE DEFAULT SYSDATE,

   CONSTRAINT PK_tbl_admin_security PRIMARY KEY(admin_no),
   CONSTRAINT UQ_tbl_admin_security_adminid UNIQUE(adminid),
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

COMMENT ON TABLE tbl_admin_security IS '관리자 계정. admin_type=HQ/BRANCH이며 BRANCH는 fk_hotel_id 필수';
COMMENT ON COLUMN tbl_admin_security.admin_no IS '관리자 PK';
COMMENT ON COLUMN tbl_admin_security.fk_hotel_id IS '지점관리자 담당 호텔 FK(tbl_hotel.hotel_id)';

CREATE SEQUENCE seq_tbl_admin_security
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


-- =====================================================================
-- 5)~6) 권한 테이블 (요청하신 스키마로 "교체")
-- =====================================================================

------------------------------------------------------------
-- 5) 회원 권한 (FK: member_no)
--  - 회원 1명(tbl_member_security)에게 여러 권한 부여(1:N)
--  - member_auth_no : 권한 "행"의 PK(시퀀스)
--  - (member_no, authority) UNIQUE : 중복 권한 방지
------------------------------------------------------------
CREATE TABLE tbl_member_authorities (
   member_auth_no  NUMBER NOT NULL,         -- PK(시퀀스)
   member_no       NUMBER NOT NULL,         -- FK -> tbl_member_security.member_no
   authority       VARCHAR2(50) NOT NULL,   -- ROLE_ prefix

   CONSTRAINT PK_tbl_member_authorities PRIMARY KEY(member_auth_no),
   CONSTRAINT UQ_tbl_member_authorities UNIQUE(member_no, authority),

   CONSTRAINT FK_tbl_member_authorities_member
     FOREIGN KEY(member_no) REFERENCES tbl_member_security(member_no) ON DELETE CASCADE,

   CONSTRAINT CK_tbl_member_authorities_prefix
     CHECK (authority LIKE 'ROLE\_%' ESCAPE '\')
);

COMMENT ON TABLE tbl_member_authorities IS '회원 권한(1:N). 회원 삭제 시 권한도 종속 삭제';
COMMENT ON COLUMN tbl_member_authorities.member_auth_no IS '회원권한 PK(시퀀스)';
COMMENT ON COLUMN tbl_member_authorities.member_no IS '회원 FK(tbl_member_security.member_no)';
COMMENT ON COLUMN tbl_member_authorities.authority IS '권한 문자열(ROLE_로 시작)';

CREATE SEQUENCE seq_tbl_member_authorities
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


------------------------------------------------------------
-- 6) 관리자 권한 (FK: admin_no)
--  - 관리자 1명(tbl_admin_security)에게 여러 권한 부여(1:N)
--  - admin_auth_no : 권한 "행"의 PK(시퀀스)
--  - (admin_no, authority) UNIQUE : 중복 권한 방지
------------------------------------------------------------
CREATE TABLE tbl_admin_authorities (
   admin_auth_no   NUMBER NOT NULL,         -- PK(시퀀스)
   admin_no        NUMBER NOT NULL,         -- FK -> tbl_admin_security.admin_no
   authority       VARCHAR2(50) NOT NULL,   -- ROLE_ prefix

   CONSTRAINT PK_tbl_admin_authorities PRIMARY KEY(admin_auth_no),
   CONSTRAINT UQ_tbl_admin_authorities UNIQUE(admin_no, authority),

   CONSTRAINT FK_tbl_admin_authorities_admin
     FOREIGN KEY(admin_no) REFERENCES tbl_admin_security(admin_no) ON DELETE CASCADE,

   CONSTRAINT CK_tbl_admin_authorities_prefix
     CHECK (authority LIKE 'ROLE\_%' ESCAPE '\')
);

COMMENT ON TABLE tbl_admin_authorities IS '관리자 권한(1:N). 관리자 삭제 시 권한도 종속 삭제';
COMMENT ON COLUMN tbl_admin_authorities.admin_auth_no IS '관리자권한 PK(시퀀스)';
COMMENT ON COLUMN tbl_admin_authorities.admin_no IS '관리자 FK(tbl_admin_security.admin_no)';
COMMENT ON COLUMN tbl_admin_authorities.authority IS '권한 문자열(ROLE_로 시작)';

CREATE SEQUENCE seq_tbl_admin_authorities
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


------------------------------------------------------------
-- 7) 로그인 히스토리(회원만) (FK: member_no)
------------------------------------------------------------
CREATE TABLE tbl_loginhistory
(
  historyno   NUMBER NOT NULL,              -- PK
  member_no   NUMBER NOT NULL,              -- FK: 로그인한 회원
  logindate   DATE DEFAULT SYSDATE NOT NULL,
  clientip    VARCHAR2(45) NOT NULL,

  CONSTRAINT PK_tbl_loginhistory PRIMARY KEY(historyno),
  CONSTRAINT FK_tbl_loginhistory_member
    FOREIGN KEY(member_no) REFERENCES tbl_member_security(member_no)
);

COMMENT ON TABLE tbl_loginhistory IS '회원 로그인 기록(회원만). member_no로 회원과 연결';
COMMENT ON COLUMN tbl_loginhistory.member_no IS '회원 FK(tbl_member_security.member_no)';

CREATE SEQUENCE seq_historyno
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


------------------------------------------------------------
-- (ERD) 호텔 승인 이력 관리
------------------------------------------------------------
CREATE TABLE HOTEL_APPROVAL_HISTORY (
    history_id   NUMBER PRIMARY KEY,        -- PK
    fk_hotel_id  NUMBER NOT NULL,           -- FK: 대상 호텔
    status       VARCHAR2(30) NOT NULL,     -- 처리 상태
    reason       VARCHAR2(500),             -- 반려/수정 사유
    decided_by   NUMBER,                    -- FK: 처리 관리자
    decided_at   DATE DEFAULT SYSDATE,

    CONSTRAINT fk_history_hotel
        FOREIGN KEY (fk_hotel_id)
        REFERENCES tbl_hotel(hotel_id),

    CONSTRAINT fk_history_admin
        FOREIGN KEY (decided_by)
        REFERENCES tbl_admin_security(admin_no),

    CONSTRAINT ck_history_status
        CHECK (status IN ('DRAFT','PENDING','NEED_REVISION','APPROVED','REJECTED'))
);

COMMENT ON TABLE HOTEL_APPROVAL_HISTORY IS '호텔 승인/반려/수정요청 이력';
COMMENT ON COLUMN HOTEL_APPROVAL_HISTORY.fk_hotel_id IS '호텔 FK(tbl_hotel.hotel_id)';
COMMENT ON COLUMN HOTEL_APPROVAL_HISTORY.decided_by IS '처리 관리자 FK(tbl_admin_security.admin_no)';

CREATE SEQUENCE SEQ_HOTEL_APPROVAL_HISTORY
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


------------------------------------------------------------
-- (ERD) 숙소 이미지
------------------------------------------------------------
CREATE TABLE HOTEL_IMAGE (
    image_id      NUMBER PRIMARY KEY,
    fk_hotel_id   NUMBER NOT NULL,          -- FK: 호텔
    image_url     VARCHAR2(500) NOT NULL,
    is_main       CHAR(1) DEFAULT 'N',
    sort_order    NUMBER DEFAULT 1,

    CONSTRAINT ck_hotel_image_main
      CHECK (is_main IN ('Y','N')),

    CONSTRAINT fk_img_hotel
      FOREIGN KEY (fk_hotel_id) REFERENCES tbl_hotel(hotel_id)
);

COMMENT ON TABLE HOTEL_IMAGE IS '호텔(지점) 이미지. fk_hotel_id로 호텔에 종속';
COMMENT ON COLUMN HOTEL_IMAGE.fk_hotel_id IS '호텔 FK(tbl_hotel.hotel_id)';

CREATE SEQUENCE SEQ_HOTEL_IMAGE
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


-- =====================================================================
-- 객실 도메인
-- =====================================================================

------------------------------------------------------------
-- ROOM_TYPE : 지점별 객실 타입(물리 객실 개별 생성 X)
-- - 지점(fk_hotel_id)별 객실 상품(타입) 정의
-- - 재고는 ROOM_STOCK에서 날짜별로 관리
------------------------------------------------------------
CREATE TABLE ROOM_TYPE (
    room_type_id    NUMBER PRIMARY KEY,
    fk_hotel_id     NUMBER NOT NULL,        -- FK: 호텔
    room_grade      VARCHAR2(100) NOT NULL,
    bed_type        VARCHAR2(50) NOT NULL,
    view_type       VARCHAR2(50) NOT NULL,
    room_name       VARCHAR2(200) NOT NULL,
    room_size       NUMBER,
    max_capacity    NUMBER NOT NULL,
    total_count     NUMBER NOT NULL,        -- 총 객실 수량(기본값/운영 기준)
    base_price      NUMBER NOT NULL,
    is_active       CHAR(1) DEFAULT 'Y',

    CONSTRAINT fk_roomtype_hotel
      FOREIGN KEY (fk_hotel_id) REFERENCES tbl_hotel(hotel_id),

    CONSTRAINT ck_roomtype_active
      CHECK (is_active IN ('Y','N'))
);

COMMENT ON TABLE ROOM_TYPE IS '지점별 객실 타입(상품). 예약은 room_type_id를 참조';
COMMENT ON COLUMN ROOM_TYPE.fk_hotel_id IS '호텔 FK(tbl_hotel.hotel_id)';

CREATE SEQUENCE SEQ_ROOM_TYPE
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


------------------------------------------------------------
-- ROOM_OPTION : 객실 부가 옵션(타입에 종속)
------------------------------------------------------------
CREATE TABLE ROOM_OPTION (
    option_id      NUMBER PRIMARY KEY,
    room_type_id   NUMBER NOT NULL,         -- FK: 객실 타입
    option_name    VARCHAR2(100) NOT NULL,
    extra_price    NUMBER DEFAULT 0,
    price_type     VARCHAR2(20),            -- 과금 기준

    CONSTRAINT fk_option_room_type
      FOREIGN KEY (room_type_id) REFERENCES ROOM_TYPE(room_type_id)
);

COMMENT ON TABLE ROOM_OPTION IS '객실 타입별 추가 옵션(1:N). 예약 시 RESERVATION_OPTION으로 선택';
COMMENT ON COLUMN ROOM_OPTION.room_type_id IS '객실타입 FK(ROOM_TYPE.room_type_id)';

CREATE SEQUENCE SEQ_ROOM_OPTION
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


------------------------------------------------------------
-- ROOM_IMAGE : 객실 이미지(타입에 종속)
------------------------------------------------------------
CREATE TABLE ROOM_IMAGE (
    image_id      NUMBER PRIMARY KEY,
    room_type_id  NUMBER NOT NULL,          -- FK: 객실 타입
    image_url     VARCHAR2(500) NOT NULL,
    is_main       CHAR(1) DEFAULT 'N',
    sort_order    NUMBER DEFAULT 1,

    CONSTRAINT ck_room_image_main
      CHECK (is_main IN ('Y','N')),

    CONSTRAINT fk_img_room
      FOREIGN KEY (room_type_id) REFERENCES ROOM_TYPE(room_type_id)
);

COMMENT ON TABLE ROOM_IMAGE IS '객실 타입 이미지';
COMMENT ON COLUMN ROOM_IMAGE.room_type_id IS '객실타입 FK(ROOM_TYPE.room_type_id)';

CREATE SEQUENCE SEQ_ROOM_IMAGE
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


------------------------------------------------------------
-- SEASON : 시즌(기간/가중치)
------------------------------------------------------------
CREATE TABLE SEASON (
    season_id   NUMBER PRIMARY KEY,
    season_name VARCHAR2(50) NOT NULL,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    price_rate  NUMBER(4,2) NOT NULL,

    CONSTRAINT ck_season_date CHECK (end_date >= start_date)
);

COMMENT ON TABLE SEASON IS '시즌 기간/요율(가격 가중치)';
CREATE SEQUENCE SEQ_SEASON
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


------------------------------------------------------------
-- WEEKDAY_RATE : 요일 가중치(지점별)
------------------------------------------------------------
CREATE TABLE WEEKDAY_RATE (
    weekday_id       NUMBER PRIMARY KEY,
    fk_hotel_id      NUMBER NOT NULL,       -- FK: 호텔
    day_of_week      NUMBER NOT NULL,       -- 1=일 ~ 7=토
    rate_multiplier  NUMBER(4,2) NOT NULL,

    CONSTRAINT fk_weekday_hotel
      FOREIGN KEY (fk_hotel_id) REFERENCES tbl_hotel(hotel_id),

    CONSTRAINT ck_weekday_range
      CHECK (day_of_week BETWEEN 1 AND 7),

    CONSTRAINT uk_weekday
      UNIQUE (fk_hotel_id, day_of_week)
);

COMMENT ON TABLE WEEKDAY_RATE IS '지점별 요일 가중치(1=일~7=토). (호텔,요일) 유니크';
COMMENT ON COLUMN WEEKDAY_RATE.fk_hotel_id IS '호텔 FK(tbl_hotel.hotel_id)';

CREATE SEQUENCE SEQ_WEEKDAY_RATE
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


------------------------------------------------------------
-- ROOM_STOCK : 날짜별 객실 재고(핵심)
-- - (room_type_id, stay_date) 1행 = 해당 날짜 판매 가능 수량/가격/마감 여부
------------------------------------------------------------
CREATE TABLE ROOM_STOCK (
    stock_id        NUMBER PRIMARY KEY,
    room_type_id    NUMBER NOT NULL,        -- FK: 객실 타입
    stay_date       DATE NOT NULL,          -- 투숙 날짜(1박 기준 날짜)
    available_count NUMBER NOT NULL,        -- 판매 가능 수량
    price_override  NUMBER,                 -- 해당 날짜 가격 덮어쓰기
    is_closed       CHAR(1) DEFAULT 'N',    -- 판매 마감 여부
    min_stay        NUMBER DEFAULT 1,       -- 최소 숙박일

    CONSTRAINT fk_stock_room_type
      FOREIGN KEY (room_type_id) REFERENCES ROOM_TYPE(room_type_id),

    CONSTRAINT uk_room_date
      UNIQUE (room_type_id, stay_date),

    CONSTRAINT ck_stock_non_negative
      CHECK (available_count >= 0),

    CONSTRAINT ck_stock_closed
      CHECK (is_closed IN ('Y','N'))
);

COMMENT ON TABLE ROOM_STOCK IS '객실 타입의 날짜별 재고/가격/마감. (room_type_id, stay_date) 유니크';
COMMENT ON COLUMN ROOM_STOCK.room_type_id IS '객실타입 FK(ROOM_TYPE.room_type_id)';

CREATE SEQUENCE SEQ_ROOM_STOCK
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


-- =====================================================================
-- 숙박 결제/예약
-- =====================================================================

------------------------------------------------------------
-- PAYMENT : 결제(회원 기준)
-- - 한 결제가 여러 예약(RESERVATION)을 묶을 수 있음(1:N)
------------------------------------------------------------
CREATE TABLE PAYMENT (
    payment_id       NUMBER PRIMARY KEY,
    member_no        NUMBER NOT NULL,          -- FK: 결제한 회원

    payment_amount   NUMBER NOT NULL,
    payment_method   VARCHAR2(50),

    payment_status   VARCHAR2(30) DEFAULT 'READY'
        CHECK (payment_status IN ('READY','PAID','FAILED','CANCELLED','PARTIAL_CANCEL')),

    imp_uid          VARCHAR2(200),
    paid_at          DATE,
    created_at       DATE DEFAULT SYSDATE,
    refunded_amount  NUMBER DEFAULT 0,

    CONSTRAINT fk_payment_member
      FOREIGN KEY (member_no) REFERENCES tbl_member_security(member_no)
);

COMMENT ON TABLE PAYMENT IS '숙박 결제 마스터. 회원(member_no) 기준으로 생성되며 여러 예약이 연결될 수 있음';
COMMENT ON COLUMN PAYMENT.member_no IS '회원 FK(tbl_member_security.member_no)';

CREATE SEQUENCE SEQ_PAYMENT
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


------------------------------------------------------------
-- RESERVATION : 숙박 예약(결제와 상태 분리)
------------------------------------------------------------
CREATE TABLE RESERVATION (
    reservation_id     NUMBER PRIMARY KEY,
    member_no          NUMBER NOT NULL,        -- FK: 예약 회원
    room_type_id       NUMBER NOT NULL,        -- FK: 예약 객실 타입

    payment_id         NUMBER,                 -- FK: 결제(옵션)

    checkin_date       DATE NOT NULL,
    checkout_date      DATE NOT NULL,
    guest_count        NUMBER NOT NULL,
    reservation_code   VARCHAR2(50) UNIQUE,

    reservation_status VARCHAR2(30) DEFAULT 'PENDING'
        CHECK (reservation_status IN ('PENDING','CONFIRMED','CANCELLED','EXPIRED','CHECKED_IN','CHECKED_OUT','NO_SHOW')),

    hold_expires_at    DATE,
    total_price        NUMBER NOT NULL,

    cancel_deadline    DATE,
    refund_amount      NUMBER,
    created_at         DATE DEFAULT SYSDATE,

    CONSTRAINT fk_res_member
      FOREIGN KEY (member_no) REFERENCES tbl_member_security(member_no),

    CONSTRAINT fk_res_room
      FOREIGN KEY (room_type_id) REFERENCES ROOM_TYPE(room_type_id),

    CONSTRAINT fk_res_payment
      FOREIGN KEY (payment_id) REFERENCES PAYMENT(payment_id),

    CONSTRAINT ck_guest_count CHECK (guest_count >= 1),
    CONSTRAINT ck_date CHECK (checkout_date > checkin_date)
);

COMMENT ON TABLE RESERVATION IS '숙박 예약. 회원/객실타입을 참조하며 결제(payment_id)는 옵션';
COMMENT ON COLUMN RESERVATION.member_no IS '회원 FK(tbl_member_security.member_no)';
COMMENT ON COLUMN RESERVATION.room_type_id IS '객실타입 FK(ROOM_TYPE.room_type_id)';
COMMENT ON COLUMN RESERVATION.payment_id IS '결제 FK(PAYMENT.payment_id)';

CREATE SEQUENCE SEQ_RESERVATION
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


------------------------------------------------------------
-- RESERVATION_OPTION : 예약별 선택 옵션(1:N)
------------------------------------------------------------
CREATE TABLE RESERVATION_OPTION (
    reservation_option_id NUMBER PRIMARY KEY,
    reservation_id        NUMBER NOT NULL,    -- FK: 예약
    option_id             NUMBER NOT NULL,    -- FK: 옵션
    option_count          NUMBER DEFAULT 1,

    CONSTRAINT fk_res_opt_res
      FOREIGN KEY (reservation_id) REFERENCES RESERVATION(reservation_id),

    CONSTRAINT fk_res_opt_opt
      FOREIGN KEY (option_id) REFERENCES ROOM_OPTION(option_id)
);

COMMENT ON TABLE RESERVATION_OPTION IS '예약별 옵션 선택 내역';
COMMENT ON COLUMN RESERVATION_OPTION.reservation_id IS '예약 FK(RESERVATION.reservation_id)';
COMMENT ON COLUMN RESERVATION_OPTION.option_id IS '옵션 FK(ROOM_OPTION.option_id)';

CREATE SEQUENCE SEQ_RESERVATION_OPTION
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


------------------------------------------------------------
-- PAYMENT_REFUND : 결제 환불 이력
------------------------------------------------------------
CREATE TABLE PAYMENT_REFUND (
    refund_id        NUMBER PRIMARY KEY,
    payment_id       NUMBER NOT NULL,        -- FK: 결제
    reservation_id   NUMBER NOT NULL,        -- FK: 예약(환불 대상)
    refund_amount    NUMBER NOT NULL,
    refund_type      VARCHAR2(20),
    refunded_at      DATE DEFAULT SYSDATE,

    CONSTRAINT fk_refund_payment
      FOREIGN KEY (payment_id) REFERENCES PAYMENT(payment_id),

    CONSTRAINT fk_refund_res
      FOREIGN KEY (reservation_id) REFERENCES RESERVATION(reservation_id),

    CONSTRAINT ck_refund_type
      CHECK (refund_type IN ('FULL','PARTIAL'))
);

COMMENT ON TABLE PAYMENT_REFUND IS '결제 환불 이력(예약 단위로 연결)';
COMMENT ON COLUMN PAYMENT_REFUND.payment_id IS '결제 FK(PAYMENT.payment_id)';
COMMENT ON COLUMN PAYMENT_REFUND.reservation_id IS '예약 FK(RESERVATION.reservation_id)';

CREATE SEQUENCE SEQ_PAYMENT_REFUND
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


-- =====================================================================
-- SHUTTLE (NO SEAT, TICKET-QUANTITY ONLY)
-- - 셔틀 단독예약 불가: 반드시 객실예약(RESERVATION) 존재해야 함
-- =====================================================================

------------------------------------------------------------
-- 8) 출발지(픽업장소) 마스터
------------------------------------------------------------
CREATE TABLE tbl_shuttle_place (
  place_code   VARCHAR2(30) PRIMARY KEY,     -- PK: 표준 코드
  place_name   NVARCHAR2(50) NOT NULL,
  active_yn    CHAR(1) DEFAULT 'Y' NOT NULL,

  CONSTRAINT CK_shuttle_place_active_yn
    CHECK (active_yn IN ('Y','N'))
);

COMMENT ON TABLE tbl_shuttle_place IS '셔틀 픽업장소 마스터(코드 기반). 시간표/예약상세가 place_code 참조';

INSERT INTO tbl_shuttle_place(place_code, place_name) VALUES ('SEOUL_STATION', N'서울역');
INSERT INTO tbl_shuttle_place(place_code, place_name) VALUES ('GIMPO',        N'김포공항');
INSERT INTO tbl_shuttle_place(place_code, place_name) VALUES ('INCHEON',      N'인천공항');


------------------------------------------------------------
-- 9) 셔틀 시간표(템플릿)
-- - 특정 호텔/방향/장소/출발시간 조합의 기본 운행 틀
------------------------------------------------------------
CREATE TABLE tbl_shuttle_timetable (
  timetable_id   NUMBER PRIMARY KEY,

  fk_hotel_id    NUMBER NOT NULL,            -- FK: 호텔
  leg_type       VARCHAR2(20) NOT NULL,      -- TO_HOTEL / FROM_HOTEL
  place_code     VARCHAR2(30) NOT NULL,      -- FK: 픽업장소
  depart_time    VARCHAR2(5)  NOT NULL,      -- HH24:MI
  capacity       NUMBER NOT NULL,            -- 기본 정원
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

COMMENT ON TABLE tbl_shuttle_timetable IS '셔틀 운행 템플릿(호텔/방향/픽업/시간). 날짜별 재고는 slot_stock에서 관리';
COMMENT ON COLUMN tbl_shuttle_timetable.fk_hotel_id IS '호텔 FK(tbl_hotel.hotel_id)';
COMMENT ON COLUMN tbl_shuttle_timetable.place_code IS '픽업장소 FK(tbl_shuttle_place.place_code)';

CREATE SEQUENCE seq_tbl_shuttle_timetable
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;

CREATE UNIQUE INDEX UQ_shuttle_timetable_key
ON tbl_shuttle_timetable(fk_hotel_id, leg_type, place_code, depart_time);

CREATE INDEX IX_shuttle_timetable_hotel
ON tbl_shuttle_timetable(fk_hotel_id, leg_type);


------------------------------------------------------------
-- 10) 날짜별 재고(= 날짜별좌석수)
-- - 시간표 1건(timetable) * 날짜 1건 당 1행
-- - booked_qty는 트랜잭션으로 증가/감소(오버부킹 방지)
------------------------------------------------------------
CREATE TABLE tbl_shuttle_slot_stock (
  stock_id        NUMBER PRIMARY KEY,

  fk_timetable_id NUMBER NOT NULL,           -- FK: 시간표
  ride_date       DATE NOT NULL,             -- 운행 날짜

  capacity        NUMBER NOT NULL,           -- 해당 날짜 정원(override 가능)
  booked_qty      NUMBER DEFAULT 0 NOT NULL, -- 예약된 티켓 합계

  updated_at      TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

  CONSTRAINT FK_shuttle_stock_timetable
    FOREIGN KEY (fk_timetable_id) REFERENCES tbl_shuttle_timetable(timetable_id),

  CONSTRAINT CK_shuttle_stock_capacity
    CHECK (capacity > 0),

  CONSTRAINT CK_shuttle_stock_booked_qty
    CHECK (booked_qty >= 0 AND booked_qty <= capacity)
);

COMMENT ON TABLE tbl_shuttle_slot_stock IS '셔틀 날짜별 재고(정원/예약수). (timetable, ride_date) 유니크';
COMMENT ON COLUMN tbl_shuttle_slot_stock.fk_timetable_id IS '시간표 FK(tbl_shuttle_timetable.timetable_id)';

CREATE SEQUENCE seq_tbl_shuttle_slot_stock
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;

CREATE UNIQUE INDEX UQ_shuttle_stock_key
ON tbl_shuttle_slot_stock(fk_timetable_id, ride_date);

CREATE INDEX IX_shuttle_stock_date
ON tbl_shuttle_slot_stock(ride_date);


------------------------------------------------------------
-- 11) 셔틀 예약 헤더 (객실예약 1건당 1건)
-- - 객실예약(RESERVATION)과 1:1 (UQ_shuttle_booking_reservation)
------------------------------------------------------------
CREATE TABLE tbl_shuttle_booking (
  shuttle_booking_id  NUMBER PRIMARY KEY,

  fk_reservation_id   NUMBER NOT NULL,        -- FK: 객실예약(필수)
  fk_hotel_id         NUMBER NOT NULL,        -- FK: 호텔(조회 편의/무결성)
  fk_member_no        NUMBER NOT NULL,        -- FK: 회원

  ride_date           DATE NOT NULL,
  status              VARCHAR2(20) DEFAULT 'BOOKED' NOT NULL,

  created_at          TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
  canceled_at         TIMESTAMP NULL,

  CONSTRAINT FK_shuttle_booking_reservation
    FOREIGN KEY (fk_reservation_id) REFERENCES RESERVATION(reservation_id),

  CONSTRAINT FK_shuttle_booking_member
    FOREIGN KEY (fk_member_no) REFERENCES tbl_member_security(member_no),

  CONSTRAINT FK_shuttle_booking_hotel
    FOREIGN KEY (fk_hotel_id) REFERENCES tbl_hotel(hotel_id),

  CONSTRAINT CK_shuttle_booking_status
    CHECK (status IN ('BOOKED','CANCELED'))
);

COMMENT ON TABLE tbl_shuttle_booking IS '셔틀 예약 헤더. 객실예약(RESERVATION) 존재가 전제이며 예약 1건당 1건(1:1)';
COMMENT ON COLUMN tbl_shuttle_booking.fk_reservation_id IS '객실예약 FK(RESERVATION.reservation_id)';
COMMENT ON COLUMN tbl_shuttle_booking.fk_member_no IS '회원 FK(tbl_member_security.member_no)';

CREATE SEQUENCE seq_tbl_shuttle_booking
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;

CREATE UNIQUE INDEX UQ_shuttle_booking_reservation
ON tbl_shuttle_booking(fk_reservation_id);

CREATE INDEX IX_shuttle_booking_member_date
ON tbl_shuttle_booking(fk_member_no, ride_date);


------------------------------------------------------------
-- 12) 셔틀 예약 상세(레그)
-- - 한 예약헤더에 TO/FROM 레그가 각각 최대 1개씩(유니크 제약)
-- - 실제 수량(ticket_qty)은 재고의 booked_qty로 합산됨
------------------------------------------------------------
CREATE TABLE tbl_shuttle_booking_leg (
  shuttle_leg_id        NUMBER PRIMARY KEY,

  fk_shuttle_booking_id NUMBER NOT NULL,    -- FK: 셔틀예약헤더
  fk_timetable_id       NUMBER NOT NULL,    -- FK: 시간표(템플릿)

  leg_type              VARCHAR2(20) NOT NULL,
  place_code            VARCHAR2(30) NOT NULL,
  depart_time           VARCHAR2(5)  NOT NULL,

  ticket_qty            NUMBER NOT NULL,
  leg_status            VARCHAR2(20) DEFAULT 'BOOKED' NOT NULL,

  created_at            TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
  canceled_at           TIMESTAMP NULL,

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

COMMENT ON TABLE tbl_shuttle_booking_leg IS '셔틀 예약 상세(레그). 한 헤더에 왕복 레그(TO/FROM)를 각각 0~1개 보유';
COMMENT ON COLUMN tbl_shuttle_booking_leg.fk_shuttle_booking_id IS '셔틀예약헤더 FK(tbl_shuttle_booking.shuttle_booking_id)';
COMMENT ON COLUMN tbl_shuttle_booking_leg.fk_timetable_id IS '시간표 FK(tbl_shuttle_timetable.timetable_id)';

CREATE SEQUENCE seq_tbl_shuttle_booking_leg
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;

CREATE UNIQUE INDEX UQ_shuttle_leg_per_booking
ON tbl_shuttle_booking_leg(fk_shuttle_booking_id, leg_type);

CREATE INDEX IX_shuttle_leg_timetable
ON tbl_shuttle_booking_leg(fk_timetable_id);


------------------------------------------------------------
-- 13) VIEW: 내 셔틀 예약내역(마이페이지 카드용)
------------------------------------------------------------
CREATE OR REPLACE VIEW vw_my_shuttle_reservation_card AS
SELECT
    b.shuttle_booking_id,
    b.fk_reservation_id,
    b.fk_hotel_id,
    b.fk_member_no,
    b.ride_date,
    b.status               AS booking_status,
    b.created_at,
    b.canceled_at,

    l1.place_code          AS to_place_code,
    l1.depart_time         AS to_depart_time,
    l1.ticket_qty          AS to_ticket_qty,
    l1.leg_status          AS to_leg_status,

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

-- VIEW 주석은 DBMS에 따라 별도 관리(오라클은 COMMENT ON VIEW 가능)
COMMENT ON TABLE vw_my_shuttle_reservation_card IS '마이페이지 셔틀예약 카드용 뷰(헤더+왕복 레그를 가로로 펼침)';


-- =====================================================================
-- DINING (ERD 범위만)
-- =====================================================================

CREATE TABLE Dining_Tables (
    table_id        NUMBER PRIMARY KEY,
    fk_hotel_id     NUMBER NOT NULL,        -- FK: 호텔
    table_number    VARCHAR2(20) NOT NULL,
    max_capacity    NUMBER NOT NULL,
    min_capacity    NUMBER DEFAULT 1,
    zone_name       VARCHAR2(50),
    is_specifiable  CHAR(1) DEFAULT 'Y',
    active_yn       CHAR(1) DEFAULT 'Y' NOT NULL,

    CONSTRAINT fk_dining_table_hotel
      FOREIGN KEY (fk_hotel_id) REFERENCES tbl_hotel(hotel_id),

    CONSTRAINT ck_dining_table_spec
      CHECK (is_specifiable IN ('Y','N')),

    CONSTRAINT ck_dining_table_active
      CHECK (active_yn IN ('Y','N')),

    CONSTRAINT ck_dining_table_capacity
      CHECK (max_capacity >= 1 AND min_capacity >= 1 AND max_capacity >= min_capacity)
);

COMMENT ON TABLE Dining_Tables IS '다이닝 테이블(좌석) 마스터. 지점별로 운영';
COMMENT ON COLUMN Dining_Tables.fk_hotel_id IS '호텔 FK(tbl_hotel.hotel_id)';

CREATE SEQUENCE SEQ_Dining_Tables
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


CREATE TABLE Dining_Payments (
    payment_id         NUMBER PRIMARY KEY,
    amount             NUMBER NOT NULL,
    original_amount    NUMBER NOT NULL,
    cancellation_fee   NUMBER DEFAULT 0,
    payment_method     VARCHAR2(50),
    status             VARCHAR2(30) DEFAULT 'PAID',
    pg_tid             VARCHAR2(100),
    paid_at            TIMESTAMP DEFAULT SYSTIMESTAMP,
    refunded_at        TIMESTAMP NULL,

    CONSTRAINT ck_dining_payment_status
      CHECK (status IN ('PAID','PARTIAL_REFUNDED','FULLY_REFUNDED','FAILED'))
);

COMMENT ON TABLE Dining_Payments IS '다이닝 예약금 결제. 숙박 PAYMENT와 분리';
CREATE SEQUENCE SEQ_Dining_Payments
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


CREATE TABLE Dining_Reservations (
    dining_reservation_id NUMBER PRIMARY KEY,
    fk_hotel_id           NUMBER NOT NULL,     -- FK: 호텔
    outlet_id             NUMBER,
    table_id              NUMBER,              -- FK: 다이닝 테이블(선택)
    fk_member_no          NUMBER NULL,         -- FK: 회원(회원예약이면 값)

    guest_name            VARCHAR2(50),
    guest_phone           VARCHAR2(20),

    adult_count           NUMBER DEFAULT 1,
    child_count           NUMBER DEFAULT 0,
    infant_count          NUMBER DEFAULT 0,

    res_date              DATE NOT NULL,
    res_time              VARCHAR2(5) NOT NULL,

    special_requests      CLOB,
    allergy_info          CLOB,

    status                VARCHAR2(30) DEFAULT 'WAITING_PAYMENT',
    payment_id            NUMBER,              -- FK: 다이닝 결제(선택)

    created_at            TIMESTAMP DEFAULT SYSTIMESTAMP,
    updated_at            TIMESTAMP DEFAULT SYSTIMESTAMP,

    CONSTRAINT fk_dining_res_hotel
      FOREIGN KEY (fk_hotel_id) REFERENCES tbl_hotel(hotel_id),

    CONSTRAINT fk_dining_res_table
      FOREIGN KEY (table_id) REFERENCES Dining_Tables(table_id),

    CONSTRAINT fk_dining_res_member
      FOREIGN KEY (fk_member_no) REFERENCES tbl_member_security(member_no),

    CONSTRAINT fk_dining_res_payment
      FOREIGN KEY (payment_id) REFERENCES Dining_Payments(payment_id),

    CONSTRAINT ck_dining_res_time
      CHECK (REGEXP_LIKE(res_time, '^[0-2][0-9]:[0-5][0-9]$')),

    CONSTRAINT ck_dining_res_status
      CHECK (status IN ('WAITING_PAYMENT','CONFIRMED','VISITED','CANCELLED','NOSHOW')),

    CONSTRAINT ck_dining_res_counts
      CHECK (adult_count >= 0 AND child_count >= 0 AND infant_count >= 0)
);

COMMENT ON TABLE Dining_Reservations IS '다이닝 예약. 회원예약이면 fk_member_no 연결, 비회원이면 NULL';
COMMENT ON COLUMN Dining_Reservations.fk_hotel_id IS '호텔 FK(tbl_hotel.hotel_id)';
COMMENT ON COLUMN Dining_Reservations.fk_member_no IS '회원 FK(tbl_member_security.member_no)';
COMMENT ON COLUMN Dining_Reservations.payment_id IS '다이닝 결제 FK(Dining_Payments.payment_id)';

CREATE SEQUENCE SEQ_Dining_Reservations
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;

CREATE INDEX idx_dining_res_date_time ON Dining_Reservations(res_date, res_time);
CREATE INDEX idx_dining_guest_phone   ON Dining_Reservations(guest_phone);


CREATE TABLE Dining_Pricing_Policies (
    pricing_policy_id     NUMBER PRIMARY KEY,
    dining_reservation_id NUMBER NOT NULL,     -- FK: 다이닝 예약
    category              VARCHAR2(20) NOT NULL,
    price                 NUMBER DEFAULT 0,

    CONSTRAINT fk_dining_price_res
      FOREIGN KEY (dining_reservation_id) REFERENCES Dining_Reservations(dining_reservation_id),

    CONSTRAINT ck_dining_price_category
      CHECK (category IN ('ADULT','CHILD','INFANT')),

    CONSTRAINT ck_dining_price_nonneg
      CHECK (price >= 0)
);

COMMENT ON TABLE Dining_Pricing_Policies IS '다이닝 예약 건 기준 연령별 가격 정책';
COMMENT ON COLUMN Dining_Pricing_Policies.dining_reservation_id IS '다이닝예약 FK(Dining_Reservations.dining_reservation_id)';

CREATE SEQUENCE SEQ_Dining_Pricing_Policies
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


CREATE TABLE Dining_Refund_Policies (
    refund_policy_id      NUMBER PRIMARY KEY,
    dining_reservation_id NUMBER NOT NULL,     -- FK: 다이닝 예약
    days_before           NUMBER,
    refund_rate           NUMBER,

    CONSTRAINT fk_dining_refund_res
      FOREIGN KEY (dining_reservation_id) REFERENCES Dining_Reservations(dining_reservation_id),

    CONSTRAINT ck_dining_refund_rate
      CHECK (refund_rate BETWEEN 0 AND 100)
);

COMMENT ON TABLE Dining_Refund_Policies IS '다이닝 예약 건 기준 환불 정책(일수/환불율)';
COMMENT ON COLUMN Dining_Refund_Policies.dining_reservation_id IS '다이닝예약 FK(Dining_Reservations.dining_reservation_id)';

CREATE SEQUENCE SEQ_Dining_Refund_Policies
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


-- =====================================================================
-- 게시판/운영 (공지/FAQ/QnA/답변)
-- =====================================================================

CREATE TABLE NOTICES (
    notice_id   NUMBER PRIMARY KEY,
    admin_no    NUMBER,                          -- FK: 작성 관리자(선택)
    fk_hotel_id NUMBER NOT NULL,                 -- FK: 지점 공지 대상
    title       VARCHAR2(200) NOT NULL,
    content     CLOB NOT NULL,
    is_top      CHAR(1) DEFAULT 'N' CHECK (is_top IN ('Y', 'N')),
    created_at  DATE DEFAULT SYSDATE,

    CONSTRAINT fk_notice_admin
      FOREIGN KEY (admin_no) REFERENCES tbl_admin_security(admin_no),

    CONSTRAINT fk_notice_hotel
      FOREIGN KEY (fk_hotel_id) REFERENCES tbl_hotel(hotel_id)
);

COMMENT ON TABLE NOTICES IS '공지사항(지점/전체). fk_hotel_id로 지점에 귀속';
COMMENT ON COLUMN NOTICES.admin_no IS '작성 관리자 FK(tbl_admin_security.admin_no)';
COMMENT ON COLUMN NOTICES.fk_hotel_id IS '호텔 FK(tbl_hotel.hotel_id)';

CREATE SEQUENCE SEQ_NOTICE_ID
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


CREATE TABLE FAQS (
    faq_id      NUMBER PRIMARY KEY,
    fk_hotel_id NUMBER NOT NULL,                 -- FK: 지점별 FAQ
    category    VARCHAR2(50),
    title       VARCHAR2(200) NOT NULL,
    content     CLOB NOT NULL,
    admin_no    NUMBER,
    view_count  NUMBER DEFAULT 0,
    created_at  DATE DEFAULT SYSDATE,

    CONSTRAINT fk_faq_admin
      FOREIGN KEY (admin_no) REFERENCES tbl_admin_security(admin_no),

    CONSTRAINT fk_faq_hotel
      FOREIGN KEY (fk_hotel_id) REFERENCES tbl_hotel(hotel_id)
);

COMMENT ON TABLE FAQS IS 'FAQ(자주묻는질문). 지점별 운영';
COMMENT ON COLUMN FAQS.fk_hotel_id IS '호텔 FK(tbl_hotel.hotel_id)';

CREATE SEQUENCE SEQ_FAQ_ID
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


CREATE TABLE QUESTIONS (
    qna_id      NUMBER PRIMARY KEY,
    fk_hotel_id NUMBER NOT NULL,                 -- FK: 문의 대상 지점
    writer_name VARCHAR2(50) NOT NULL,
    title       VARCHAR2(200) NOT NULL,
    content     CLOB NOT NULL,
    status      VARCHAR2(20) DEFAULT 'WAITING' CHECK (status IN ('WAITING', 'ANSWERED')),
    is_secret   CHAR(1) DEFAULT 'N' CHECK (is_secret IN ('Y', 'N')),
    created_at  DATE DEFAULT SYSDATE,

    CONSTRAINT fk_qna_hotel
      FOREIGN KEY (fk_hotel_id) REFERENCES tbl_hotel(hotel_id)
);

COMMENT ON TABLE QUESTIONS IS 'QnA 질문(고객문의). 답변은 ANSWERS가 1:N로 연결';
COMMENT ON COLUMN QUESTIONS.fk_hotel_id IS '호텔 FK(tbl_hotel.hotel_id)';

CREATE SEQUENCE SEQ_QNA_ID
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


CREATE TABLE ANSWERS (
    answer_id   NUMBER PRIMARY KEY,
    qna_id      NUMBER NOT NULL,                 -- FK: 질문
    admin_no    NUMBER NOT NULL,                 -- FK: 답변 관리자
    content     CLOB NOT NULL,
    created_at  DATE DEFAULT SYSDATE,

    CONSTRAINT fk_answer_qna
      FOREIGN KEY (qna_id) REFERENCES QUESTIONS(qna_id) ON DELETE CASCADE,

    CONSTRAINT fk_answer_admin
      FOREIGN KEY (admin_no) REFERENCES tbl_admin_security(admin_no)
);

COMMENT ON TABLE ANSWERS IS 'QnA 답변. 질문 삭제 시 답변도 종속 삭제';
COMMENT ON COLUMN ANSWERS.qna_id IS '질문 FK(QUESTIONS.qna_id)';
COMMENT ON COLUMN ANSWERS.admin_no IS '관리자 FK(tbl_admin_security.admin_no)';

CREATE SEQUENCE SEQ_ANSWER_ID
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


-- =====================================================================
-- 수익/프로모션
-- =====================================================================

CREATE TABLE PROMOTION_MASTER (
    promotion_id    NUMBER PRIMARY KEY,
    fk_hotel_id     NUMBER NOT NULL,          -- FK: 지점
    title           VARCHAR2(200) NOT NULL,
    promotion_type  VARCHAR2(50),
    discount_rate   NUMBER(5, 2),
    discount_amount NUMBER DEFAULT 0,
    start_date      DATE,
    end_date        DATE,
    is_active       NUMBER(1) DEFAULT 1,
    created_at      DATE DEFAULT SYSDATE,

    CONSTRAINT fk_promotion_hotel
      FOREIGN KEY (fk_hotel_id) REFERENCES tbl_hotel(hotel_id)
);

COMMENT ON TABLE PROMOTION_MASTER IS '프로모션 정의(마스터). 예약과의 적용 관계는 RESERVATION_PROMOTION_MAPPING';
COMMENT ON COLUMN PROMOTION_MASTER.fk_hotel_id IS '호텔 FK(tbl_hotel.hotel_id)';

CREATE SEQUENCE SEQ_PROMOTION_ID
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


CREATE TABLE PROMOTION_BANNER (
    banner_id    NUMBER PRIMARY KEY,
    fk_hotel_id  NUMBER NOT NULL,            -- FK: 지점
    title        VARCHAR2(200) NOT NULL,
    promo_type   VARCHAR2(50),
    price        NUMBER,
    start_date   DATE,
    end_date     DATE,
    image_url    VARCHAR2(500),
    room_type    VARCHAR2(50),
    benefits     CLOB,
    created_at   DATE DEFAULT SYSDATE,
    active_yn    CHAR(1) DEFAULT 'Y' CHECK (active_yn IN ('Y','N')),

    CONSTRAINT fk_banner_hotel
      FOREIGN KEY (fk_hotel_id) REFERENCES tbl_hotel(hotel_id)
);

COMMENT ON TABLE PROMOTION_BANNER IS '메인 전시용 배너(프로모션 노출 데이터). 실제 정책/할인은 PROMOTION_MASTER 기준';
COMMENT ON COLUMN PROMOTION_BANNER.fk_hotel_id IS '호텔 FK(tbl_hotel.hotel_id)';

CREATE SEQUENCE SEQ_PROMO_BANNER_ID
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


CREATE TABLE DAILY_REVENUE_STATS (
    stat_date       DATE,
    fk_hotel_id     NUMBER,
    total_revenue   NUMBER DEFAULT 0,
    sold_rooms      NUMBER DEFAULT 0,
    total_rooms     NUMBER DEFAULT 100,
    adr             NUMBER DEFAULT 0,
    revpar          NUMBER DEFAULT 0,
    occupancy_rate  NUMBER(5, 2) DEFAULT 0,
    PRIMARY KEY (stat_date, fk_hotel_id),

    CONSTRAINT fk_daily_rev_hotel
      FOREIGN KEY (fk_hotel_id) REFERENCES tbl_hotel(hotel_id)
);

COMMENT ON TABLE DAILY_REVENUE_STATS IS '일별 매출/ADR/RevPAR/가동률 통계(지점 단위)';
COMMENT ON COLUMN DAILY_REVENUE_STATS.fk_hotel_id IS '호텔 FK(tbl_hotel.hotel_id)';


CREATE TABLE DAILY_PROMOTION_STATS (
    stat_date       DATE,
    fk_hotel_id     NUMBER,
    promotion_id    NUMBER,
    promo_revenue   NUMBER DEFAULT 0,
    promo_count     NUMBER DEFAULT 0,
    PRIMARY KEY (stat_date, fk_hotel_id, promotion_id),

    CONSTRAINT fk_daily_promo_hotel
      FOREIGN KEY (fk_hotel_id) REFERENCES tbl_hotel(hotel_id),

    CONSTRAINT fk_daily_promo_promotion
      FOREIGN KEY (promotion_id) REFERENCES PROMOTION_MASTER(promotion_id)
);

COMMENT ON TABLE DAILY_PROMOTION_STATS IS '일별 프로모션 성과(지점+프로모션 단위)';
COMMENT ON COLUMN DAILY_PROMOTION_STATS.promotion_id IS '프로모션 FK(PROMOTION_MASTER.promotion_id)';


CREATE TABLE RESERVATION_PROMOTION_MAPPING (
    mapping_id            NUMBER PRIMARY KEY,
    reservation_id        NUMBER NOT NULL,      -- FK: 예약
    promotion_id          NUMBER NOT NULL,      -- FK: 프로모션
    applied_price_at_time NUMBER,
    benefit_delivered     VARCHAR2(500),
    applied_at            DATE DEFAULT SYSDATE,

    CONSTRAINT fk_map_reservation
      FOREIGN KEY (reservation_id) REFERENCES RESERVATION(reservation_id),

    CONSTRAINT fk_map_promotion
      FOREIGN KEY (promotion_id) REFERENCES PROMOTION_MASTER(promotion_id)
);

COMMENT ON TABLE RESERVATION_PROMOTION_MAPPING IS '예약에 어떤 프로모션이 적용되었는지 기록(예약-프로모션 매핑)';
COMMENT ON COLUMN RESERVATION_PROMOTION_MAPPING.reservation_id IS '예약 FK(RESERVATION.reservation_id)';
COMMENT ON COLUMN RESERVATION_PROMOTION_MAPPING.promotion_id IS '프로모션 FK(PROMOTION_MASTER.promotion_id)';

CREATE SEQUENCE SEQ_MAPPING_ID
START WITH 1 INCREMENT BY 1 NOMAXVALUE NOMINVALUE NOCYCLE NOCACHE;


COMMIT;