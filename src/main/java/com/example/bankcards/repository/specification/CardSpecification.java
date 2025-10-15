package com.example.bankcards.repository.specification;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CardSpecification {
    public static Specification<Card> hasOwnerId(Long userId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("owner").get("id"), userId);
    }

    public static Specification<Card> hasStatus(CardStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Card> panLast4Contains(String panLast4) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("panLast4"), "%" + panLast4 + "%");
    }


    public static Specification<Card> filterBy(Long userId, CardStatus status, String panLast4) {
        List<Specification<Card>> specs = new ArrayList<>();
        specs.add(hasOwnerId(userId));

        if (status != null) {
            specs.add(hasStatus(status));
        }
        if (panLast4 != null && !panLast4.isBlank()) {
            specs.add(panLast4Contains(panLast4));
        }
        return Specification.allOf(specs);
    }

}