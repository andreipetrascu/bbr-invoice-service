package bbr.bbrinvoiceservice.service;

import bbr.bbrinvoiceservice.entity.*;
import bbr.bbrinvoiceservice.repository.CodeRepository;
import bbr.bbrinvoiceservice.repository.InvoiceItemRepository;
import bbr.bbrinvoiceservice.repository.InvoiceRepository;
import bbr.bbrinvoiceservice.util.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService {

    private static final String RON_ID = "3";
    private static final String EUR_AND_ZERO_TVA_ID = "2";
    private static final String EUR_ID = "0";

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CodeRepository codeRepository;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceRepository.class);

    /**
     * 'Code' table should have only one row
     * If codes are not set, then insert init data
     */
    public void init() {
        LOGGER.info("[InvoiceService] init");
        List<Code> codeList = codeRepository.findAll();
        if (codeList.isEmpty()) {
            LOGGER.info("[invoiceService init] findAllCodes is empty. Inserting dummy data into 'code' table.");
            codeRepository.save(new Code(1, 1, 1, 1));
        }
    }

    public Response findAllInvoices() {
        List<Invoice> invoices = invoiceRepository.findAll();
        if (invoices.isEmpty()) {
            LOGGER.info("Find all invoices: {}", "No Content");
            return new Response<>(1, "No content", invoices);
        }
        return new Response<>(1, "[findAllInvoices] Success", invoices);
    }

    public Response findAllItemsByInvoiceId(Integer invoiceId) {
        List<InvoiceItem> invoiceItemList = invoiceItemRepository.findAllItemsByInvoiceId(invoiceId);
        if (invoiceItemList.isEmpty()) {
            LOGGER.info("Find all items by invoice id. No Content");
            return new Response<>(1, "No content", invoiceItemList);
        }
        return new Response<>(1, "[findAllItemsByInvoiceId] Success", invoiceItemList);
    }

    public Response storno(Integer invoiceId) {
        Optional<Invoice> invoiceOptional = invoiceRepository.findById(invoiceId);
        if (invoiceOptional.isEmpty()) {
            return new Response<>(1, "[storno] no invoice found", invoiceOptional);
        }
        Invoice oldInvoice = invoiceOptional.get();
        List<InvoiceItem> invoiceItemList = invoiceItemRepository.findAllItemsByInvoiceId(invoiceId);

        // used to generate invoice code for the new invoice
        InvoiceDTO invoiceDTO = new InvoiceDTO();
        invoiceDTO.setTva(oldInvoice.getTva());
        invoiceDTO.setIsEur(oldInvoice.getIsEur());

        List<ItemDTO> itemDTOList = new ArrayList<>();
        for (InvoiceItem item : invoiceItemList
        ) {
            ItemDTO itemDTO = new ItemDTO();
            itemDTO.setName(item.getName());
            itemDTO.setPrice((-1) * item.getPrice());
            itemDTO.setQuantity(item.getQuantity());
            itemDTOList.add(itemDTO);
        }
        invoiceDTO.setItems(itemDTOList);

        Invoice newInvoice = new Invoice();
        newInvoice.setInvoiceCode(generateInvoiceCode(invoiceDTO));
        newInvoice.setCreationDate(oldInvoice.getCreationDate());
        newInvoice.setDueDate(oldInvoice.getDueDate());
        newInvoice.setEuro(oldInvoice.getEuro());
        newInvoice.setTva(oldInvoice.getTva());
        newInvoice.setPriceWithoutTva((-1) * oldInvoice.getPriceWithoutTva());
        newInvoice.setPriceTva((-1) * oldInvoice.getPriceTva());
        newInvoice.setPriceTotal((-1) * oldInvoice.getPriceTotal());
        newInvoice.setProviderId(oldInvoice.getProviderId());
        newInvoice.setClientId(oldInvoice.getClientId());
        newInvoice.setIsEur(oldInvoice.getIsEur());
        newInvoice.setIsStorno(true);
        newInvoice.setStornoCode(oldInvoice.getInvoiceCode());

        Invoice savedInvoice = invoiceRepository.save(newInvoice);
        saveInvoiceItems(invoiceDTO, savedInvoice.getId());

        return new Response<>(1, "[storno] Success", savedInvoice);
    }

    public Response findAllCodes() {
        List<Code> codeList = codeRepository.findAll();
        if (codeList.isEmpty()) {
            LOGGER.info("[findAllCodes] No Content");
            return new Response<>(1, "[findAllCodes] No Content", codeList);
        }
        return new Response<>(1, "[findAllCodes] Success", codeList);
    }

    public Response updateCodes(Code code) {
        Optional<Code> foundCode = codeRepository.findById(code.getId());
        if (!foundCode.isPresent()) {
            LOGGER.info("[Update Codes] Code Id not found");
            return new Response<>(0, "[updateCodes] Code Id " + code.getId() + " not found.", null);
        } else {
            Code updateCode = foundCode.get();
            updateCode.setRonCode(code.getRonCode());
            updateCode.setEurCode(code.getEurCode());
            updateCode.setTvaZeroCode(code.getTvaZeroCode());
            codeRepository.save(updateCode);
            return new Response<>(1, "[updateCodes] Success", updateCode);
        }
    }

    public Response generatePdf(Integer invoiceId) {
        List<InvoiceItem> invoiceItemList = invoiceItemRepository.findAllByInvoiceId(invoiceId);
        return new Response<>(1, "[generatePdf] Success", invoiceItemList);
    }

    public Response addInvoice(InvoiceDTO invoiceDTO) {
        Float latestExchangeRate = getLatestExchangeRate();
        if (latestExchangeRate == -1) {
            return new Response<>(0, "[addInvoice] cannot get latest exchange rate.", null);
        }
        CompanyDTO company = invoiceDTO.getCompany();
        LOGGER.info(">> company:" + company);

        Invoice invoice = new Invoice();
        invoice.setInvoiceCode(generateInvoiceCode(invoiceDTO));
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Vienna"));
        invoice.setCreationDate(today);
        invoice.setDueDate(today.plusDays(company.getPaymentDays()));
        invoice.setEuro(latestExchangeRate);
        invoice.setTva(invoiceDTO.getTva());
        Float priceWithoutTva = computePriceWithoutTva(invoiceDTO);
        Float priceTva = (invoice.getTva() / 100) * priceWithoutTva;
        invoice.setPriceWithoutTva(priceWithoutTva);
        invoice.setPriceTva(priceTva);
        invoice.setPriceTotal(priceWithoutTva + priceTva);
        invoice.setProviderId(1);
        invoice.setClientId(company.getId());
        invoice.setIsEur(invoiceDTO.getIsEur());
        invoice.setIsStorno(false);

        Invoice savedInvoice = invoiceRepository.save(invoice);
        saveInvoiceItems(invoiceDTO, savedInvoice.getId());

        return new Response<>(1, "[addInvoice] Success", savedInvoice);
    }

    public Response editInvoice(InvoiceDTO invoiceDTO) {

        Optional<Invoice> foundInvoice = invoiceRepository.findById(invoiceDTO.getId());
        if (!foundInvoice.isPresent()) {
            LOGGER.info("[editInvoice] Invoice Id Is Non-Existent");
            return new Response<>(0, "[editInvoice] Invoice Id " + invoiceDTO.getId() + " not found.", null);
        } else {

            // update all items
            for (ItemDTO item : invoiceDTO.getItems()
            ) {
                Optional<InvoiceItem> foundItem = invoiceItemRepository.findById(item.getId());
                if (!foundItem.isPresent()) {
                    return new Response<>(0, "[editInvoice] Item Id " + item.getId() + " not found.", null);
                } else {
                    InvoiceItem editItem = foundItem.get();
                    editItem.setName(item.getName());
                    editItem.setPrice(item.getPrice());
                    editItem.setQuantity(item.getQuantity());
                    invoiceItemRepository.save(editItem);
                }
            }

            Invoice editInvoice = foundInvoice.get();
            editInvoice.setTva(invoiceDTO.getTva());

            List<InvoiceItem> invoiceItemList = invoiceItemRepository.findAllItemsByInvoiceId(invoiceDTO.getId());
            float price = 0;
            for (InvoiceItem item : invoiceItemList) {
                price += item.getPrice() * item.getQuantity();
            }

            Float priceWithoutTva = price;
            Float priceTva = (invoiceDTO.getTva() / 100) * priceWithoutTva;
            editInvoice.setPriceWithoutTva(priceWithoutTva);
            editInvoice.setPriceTva(priceTva);
            editInvoice.setPriceTotal(priceWithoutTva + priceTva);

            editInvoice.setClientId(invoiceDTO.getCompany().getId());
            editInvoice.setIsEur(invoiceDTO.getIsEur());

            invoiceRepository.save(editInvoice);

            LOGGER.info("[Update Invoice] Success.");
            return new Response<>(1, "[editInvoice] Success", editInvoice);
        }
    }

    private void saveInvoiceItems(InvoiceDTO invoiceDTO, Integer invoiceId) {
        for (ItemDTO item : invoiceDTO.getItems()) {
            InvoiceItem invoiceItem = InvoiceItemMapper.toEntity(item, invoiceId);
            invoiceItemRepository.save(invoiceItem);
        }
    }

    private Float computePriceWithoutTva(InvoiceDTO invoiceDTO) {
        float price = 0;
        for (ItemDTO item : invoiceDTO.getItems()) {
            price += item.getPrice() * item.getQuantity();
        }
        return price;
    }

    private String generateInvoiceCode(InvoiceDTO invoiceDTO) {
        String code = "BBR";

        LocalDate today = LocalDate.now(ZoneId.of("Europe/Vienna"));
        Integer lastTwoCharOfCurrentYear = today.getYear() % 100;
        code += lastTwoCharOfCurrentYear;

        Optional<Code> foundCode = codeRepository.findById(1);
        Code repositoryCode = foundCode.orElse(null);
        Integer codeNumber;

        if (invoiceDTO.getTva() == 0.0) {
            codeNumber = repositoryCode.getTvaZeroCode();
            code += EUR_AND_ZERO_TVA_ID;

            if (codeNumber < 10) {
                code += "00";
            } else if (codeNumber < 100) {
                code += "0";
            }

            code += codeNumber;
            repositoryCode.setTvaZeroCode(codeNumber + 1);
            codeRepository.save(repositoryCode);

        } else if (invoiceDTO.getIsEur()) {
            codeNumber = repositoryCode.getEurCode();
            code += EUR_ID;

            if (codeNumber < 10) {
                code += "00";
            } else if (codeNumber < 100) {
                code += "0";
            }

            code += codeNumber;
            repositoryCode.setEurCode(codeNumber + 1);
            codeRepository.save(repositoryCode);

        } else {
            codeNumber = repositoryCode.getRonCode();
            code += RON_ID;
            if (codeNumber < 10) {
                code += "00";
            } else if (codeNumber < 100) {
                code += "0";
            }

            code += codeNumber;
            repositoryCode.setRonCode(codeNumber + 1);
            codeRepository.save(repositoryCode);
        }

        return code;
    }

    private Float getLatestExchangeRate() {
        Document document = null;
        try {
            document = Jsoup.connect("https://www.cursbnr.ro/insert/cursvalutar.php").get();
        } catch (IOException e) {
            e.printStackTrace();
            return (float) -1;
        }
        Element result = document.select("table tr td table tr td table tr").get(2).select("td").first();
        String tdText = result.text();
        String strEurValue = tdText.split("\\s+", 0)[3];
        Float floatEurValue = Float.valueOf(strEurValue);
        return floatEurValue;
    }

}
