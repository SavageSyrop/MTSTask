package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.example.entities.Smartphone;
import org.example.enums.OutputType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MegafonShopSmartphoneParser {
    private static final char COLUMN_SEPARATOR = ';';
    private static final String URL = "https://moscow.shop.megafon.ru";
    private static final String OUTPUT_FILE_NAME = "smartphones.csv";
    private Integer numberOfPagesToCheck;
    private final OutputType outputType;

    public MegafonShopSmartphoneParser(Integer numberOfPagesToCheck, OutputType outputType) {
        this.numberOfPagesToCheck = numberOfPagesToCheck;
        this.outputType = outputType;
    }

    public void parseSmartphones() throws IOException {
        WebDriver webDriver = new ChromeDriver();   // Устанавливает соединение и получает сформированный html
        // Jsoup не ждет выполнения JavaScript и присылает html, в котором могут отсутствовать данные заполняемые динамически
        // Selenium позволяет получить информацию о персональных акциях со страницы магазина
        File csvOutputFile = new File(OUTPUT_FILE_NAME);
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = csvMapper.schemaFor(Smartphone.class).withHeader().withColumnSeparator(COLUMN_SEPARATOR);
        ObjectMapper jsonMapper = new ObjectMapper();


        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(csvOutputFile, false));

        if (outputType == OutputType.COMBINED || outputType == OutputType.FILE) {
            bufferedWriter.append(csvMapper.writer(schema).writeValueAsString(null));   // шапка таблицы в csv
            schema = schema.withoutHeader();
        }

        webDriver.get(URL + "/mobile");
        Elements pageNumbers = Jsoup.parse(webDriver.getPageSource())
                .select("body > div.g-page > div.g-wrapper > div.container > div.content > div > div:nth-child(4) > div.g-content.g-content_type_leftbar.colsval-5 > div.g_goods.g_goods_mobile.g_goods-list.g_goods-list-mobile > div.b-goods-list__pagination > div > div.b-pagination__body > a.b-pagination__num");
        int lastPageNumber = Integer.parseInt(pageNumbers.get(pageNumbers.size() - 1).text());

        if (lastPageNumber < numberOfPagesToCheck) {
            numberOfPagesToCheck = lastPageNumber;
            System.out.println("ВНИМАНИЕ: Введенное количество страниц превышает количество существующих. Установлено максимальное количество страниц: " + numberOfPagesToCheck);
        }


        for (int i = 1; i <= numberOfPagesToCheck; i++) {
            webDriver.get(URL + "/mobile?page=" + i);
            Document shopPage = Jsoup.parse(webDriver.getPageSource());
            Elements smartphoneCards = shopPage.select("body > div.g-page > div.g-wrapper > div.container > div.content > div > div:nth-child(4) > div.g-content.g-content_type_leftbar.colsval-5 > div.g_goods.g_goods_mobile.g_goods-list.g_goods-list-mobile > div.b-goods-list.b-goods-list_bottom-line > div.b-goods-list__item");

            for (Element element : smartphoneCards.select("div > div > div.b-good__title.title > div > a")) {   // ссылки на страницы смартфонов
                String href = element.attr("href");
                webDriver.get(URL + href);
                Document smartphoneInfo = Jsoup.parse(webDriver.getPageSource());   // html страница смартфона
                try {
                    String[] splittedHref = href.split("/");
                    Smartphone smartphone = new Smartphone(Long.parseLong(splittedHref[splittedHref.length - 1]));
                    String smartphoneName = smartphoneInfo
                            .select("body > div.g-page > div.g-wrapper > div.container > div.content > div > div.c-tabs.tab-effect.tab-effect_before-side.tab-effect_after-side.tab-effect_before-side-hidden.tab-effect_after-side-hidden > ol > li:nth-child(4) > span")
                            .text();
                    Integer price = Integer.parseInt(smartphoneInfo
                            .select("body > div.g-page > div.g-wrapper > div.container > div.content > div > div.g-content.g-content_goodcart > div.g_goods_cards_universalDetails > div > div.b-good-cards__descr-block > div:nth-child(1) > div.b-good-cards__block.b-good-cards__block_price > form > div > span.b-price-cards.b-price.g_priceBlocks-bigPriceNew > span.b-price-cards__actual.b-price__actual > span.b-price-cards__value.b-price__value")
                            .text()
                            .replaceAll(" ", ""));
                    StringBuilder tradeOffers = new StringBuilder();
                    Elements jsFilledOffers = smartphoneInfo.select("body > div.g-page > div.g-wrapper > div.container > div.content > div > div.g-content.g-content_goodcart > div.g_goods_cards_universalDetails > div > div.b-good-cards__photo-block > div.b-good-cards__actions.b-actions.actions.g_clearfix.noRemappingChildUrls > div#stockGoodsTips > div.action-stocks > span");
                    for (Element offerElement : jsFilledOffers) {
                        tradeOffers.append(offerElement.text()).append(",");
                    }
                    if (!tradeOffers.toString().equals("")) {
                        tradeOffers = new StringBuilder(tradeOffers.substring(0, tradeOffers.length() - 1));
                    } else {
                        tradeOffers = null;
                    }

                    double rating;
                    try {
                        rating = Double.parseDouble(smartphoneInfo
                                .select("body > div.g-page > div.g-wrapper > div.container > div.content > div > div.g-content.g-content_goodcart > div.b-info-goods.g_clearfix.infoAndRight > div.b-info-goods__flex-box > div.b-info-goods__information.g_goods_goodInformation > div.b-bookmarks.bookmarks > div.b-bookmarks__item.bookmark.bookmark-rating.active > div > div.b-comments__header-top > div.b-comments__header-top-wrap > span")
                                .text());
                    } catch (NumberFormatException numberFormatException) {
                        rating = 0d;    // у товара нет оценок
                    }


                    smartphone.setName(smartphoneName);
                    smartphone.setPrice(price);
                    smartphone.setTradeOffers(String.valueOf(tradeOffers));
                    smartphone.setRating(rating);


                    switch (outputType) {
                        case FILE -> {
                            String res = csvMapper.writer(schema).writeValueAsString(smartphone);
                            writeInFile(bufferedWriter, res);
                        }
                        case CONSOLE -> System.out.println(jsonMapper.writeValueAsString(smartphone));
                        case COMBINED -> {
                            String res = csvMapper.writer(schema).writeValueAsString(smartphone);
                            writeInFile(bufferedWriter, res);
                            System.out.println(jsonMapper.writeValueAsString(smartphone));
                        }
                    }
                } catch (RuntimeException runtimeException) {
                    System.out.println("ПРОИЗОШЛА ОШИБКА С МОДЕЛЬЮ: " + href);
                }
            }
        }
        webDriver.close();
        bufferedWriter.close();
    }

    private void writeInFile(BufferedWriter bufferedWriter, String data) throws IOException {
        bufferedWriter.append(data);
        bufferedWriter.flush();
    }
}
