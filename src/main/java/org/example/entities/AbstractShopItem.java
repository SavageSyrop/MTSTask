package org.example.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public abstract class AbstractShopItem implements Serializable {
    @NonNull
    private final Long id;
}
