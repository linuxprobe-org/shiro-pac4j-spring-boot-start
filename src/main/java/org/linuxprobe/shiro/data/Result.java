package org.linuxprobe.shiro.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.linuxprobe.luava.json.JacksonUtils;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class Result<T> {
    public Result(T data) {
        this.data = data;
    }

    private int code = 0;
    private String message = "success";
    private T data;

    public static Result<Void> fail(String message) {
        return Result.fail(500, message);
    }

    public static Result<Void> fail(int code, String message) {
        if (code == 0) {
            throw new IllegalArgumentException("code can not be 0");
        }
        Result<Void> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <Ts> Result<Ts> success(Ts data) {
        return new Result<>(data);
    }

    public static Result<Void> success() {
        return new Result<>(null);
    }

    public boolean resultIsError() {
        return this.code != 0;
    }

    /**
     * 把数据转换为指定类型
     */
    public <OT> OT dataConversion(Class<OT> type) {
        return JacksonUtils.conversion(this.data, type);
    }

    @Override
    public String toString() {
        return JacksonUtils.toJsonString(this);
    }
}
