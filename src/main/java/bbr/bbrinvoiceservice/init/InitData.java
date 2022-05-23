package bbr.bbrinvoiceservice.init;

import bbr.bbrinvoiceservice.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InitData implements CommandLineRunner {

    @Autowired
    InvoiceService invoiceService;

    @Override
    public void run(String... args) throws Exception {
        invoiceService.init();
    }
}
