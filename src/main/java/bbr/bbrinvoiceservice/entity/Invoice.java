package bbr.bbrinvoiceservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String invoiceCode;

    private LocalDate creationDate;

    private LocalDate dueDate;

    private Float euro;

    private Float tva;

    private Float priceWithoutTva;

    private Float priceTva;

    private Float priceTotal;

    private Integer providerId;

    private Integer clientId;

    private Boolean isEur;

    private Boolean isStorno;

    private String stornoCode;

}
