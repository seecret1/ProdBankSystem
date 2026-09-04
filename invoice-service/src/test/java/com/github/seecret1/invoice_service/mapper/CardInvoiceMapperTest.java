package com.github.seecret1.invoice_service.mapper;

import com.github.seecret1.invoice_service.dto.message.BaseMessage;
import com.github.seecret1.invoice_service.dto.order.OrderInvoiceDto;
import com.github.seecret1.invoice_service.dto.request.CardInvoiceCreateRequest;
import com.github.seecret1.invoice_service.dto.response.CardInvoiceResponse;
import com.github.seecret1.invoice_service.entity.CardInvoice;
import com.github.seecret1.invoice_service.entity.enums.CardType;
import com.github.seecret1.invoice_service.entity.enums.InvoiceStatus;
import com.github.seecret1.invoice_service.entity.enums.OrderStatus;
import com.github.seecret1.invoice_service.entity.enums.OrderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CardInvoiceMapper Unit Tests")
class CardInvoiceMapperTest {

    private final CardInvoiceMapper mapper = new CardInvoiceMapper();

    @Test @DisplayName("toResponse: should map entity correctly")
    void shouldMapToResponse() {
        CardInvoice e = CardInvoice.builder().id("id1").cardId("card1").invoiceNumber("INV-1").currency("RUB").status(InvoiceStatus.ACTIVE).balance(new BigDecimal("1000.00")).spendingLimit(new BigDecimal("200000")).freeLimit(new BigDecimal("100000")).deleted(false).createdAt(Instant.now()).updatedAt(Instant.now()).build();
        CardInvoiceResponse r = mapper.toResponse(e);
        assertThat(r.id()).isEqualTo("id1");
        assertThat(r.cardId()).isEqualTo("card1");
        assertThat(r.balance()).isEqualByComparingTo("1000.00");
        assertThat(r.deleted()).isFalse();
    }

    @Test @DisplayName("toResponseList: should map list")
    void shouldMapList() {
        CardInvoice e1 = CardInvoice.builder().id("1").cardId("c1").invoiceNumber("INV1").currency("RUB").status(InvoiceStatus.ACTIVE).balance(BigDecimal.TEN).spendingLimit(BigDecimal.TEN).freeLimit(BigDecimal.TEN).deleted(false).build();
        CardInvoice e2 = CardInvoice.builder().id("2").cardId("c2").invoiceNumber("INV2").currency("USD").status(InvoiceStatus.BLOCKED).balance(BigDecimal.ONE).spendingLimit(BigDecimal.ONE).freeLimit(BigDecimal.ONE).deleted(false).build();
        List<CardInvoiceResponse> list = mapper.toResponseList(List.of(e1, e2));
        assertThat(list).hasSize(2);
        assertThat(list.get(1).status()).isEqualTo(InvoiceStatus.BLOCKED);
    }

    @Test @DisplayName("toEntity: should map request to ACTIVE invoice")
    void shouldMapToEntity() {
        var req = new CardInvoiceCreateRequest("cardX", "user1", "INV-X", "RUB", new BigDecimal("500.00"), new BigDecimal("200000"), new BigDecimal("100000"));
        CardInvoice e = mapper.toEntity(req);
        assertThat(e.getCardId()).isEqualTo("cardX");
        assertThat(e.getStatus()).isEqualTo(InvoiceStatus.ACTIVE);
        assertThat(e.getDeleted()).isFalse();
        assertThat(e.getBalance()).isEqualByComparingTo("500.00");
    }

    @Test @DisplayName("toMessage: should build BaseMessage with invoice data")
    void shouldBuildMessage() {
        OrderInvoiceDto dto = OrderInvoiceDto.builder().traceId("t1").userId("u1").cardId("c1").orderId("o1").cardType(CardType.DEBIT).orderType(OrderType.CARD).build();
        CardInvoiceResponse inv = new CardInvoiceResponse("id1", "c1", "INV1", "RUB", InvoiceStatus.ACTIVE, BigDecimal.TEN, false, Instant.now(), null, null, null, new BigDecimal("200000"), new BigDecimal("100000"));
        BaseMessage msg = mapper.toMessage(dto, inv, OrderStatus.SUCCESS, "ok");
        assertThat(msg.getTraceId()).isEqualTo("t1");
        assertThat(msg.getProductId()).isEqualTo("id1");
        assertThat(msg.getStatus()).isEqualTo(OrderStatus.SUCCESS);
        assertThat(msg.getData()).isEqualTo(inv);
    }

    @Test @DisplayName("toMessageList: should build message with list data")
    void shouldBuildListMessage() {
        OrderInvoiceDto dto = OrderInvoiceDto.builder().traceId("t1").userId("u1").cardId("c1").orderId("o1").cardType(CardType.CREDIT).orderType(OrderType.CARD).build();
        CardInvoiceResponse r1 = new CardInvoiceResponse("id1", "c1", "INV1", "RUB", InvoiceStatus.ACTIVE, BigDecimal.TEN, false, Instant.now(), null, null, null, BigDecimal.TEN, BigDecimal.TEN);
        BaseMessage msg = mapper.toMessageList(dto, List.of(r1), OrderStatus.SUCCESS, "found");
        assertThat(msg.getData()).isEqualTo(List.of(r1));
        assertThat(msg.getProductId()).isEqualTo("c1");
    }
}
