## 기본 과제

### 문제 1:  UserPoint 도메인 객체에서 단위 테스트를 진행한 것에 대해 Service에서 다시 수행할 필요가 있을까?

- 도메인 객체에서 수행한 단위 테스트를 다시 한번 서비스 단위 테스트를 진행하면서 뭔가 같은 것을 반복한다는 느낌 들었습니다.
- 서비스를 자세히보면 Repository 메서드들의 호출 결과를 확인해야 하거나 도메인 로직만 호출하는 것이 전부입니다.
- 이런 고민을 가지고 있었으나 멘토링 이후 PointService는 통합테스트를 해야하는 것으로 결론 짓고 통합 테스트를 진행했습니다.

### 문제 2: Controller 단위 테스트는 유의미한가?

- Controller의 단위 테스트는 단순히 입력값을 검증하는 것에 국한되는게 맞을까?
- PointController의 입력값 검증은 전부 스프링의 Bean Validation 어노테이션으로 진행했습니다.
- 그런데 이 애노테이션들은 이미 라이브러리 단에서 테스트가 전부 이루어졌을텐데 굳이 내가 또 테스트 해야하나?
- 그러면 Controller는 단위 테스트 대상이 맞는가? 라는 고민이 들었고 멘토링 이후 E2E Test로 진행하라는 조언을 얻었습니다.
- 성공, 실패 케이스의 입력값과 그 응답에 대응하는 HTTP 호출 및 응답결과를 검증했습니다.

### 사소한 문제

- 통합 테스트 실행 시 매번 데이터를 클린징 해줘야하지만 과제 요구사항에선 database 패키지 수정을 하지 말란 요구사항이 있었습니다.
- 그래서 delete 메서드를 따로 추가하지 않았습니다.
- 임시방편으로 `@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)`을 사용했습니다.
- 하지만 매 번 컨텍스트를 초기화해서 시간을 많이 소요하는 문제가 있었습니다.

---

## 심화 과제

### 문제:  동시 충전, 사용 요청을 모두 반영할 수 있게 하기

- 동시성 문제를 해결하기 위해서는 임계 영역과 공유 자원을 파악하는 것이 먼저입니다.
    - 공유 자원이란 말 그대로 여러 스레드가 공유하는 자원을 뜻하고 우리의 과제에서는 UserPoint의 point 필드입니다.
    - 임계 영역이란 여러 스레드가 동시에 접근할 경우 데이터 불일치나 예상치 못한 동작이 발생할 수 있는 영역을 말합니다.
    - 이 과제에서는 포인트를 충전하거나 차감하는 서비스 계층의 메서드에서 발생할 수 있습니다.
- Java에서 동시성 문제를 해결할 수 있는 방법은 synchronized와 JDK 1.5에서 공개된 concurrent 패키지의 Lock을 활용하는 것입니다.

### 해결책1 : Synchronized

- 첫 번째 해결 방법은 synchronized입니다. 하지만 이 과제의 의도가 단순히 synchronized를 사용하는 것은 아닐거라고 생각했습니다.
    - 왜냐하면 synchronized는 무한 대기와 공정하지 않다는 문제가 있습니다.
    - 무한 대기는 스레드가 인터럽트 되지 않고 계속해서 락을 기다리는 문제를 말합니다.
    - 공정하지 않은 문제는 synchronized가 순서를 보장하지 않는다는 문제를 말합니다.
- 실제로 synchronized를 적용한 charge, use 메서드를 만들었으나 50개의 동시요청 처리가 엄청나게 느렸습니다.
    - 게다가 userId 별로 락이 관리가 되지 않으니 n명의 유저에게 m번의 동시요청이 들어오면 이 처리는 더욱 느릴 것입니다.

### 해결책2: ReenterantLock + ConcurrentHashMap

- ReenterantLock + ConcurrentHashMap을 같이 쓰면 이 문제를 더 높은 처리량으로 해결할 수 있습니다.
- 처음엔 단순히 단일 ReenterantLock을 사용해서 문제를 해결하려고 하였으나 이렇게 되면 synchronized하고 별반 다를게 없다는 생각이 들었습니다.
- 그래서 ConcurrentHashMap에 userId를 키로 하고 ReenterantLock을 값으로 하는 구조로 만들었습니다.
- 이렇게 해서 userId 별로 Lock을 걸어서 서로 다른 사용자에 대한 요청은 서로 영향을 주지 않게 만들었습니다.

### 미처 해결하지 못한 것.

- 2명의 사용자에게 각각 20번씩 요청을 걸어서 40번의 요청을 처리한 뒤 이력의 개수를 검증하는 곳에서 이력이 20개가아닌 19개가 나오는 문제가 간헐적으로 있었는데 해결방법을 찾진 못했습니다.

---

## 피드백

- **Good**
    - 과제에 대한 고민과 해답을 기록해놓은 점이 좋았다.
    - UserPoint 도메인 모델링과 그 단위테스트가 적절했다.
- **Bad**
    - PointRepository에서 History도 제공해도 되지 않을까? 계층에 대한 경계면 추상화를 한 것 같은데 DataSource 마다 따로 두는 것도 어색하다.
    - 값에 대한 검증은 비즈니스에서와 HTTP 요청에서의 같은 값이어도 목적이 달라서 검증할 필요가 있다고 생각해보면 좋겠다.
    - 보고서/기술문서에는 각 해결책들에 대한 코드블록이나 사례, 벤치마크 등을 추가하여 문서의 신뢰성을 높여라
    - LockProvider보다는 LockTemplate라는 이름이 더 적절하고, 이에대한 통합테스트가 없다는 점은 아쉽다.
    - 스레드카운트는 실제 Usecase에 근접하게 작성하면 좋을 것 같다.
    - 동시에 충전하면서 사용하는 것은 왜 하지 않았지?
    - 히스토리가 누락되는 것은 포인트 히스토리 내부의 cursor 값이 thread-safe하지 않아서 동시에 ++를 할 때 이슈가 발생하는 것

