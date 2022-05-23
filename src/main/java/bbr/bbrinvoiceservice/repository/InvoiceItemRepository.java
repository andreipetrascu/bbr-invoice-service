package bbr.bbrinvoiceservice.repository;

import bbr.bbrinvoiceservice.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Integer> {

    public List<InvoiceItem> findAllByInvoiceId(Integer invoiceId);

    List<InvoiceItem> findAllItemsByInvoiceId(Integer invoiceId);
}
