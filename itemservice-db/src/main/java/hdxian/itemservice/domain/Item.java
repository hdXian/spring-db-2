package hdxian.itemservice.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "item") // able to omit (if same)
public class Item {

    // @Id means PK. // @GeneratedValue: Key generation strategy is Identity. (DB auto generate)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mapping property name to DB table Column. it's able to omit if both is same.
    @Column(name = "item_name", length = 10) // length can be used in DDL (create table ...)
    private String itemName;
    private Integer price;
    private Integer quantity;

    // public or protected basic constructor needed. (JPA spec)
    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
