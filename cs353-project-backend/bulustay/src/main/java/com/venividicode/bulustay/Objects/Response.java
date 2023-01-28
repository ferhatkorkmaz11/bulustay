package com.venividicode.bulustay.Objects;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Response {
    private String code;
    private String message;
    private Object body;
}
