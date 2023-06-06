package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.OutputType;
import org.example.service.MegafonShopSmartphoneParser;

import java.io.IOException;
import java.util.Scanner;

@Slf4j
public class Main {


    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите количество страниц для парсинга (на одной странице 24 смартфона): ");
        int numberOfPagesToParse = scanner.nextInt();
        while (numberOfPagesToParse<1) {
            System.out.print("Некорректное число, повторите ввод: ");
            numberOfPagesToParse = scanner.nextInt();
        }
        System.out.println("1. Вывод в файл\n2. Вывод в консоль\n3. Одновременный вывод в консоль и в файл");
        System.out.print("Выберите тип вывода: ");
        int type = scanner.nextInt();
        OutputType outputType = OutputType.get(type); // тип вывода результата
        while (outputType == null) {
            System.out.print("Некорректное число, повторите ввод: ");
            type = scanner.nextInt();
            outputType = OutputType.get(type);
        }
        MegafonShopSmartphoneParser parser = new MegafonShopSmartphoneParser(numberOfPagesToParse, outputType);
        parser.parseSmartphones();
    }
}