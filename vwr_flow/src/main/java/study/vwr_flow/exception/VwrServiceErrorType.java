package study.vwr_flow.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VwrServiceErrorType {
    ALREADY_ADD_VWR(HttpStatus.CONFLICT, "VWR-001", "이미 VWR이 등록되어 있습니다."),
    NOT_FOUND_VWR(HttpStatus.NOT_FOUND, "VWR-002", "VWR이 존재하지 않습니다 : %s"),

    TOKEN_GENERATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "VWR-901", "토큰생성에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    public ServiceException build(Object... args) {
        return new ServiceException(httpStatus, code, message.formatted(args));
    }
}
