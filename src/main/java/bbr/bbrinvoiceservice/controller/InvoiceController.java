package bbr.bbrinvoiceservice.controller;

import bbr.bbrinvoiceservice.entity.Code;
import bbr.bbrinvoiceservice.entity.InvoiceDTO;
import bbr.bbrinvoiceservice.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping(path = "/welcome")
    public String home() {
        return "Welcome to [ Invoice Service ] !";
    }

    @GetMapping(path = "/invoices")
    public HttpEntity findAllInvoices() {
        return ok(invoiceService.findAllInvoices());
    }

    @PostMapping(path = "/invoices")
    public HttpEntity addInvoice(@RequestBody InvoiceDTO invoiceDTO) {
        return ok(invoiceService.addInvoice(invoiceDTO));
    }

    @PutMapping(path = "/invoices")
    public HttpEntity editInvoice(@RequestBody InvoiceDTO invoiceDTO) {
        return ok(invoiceService.editInvoice(invoiceDTO));
    }

    @PostMapping(path = "/generate-pdf/{invoiceId}")
    public HttpEntity generatePdf(@PathVariable Integer invoiceId) {
        return ok(invoiceService.generatePdf(invoiceId));
    }

    @GetMapping(path = "/codes")
    public HttpEntity findAllCodes() {
        return ok(invoiceService.findAllCodes());
    }

    @PutMapping(value = "/codes")
    public HttpEntity updateCodes(@RequestBody Code code) {
        return ok(invoiceService.updateCodes(code));
    }

    @GetMapping(path = "/items/{invoiceId}")
    public HttpEntity findAllItemsByInvoiceId(@PathVariable Integer invoiceId) {
        return ok(invoiceService.findAllItemsByInvoiceId(invoiceId));
    }

    @GetMapping(path = "/storno/{invoiceId}")
    public HttpEntity storno(@PathVariable Integer invoiceId) {
        return ok(invoiceService.storno(invoiceId));
    }

}

