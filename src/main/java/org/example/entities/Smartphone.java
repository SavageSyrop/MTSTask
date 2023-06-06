package org.example.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Smartphone extends AbstractShopItem {
    private String name;
    private Integer price;
    private Double rating;
    private String tradeOffers;

    public Smartphone(Long id) {
        super(id);
    }
}
