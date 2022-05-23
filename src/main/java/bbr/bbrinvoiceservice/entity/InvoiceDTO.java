package bbr.bbrinvoiceservice.entity;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@ToString
@Data
public class InvoiceDTO {

    private Integer id;

    private Float tva;

    private Boolean isEur;

    private CompanyDTO company;

    private List<ItemDTO> items;
}
