package com.sparta.sportify.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

	USER_NOT_FOUND(404, "존재하지 않는 유저입니다"),
	NO_REGISTERED_STADIUM(404, "등록한 구장이 없습니다"),
	NO_STADIUM_FOUND(404, "구장이 존재하지 않습니다"),
	ONLY_OWN_STADIUM_CAN_BE_MODIFIED(403, "자신의 구장만 수정 가능합니다"),
	ONLY_OWN_STADIUM_CAN_BE_DELETED(403, "자신의 구장만 삭제 가능합니다"),
	STADIUM_TIME_ALREADY_EXISTS(409, "구장 시간이 이미 저장되었습니다"),
	UNAPPROVED_STADIUM(403, "승인되지 않은 구장입니다"),
	NO_STADIUM_TIME_FOUND(404, "저장된 구장 시간이 없습니다"),
	TEAM_NOT_FOUND(404, "존재하지 않는 팀입니다"),
	ONLY_TEAM_MEMBER_CAN_WRITE(403, "팀 멤버만 작성 가능합니다"),
	ONLY_TEAM_MEMBER_CAN_VIEW(403, "팀 멤버만 조회 가능합니다"),
	POST_NOT_FOUND(404, "게시물이 존재하지 않습니다"),
	ONLY_OWN_POST_CAN_BE_MODIFIED(403, "자신의 게시물만 수정 가능합니다"),
	ONLY_TEAM_MEMBER_CAN_MODIFY(403, "팀 멤버만 수정 가능합니다"),
	ONLY_OWN_POST_CAN_BE_DELETED(403, "자신의 게시물만 삭제 가능합니다"),
	ONLY_TEAM_MEMBER_CAN_DELETE(403, "팀 멤버만 삭제 가능합니다"),
	NOT_A_MEMBER_OF_THE_TEAM(403, "해당 팀이 아닙니다"),
	STADIUM_NAME_ALREADY_EXISTS(400, "구장 이름이 이미 존재합니다");

	private final int status;
	private final String message;

	ErrorCode(int status, String message) {
		this.status = status;
		this.message = message;
	}

}
