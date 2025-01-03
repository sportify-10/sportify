package com.sparta.sportify.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    USER_NOT_FOUND(404, "존재하지 않는 유저입니다"),
    COUPON_NOT_FOUND(404, "존재하지 않는 쿠폰입니다"),
    COUPON_ALREADY_EXISTS(409, "쿠폰이 이미 존재합니다"),
    COUPON_ALREADY_USED(409, "이미 사용하신 쿠폰입니다"),
    STADIUM_NOT_OPERATIONAL(400, "구장이 운영중이 아닙니다"),
    INVALID_OPERATION_TIME(400, "구장 운영시간이 맞지 않습니다"),
    DUPLICATE_RESERVATION(409, "이미 중복된 시간에 예약을 하였습니다"),
    USER_INFO_INVALID(400, "유저 정보가 잘못되었습니다"),
    TEAM_NOT_FOUND(404, "팀을 찾을 수 없습니다"),
    NOT_ENOUGH_SPOTS_FOR_TEAM(400, "요청한 인원수보다 남은 자리수가 적습니다."),
    INVALID_REQUEST(400, "잘못된 요청"),
    RESERVATION_NOT_FOUND(404, "찾을 수 없는 예약 ID입니다"),
    USER_INFO_MISMATCH(400, "해당 유저 정보가 다릅니다"),
    DUPLICATE_EMAIL(409, "중복된 이메일이 존재합니다"),
    INVALID_EMAIL(404, "존재하지 않는 이메일입니다"),
    PASSWORD_MISMATCH(400, "비밀번호가 일치하지 않습니다"),
    TEAM_NOT_REGISTERED(404, "가입되어 있는 팀이 없습니다"),
    UNSUPPORTED_OAUTH_PROVIDER(400, "지원하지 않는 OAuth 공급자입니다"),
    MISSING_OAUTH2_USER_INFO(400, "OAuth2 사용자 정보가 없습니다"),
    EXPIRED_COUPON(400, "만료된 쿠폰입니다"),
    INVALID_TOKEN(400, "유효하지 않은 토큰입니다."),
    TOKEN_PARSING_ERROR(400, "토큰 파싱 오류입니다."),
    TOKEN_EXPIRED(400, "토큰이 만료되었습니다."),
    UNSUPPORTED_JWT_TOKEN(400, "지원되지 않는 JWT 토큰입니다."),
    INVALID_JWT_STRUCTURE(400, "잘못된 JWT 구조입니다."),
    INVALID_TOKEN_SIGNATURE(400, "잘못된 토큰 서명입니다."),
    DELETED_USER(400, "삭제된 사용자입니다."),
    NO_REGISTERED_STADIUM(404, "등록한 구장이 없습니다"),
    NO_STADIUM_FOUND(404, "구장이 존재하지 않습니다"),
    ONLY_OWN_STADIUM_CAN_BE_MODIFIED(403, "자신의 구장만 수정 가능합니다"),
    ONLY_OWN_STADIUM_CAN_BE_DELETED(403, "자신의 구장만 삭제 가능합니다"),
    STADIUM_TIME_ALREADY_EXISTS(409, "구장 시간이 이미 저장되었습니다"),
    UNAPPROVED_STADIUM(403, "승인되지 않은 구장입니다"),
    NO_STADIUM_TIME_FOUND(404, "저장된 구장 시간이 없습니다"),
    ONLY_TEAM_MEMBER_CAN_WRITE(403, "팀 멤버만 작성 가능합니다"),
    ONLY_TEAM_MEMBER_CAN_VIEW(403, "팀 멤버만 조회 가능합니다"),
    POST_NOT_FOUND(404, "게시물이 존재하지 않습니다"),
    ONLY_OWN_POST_CAN_BE_MODIFIED(403, "자신의 게시물만 수정 가능합니다"),
    ONLY_TEAM_MEMBER_CAN_MODIFY(403, "팀 멤버만 수정 가능합니다"),
    ONLY_OWN_POST_CAN_BE_DELETED(403, "자신의 게시물만 삭제 가능합니다"),
    ONLY_TEAM_MEMBER_CAN_DELETE(403, "팀 멤버만 삭제 가능합니다"),
    NOT_A_MEMBER_OF_THE_TEAM(403, "해당 팀이 아닙니다"),
    STADIUM_NAME_ALREADY_EXISTS(400, "구장 이름이 이미 존재합니다"),
    INSUFFICIENT_PERMISSION(403, "팀을 수정할 권한이 없습니다"),
    ALREADY_MEMBER(400, "이미 해당 팀에 가입되어 있습니다"),
    NOT_TEAM_MEMBER(403, "팀원이 아닙니다"),
    APPLICATION_NOT_FOUND(404, "신청 내역이 없습니다"),
    CASH_LOG_NOT_FOUND(404, "캐시 사용 내역이 없습니다."),
    MATCH_NOT_FOUND(404, "경기를 찾을 수 없습니다."),
    RESERVATION_NOT_FOUND_FOR_TEAM(404, "팀에 대한 예약을 찾을 수 없습니다."),
    MATCHRESULT_NOT_FOUND(404, "경기 결과를 찾을 수 없습니다."),
    STADIUMTIME_NOT_FOUND(404, "경기 시간을 찾을 수 없습니다"),
    ERR_USER_LIMIT_EXCEEDED(429, "인원수가 초과되었습니다."),
    ALREADY_PENDING(400, "이미 신청 상태입니다"),
    INVALID_ROLE(403, "없는 권한입니다"),
    CHARGE_LOG_NOT_FOUND(404, "승인할 결제 로그를 찾을수 없습니다"),
    REFUND_LOG_NOT_FOUND(404, "환불할 결제 로그를 찾을수 없습니다");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }


}
