package bbr.bbrinvoiceservice.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Response<T> {

    /**
     * 1 good, 0 bad
     */
    Integer status;

    String msg;

    T data;

}
