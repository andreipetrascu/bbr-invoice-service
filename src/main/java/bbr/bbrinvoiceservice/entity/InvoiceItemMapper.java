package bbr.bbrinvoiceservice.entity;

public class InvoiceItemMapper {

    public static InvoiceItem toEntity(ItemDTO itemDTO, Integer invoiceId) {
        InvoiceItem invoiceItem = new InvoiceItem();
        invoiceItem.setInvoiceId(invoiceId);
        invoiceItem.setName(itemDTO.getName());
        invoiceItem.setPrice(itemDTO.getPrice());
        invoiceItem.setQuantity(itemDTO.getQuantity());
        return invoiceItem;
    }

}
