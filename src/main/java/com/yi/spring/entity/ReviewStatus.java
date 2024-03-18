package com.yi.spring.entity;

public enum ReviewStatus {
    NORMAL("일반"),
    INSULT("욕설 및 비방"),
    FALSE_EXAGGERATION("허위 및 과장"),
    WRONG_IMAGE("잘못된 사진"),
    ADVERTISEMENT("광고"),
    OTHER("기타");

    private final String reason;

    ReviewStatus(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

}
