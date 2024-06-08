package study.vwr_flow.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceException extends RuntimeException {
    private HttpStatus httpStatus;
    private String code;
    private String message;

    public ServiceException(HttpStatus httpStatus, String code, String formatted) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = formatted;
    }
}
