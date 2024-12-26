package com.sparta.sportify.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    USER_NOT_FOUND(404, "존재하지 않는 유저입니다"),
    TEAM_NOT_FOUND(404, "존재하지 않는 팀입니다"),
    INSUFFICIENT_PERMISSION(403, "팀을 수정할 권한이 없습니다"),
    ALREADY_MEMBER(400, "이미 해당 팀에 가입되어 있습니다"),
    NOT_TEAM_MEMBER(403, "팀원이 아닙니다"),
    APPLICATION_NOT_FOUND(404, "신청 내역이 없습니다");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }


}
