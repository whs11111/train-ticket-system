package com.train.trainticketserver;

import com.train.trainticketserver.entity.TicketPrice;
import com.train.trainticketserver.entity.Train;
import com.train.trainticketserver.entity.TrainStation;
import com.train.trainticketserver.mapper.*;
import com.train.trainticketserver.service.impl.TicketPriceServiceImpl;
import com.train.trainticketserver.vo.PriceManageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TicketPriceServiceImplTests {
    private TicketPriceMapper prices;
    private TicketPriceServiceImpl service;

    @BeforeEach
    void setup() {
        TrainMapper trains = mock(TrainMapper.class);
        TrainStationMapper stations = mock(TrainStationMapper.class);
        prices = mock(TicketPriceMapper.class);
        when(trains.lockById(1L)).thenReturn(new Train());
        when(stations.findByTrainId(1L)).thenReturn(List.of(station(10L), station(20L), station(30L)));
        service = new TicketPriceServiceImpl(trains, stations, mock(StationMapper.class), prices);
    }

    @Test
    void rebuildsAllIntervalsForEachSeatWithIntegerFaresAndZero() {
        service.savePrices(List.of(segment(10L, 20L, "10"), segment(20L, 30L, "20")));
        ArgumentCaptor<TicketPrice> saved = ArgumentCaptor.forClass(TicketPrice.class);
        verify(prices, times(9)).insert(saved.capture());
        for (long seat = 1; seat <= 3; seat++) {
            final long seatId = seat;
            TicketPrice through = saved.getAllValues().stream()
                    .filter(p -> p.getStartStationId() == 10L && p.getEndStationId() == 30L
                            && p.getSeatTypeId() == seatId).findFirst().orElseThrow();
            BigDecimal expected = seat == 3 ? BigDecimal.ZERO : new BigDecimal("30");
            assertEquals(0, expected.compareTo(through.getPrice()));
        }
    }

    @Test
    void singleSegmentEditAlsoUpdatesExistingThroughFare() {
        when(prices.findPrice(1L, 20L, 30L, 1L)).thenReturn(new BigDecimal("20"));
        when(prices.findPrice(1L, 20L, 30L, 2L)).thenReturn(new BigDecimal("10"));
        when(prices.findPrice(1L, 20L, 30L, 3L)).thenReturn(BigDecimal.ZERO);
        TicketPrice existing = new TicketPrice();
        existing.setPriceId(99L);
        when(prices.findOne(1L, 10L, 30L, 1L)).thenReturn(existing);

        service.savePrice(segment(10L, 20L, "5"));

        verify(prices).updateById(existing);
        assertEquals(0, new BigDecimal("25").compareTo(existing.getPrice()));
        verify(prices, times(8)).insert(any(TicketPrice.class));
    }

    @Test
    void rejectsMissingSegmentsAndInvalidAmountsBeforeWriting() {
        assertThrows(ResponseStatusException.class,
                () -> service.savePrices(List.of(segment(10L, 20L, "1"))));
        assertThrows(ResponseStatusException.class,
                () -> service.savePrices(List.of(segment(10L, 20L, "1"), segment(20L, 30L, "-1"))));
        assertThrows(ResponseStatusException.class,
                () -> service.savePrices(List.of(segment(10L, 20L, "1"), segment(20L, 30L, "1.001"))));
        assertThrows(ResponseStatusException.class,
                () -> service.savePrices(List.of(segment(10L, 20L, "1"), segment(20L, 30L, "1.5"))));
        assertThrows(ResponseStatusException.class,
                () -> service.savePrices(List.of(segment(10L, 30L, "1"), segment(20L, 30L, "1"))));
        verify(prices, never()).insert(any(TicketPrice.class));
        verify(prices, never()).updateById(any(TicketPrice.class));
    }

    private TrainStation station(long id) {
        TrainStation station = new TrainStation();
        station.setStationId(id);
        return station;
    }

    private PriceManageVO segment(long start, long end, String amount) {
        PriceManageVO price = new PriceManageVO();
        price.setTrainId(1L);
        price.setStartStationId(start);
        price.setEndStationId(end);
        price.setBusinessPrice(new BigDecimal(amount));
        price.setFirstPrice(new BigDecimal(amount));
        price.setSecondPrice(BigDecimal.ZERO);
        return price;
    }
}
