package com.example.hostdevtools.cardsimulator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

/**
 * "카드사 시뮬레이터" 화면의 상태 관리 로직.
 *
 * 확인 필요: 실제 대상 카드사 목록과 초기 상태, 그리고 실행/중지/재시작 시 실제로 무엇을
 * 호출해야 하는지(재기동 쿼리/명령 실행 위치)가 아직 정의되지 않았습니다. 지금은 메모리에
 * Mock 상태만 두고 버튼으로 상태만 토글하는 골격 단계이며, 실제 프로세스/장비 연동은
 * 이후 요구사항이 확정되면 changeStatus 부분에 이어서 구현하면 됩니다.
 */
@Service
public class CardSimulatorService {

    private final Map<String, CardSimulatorState> simulators = new LinkedHashMap<>();

    public CardSimulatorService() {
        seed("shinhan", "신한카드", CardSimulatorStatus.RUNNING);
        seed("samsung", "삼성카드", CardSimulatorStatus.STOPPED);
        seed("kookmin", "국민카드", CardSimulatorStatus.RUNNING);
        seed("hyundai", "현대카드", CardSimulatorStatus.STOPPED);
        seed("lotte", "롯데카드", CardSimulatorStatus.STOPPED);
        seed("hana", "하나카드", CardSimulatorStatus.RUNNING);
    }

    private void seed(String id, String name, CardSimulatorStatus status) {
        simulators.put(id, new CardSimulatorState(id, name, status));
    }

    public synchronized List<CardSimulatorDto> list() {
        return simulators.values().stream().map(this::toDto).toList();
    }

    public synchronized CardSimulatorDto start(String id) {
        return changeStatus(id, CardSimulatorStatus.RUNNING);
    }

    public synchronized CardSimulatorDto stop(String id) {
        return changeStatus(id, CardSimulatorStatus.STOPPED);
    }

    public synchronized CardSimulatorDto restart(String id) {
        // 확인 필요: 실제 재기동 쿼리/명령이 정의되면 여기서 호출. 지금은 RUNNING으로 상태만 되돌리는 Mock 동작.
        return changeStatus(id, CardSimulatorStatus.RUNNING);
    }

    private CardSimulatorDto changeStatus(String id, CardSimulatorStatus status) {
        CardSimulatorState state = simulators.get(id);
        if (state == null) {
            throw new NoSuchElementException("알 수 없는 카드사 ID: " + id);
        }
        state.status = status;
        state.lastActionAt = java.time.Instant.now();
        return toDto(state);
    }

    private CardSimulatorDto toDto(CardSimulatorState state) {
        return new CardSimulatorDto(state.id, state.name, state.status.name(), state.lastActionAt.toString());
    }
}
