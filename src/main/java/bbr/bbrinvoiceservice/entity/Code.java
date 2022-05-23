package bbr.bbrinvoiceservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Code {
    @Id
    private Integer id;

    private Integer eurCode;

    private Integer ronCode;

    private Integer tvaZeroCode;
}
