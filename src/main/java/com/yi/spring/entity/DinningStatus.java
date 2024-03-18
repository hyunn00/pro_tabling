package com.yi.spring.entity;

public enum DinningStatus {
    HOLD, //가게 등록 보류
    REJECT, // 가게 등록 거절
    NORMAL, // 일반 가게
    WAIT, // 폐점 신청 대기중
    CLOSED //폐점 처리 완료
}
