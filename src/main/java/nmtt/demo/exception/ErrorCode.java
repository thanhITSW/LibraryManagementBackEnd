package nmtt.demo.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXEPTION(9999, "Uncategorized error"),
    USER_EXISTED(1002, "User existed"),
    INVALID_KEY(1001, "Uncategorized error"),
    PASSWORD_INVALID(1003, "Password must be at least 6 characters")
    ;
    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
