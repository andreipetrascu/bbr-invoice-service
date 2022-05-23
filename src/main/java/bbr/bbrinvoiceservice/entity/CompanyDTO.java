package bbr.bbrinvoiceservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CompanyDTO {
    private Integer id;
    private Integer paymentDays;
}
