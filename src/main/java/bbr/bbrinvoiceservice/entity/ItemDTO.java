package bbr.bbrinvoiceservice.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ItemDTO {
    private Integer id;

    private String name;

    private Float price;

    private Integer quantity;

}
