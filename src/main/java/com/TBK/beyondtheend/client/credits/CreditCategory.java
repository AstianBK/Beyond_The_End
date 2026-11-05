
package com.TBK.beyondtheend.client.credits;

import java.util.List;

public record CreditCategory(
        String category,
        List<String> authors
) {
    public CreditCategory {
        authors = List.copyOf(authors);
    }
}
