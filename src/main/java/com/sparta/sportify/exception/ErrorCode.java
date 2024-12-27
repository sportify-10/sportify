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


    ;

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }


}
