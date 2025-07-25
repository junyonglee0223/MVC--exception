# MVC--exception




---
# 250121
## [MVC2 9-2] API 예외 처리 (2)

---

### TypeMismatchException 핸들링 예제

Spring 애플리케이션에서 `TypeMismatchException`을 처리하는 방법에 대해 확인합니다. 
설정된 코드에서는 타입 불일치(type mismatch)가 발생할 경우 서버가 `400 Bad Request` 응답을 반환하는지 확인하는 과정입니다.

---

### 1. **핸들러 확인**

`handleTypeMismatch` 메서드는 `TypeMismatchException`이 발생했을 때 
HTTP 응답 코드를 설정하는 역할을 합니다. 해당 코드는 `DefaultHandlerExceptionResolver.class`에 있습니다.

```java
protected ModelAndView handleTypeMismatch(TypeMismatchException ex, HttpServletRequest request, HttpServletResponse response, @Nullable Object handler) throws IOException {
    response.sendError(400); // 400 Bad Request 응답 반환
    return new ModelAndView();
}
```

#### 주요 기능:
1. 타입 불일치 예외 발생 시 클라이언트에게 `400` 상태 코드 전송.
2. 추가적인 응답 본문은 없으며, 빈 `ModelAndView`를 반환.

---

### 2. **APIExceptionController 추가**

#### **구현 코드**
```java
@RestController
@RequestMapping("/api")
public class APIExceptionController {

    @GetMapping("/default-handler-ex")
    public String defaultException(@RequestParam Integer data) {
        return "ok";
    }
    /*코드 생략*/
}
```

#### **설명**
- **URL**: `/api/default-handler-ex`
- **메서드 이름**: `defaultException`
- **파라미터**: `@RequestParam Integer data`
    - URL 파라미터로 `Integer` 타입 데이터를 받음.
    - 잘못된 데이터 타입이 전달되면 `TypeMismatchException`이 발생.
- **반환값**: `"ok"`

---

### 3. **Postman 테스트**

Postman을 사용하여 API의 동작을 확인할 수 있습니다.

#### **테스트 시나리오**
1. 정상적인 요청:
    - **URL**: `./api/default-handler-ex?data=10`
    - **결과**: 응답 본문 `"ok"`와 상태 코드 `200 OK`.

2. 잘못된 요청(타입 불일치):
    - **URL**: `./api/default-handler-ex?data=qqq`
    - **결과**: 상태 코드 `400 Bad Request`.

---
### API 예외 처리 - ExceptionHandler

#### 1. 프로젝트 개요
`ExceptionHandler` annotation을 활용해서 다양한 예외 핸들링 기능 입니다.
`ApiExceptionV2Controller` 클래스는 각 예외 유형에 맞는 
커스텀 에러 메시지를 반환하며, 클라이언트는 적절한 상태 코드와 함께 
명확한 에러 정보를 받을 수 있습니다.

---

#### 2. 예외 처리 방식

- **`@ExceptionHandler`를 사용한 예외 처리**  
  컨트롤러 내부에서 발생할 수 있는 특정 예외를 핸들링하기 위해 `@ExceptionHandler` 어노테이션을 사용합니다.

##### 주요 예외 처리
1. **`IllegalArgumentException` 처리**
    - 상태 코드: 400 (Bad Request)
    - 반환 데이터:
      ```json
      {
          "code": "BAD",
          "message": "에러 메시지"
      }
      ```
    - 로그 기록: 예외 발생 시 로그를 남김.

2. **`UserException` 처리**
    - 상태 코드: 400 (Bad Request)
    - 반환 데이터:
      ```json
      {
          "code": "USER-EX",
          "message": "유저 관련 에러 메시지"
      }
      ```
    - `ResponseEntity`를 사용해 상태 코드와 데이터 반환.

3. **기타 `Exception` 처리**
    - 상태 코드: 500 (Internal Server Error)
    - 반환 데이터:
      ```json
      {
          "code": "EX",
          "message": "에러 메시지"
      }
      ```
    - 서버에서 처리할 수 없는 일반적인 에러를 처리.

---

#### 3. API 사용

##### **요청**
```http
GET /api2/members/{id}
```

##### **응답**

| `id` 값    | 설명                              | 상태 코드 | 반환 데이터 예시                                          |
|------------|-----------------------------------|-----------|----------------------------------------------------------|
| `ex`       | 런타임 예외 발생                 | 500       | `{ "code": "EX", "message": "wrong user" }`              |
| `bad`      | 잘못된 입력 값으로 인한 예외 발생 | 400       | `{ "code": "BAD", "message": "wrong input value" }`      |
| `user-ex`  | 사용자 정의 예외 발생             | 400       | `{ "code": "USER-EX", "message": "user exception" }`     |
| 기타       | 정상 처리                         | 200       | `{ "memberId": "id값", "name": "hello id값" }`           |

---

#### 4. 주요 클래스 및 코드 설명

1. **`ErrorResult` 클래스**
    - 에러 응답을 정의하는 DTO로, `code`와 `message` 필드를 포함.
    - 예외 유형에 따라 적절한 값을 설정해 클라이언트로 반환.


2. **`ApiExceptionV2Controller` 클래스**
    - 예외 처리 및 API 응답을 관리하는 컨트롤러.
    - 요청 매핑: `/api2/members/{id}`



3. **`MemberDto` 클래스**
    - API 응답을 정의하는 DTO로, `memberId`와 `name` 필드를 포함.


---

#### 5. 테스트 데이터

| 테스트 케이스 | URL                        | 기대 결과                                                                                 |
|---------------|----------------------------|------------------------------------------------------------------------------------------|
| 정상 요청      | `/api2/members/test`       | `{ "memberId": "test", "name": "hello test" }`                                           |
| 잘못된 입력    | `/api2/members/bad`        | `{ "code": "BAD", "message": "wrong input value" }`                                      |
| 사용자 정의 예외 | `/api2/members/user-ex`   | `{ "code": "USER-EX", "message": "user exception" }`                                     |
| 런타임 예외    | `/api2/members/ex`         | `{ "code": "EX", "message": "wrong user" }`                                              |


---
### 전역 예외 처리

#### 1. 개요
예외 처리 로직을 컨트롤러에서 분리하여 전역적으로 관리하기 위해 `@RestControllerAdvice` 어노테이션을 활용합니다.  
전역 예외 처리 클래스인 `ExControllerAdvice`는 프로젝트의 모든 컨트롤러에서 발생하는 예외를 통합적으로 처리하며, 예외 유형에 따라 적절한 응답을 클라이언트로 반환합니다.

---

#### 2. `@RestControllerAdvice`의 역할
- **전역 예외 처리**: 모든 컨트롤러에서 발생하는 예외를 한 곳에서 관리.
- **코드 재사용성 향상**: 컨트롤러 내부에서 중복되는 예외 처리 코드를 제거.
- **유지보수성 증가**: 예외 처리 로직이 분리되어 코드 가독성과 유지보수성이 향상됨.

---

#### 3. ExControllerAdvice의 장점

1. **코드 분리 및 재사용성**
    - 예외 처리 로직이 컨트롤러 로직과 분리되어, 컨트롤러는 비즈니스 로직에만 집중할 수 있음.

2. **전역 적용**
    - `@RestControllerAdvice`는 모든 컨트롤러에 예외 처리를 일관되게 적용하므로, 중복 코드 작성 방지.

3. **유지보수성 향상**
    - 예외 처리 정책 변경 시 단일 클래스를 수정하면 되므로 유지보수가 용이함.

4. **로깅 통합**
    - 예외 발생 시 로그를 남겨 디버깅 및 문제 추적이 용이.



---
# 250120
## [MVC2 9-1] API 예외 처리 (1)

---
## Exception Handling with Custom `UserException`


사용자 정의 예외(`UserException`)를 처리하기 위해 다음과 같은 예외 처리 흐름을 구현하였습니다:

1. 특정 조건에서 `UserException` 발생.
2. `UserHandlerExceptionResolver`에서 예외 처리:
    - `application/json` 요청 헤더인 경우 JSON 형식의 에러 메시지 반환.
    - 그 외의 경우 500 에러 페이지로 연결.
3. `WebConfig`에서 예외 처리 리졸버 등록.

---

### 주요 구현 내용

### 1. **`ApiExceptionController`**

사용자 정의 예외 `UserException`을 발생시키는 컨트롤러입니다.

---

### 2. **`UserException`**

`RuntimeException`을 확장하여 구현된 예외 클래스입니다. 예외 발생 시 메시지를 받아 처리할 수 있습니다.

---

### 3. **`UserHandlerExceptionResolver`**

Spring MVC의 `HandlerExceptionResolver`를 구현하여 사용자 정의 예외를 처리합니다.

- 요청 헤더의 `Accept` 값이 `application/json`일 경우:
    - 예외 정보를 JSON 형식으로 반환.
- 그 외의 경우:
    - 500 에러 페이지로 연결.


---

### 4. **`WebConfig`**

`HandlerExceptionResolver`를 등록하고, 기존 `MyHandlerExceptionResolver`를 주석 처리합니다.


---

### 예외 처리 흐름

1. 특정 조건에서 `UserException` 발생:
    - 예시: `/api/test?id=user-ex` 요청.
2. `UserHandlerExceptionResolver`가 예외 처리:
    - 요청 헤더가 `application/json`인 경우:
      ```json
      {
        "ex": "class hello.exception.exception.UserException",
        "message": "user error occurs!!"
      }
      ```
    - 그 외의 경우:
        - `error/500` 페이지로 리다이렉트.
3. `WebConfig`에서 `UserHandlerExceptionResolver`를 등록하여 동작.

---

### 테스트 및 결과 확인

### 테스트 1: `application/json` 요청
- **요청**:
  ```http
  GET /api/test?id=user-ex
  Accept: application/json
  ```
- **응답**:
  ```json
  {
    "ex": "class hello.exception.exception.UserException",
    "message": "user error occurs!!"
  }
  ```

### 테스트 2: HTML 요청
- **요청**:
  ```http
  GET /api/test?id=user-ex
  Accept: text/html
  ```
- **결과**:
    - 브라우저에서 `error/500` 페이지로 이동.

---

### 주의사항

1. 기존 `MyHandlerExceptionResolver`는 주석 처리해야 정상 동작합니다.
2. 예외 처리 우선순위는 `HandlerExceptionResolver`에 등록된 순서에 따라 결정됩니다.

---
### API 예외 처리 (ResponseStatus 및 ResponseStatusException 사용)

Spring Boot에서 API 예외 처리를 위해 `@ResponseStatus`와 `ResponseStatusException`을 활용한 방식을 실험하였습니다. 각각의 방식에 따른 결과를 아래와 같이 정리하였습니다.

---

### ResponseStatus를 사용한 400 에러 처리

`@ResponseStatus` 애너테이션을 사용하여 HTTP 상태 코드를 설정하고, 특정 예외 발생 시 이를 클라이언트에 전달할 수 있습니다.

#### **코드**
```properties
#message.properties
error.bad = wrong request! use message properties
```

```java
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "error.bad")
public class BadRequestException extends RuntimeException {
}
```

```java
@GetMapping("/api/response-status-ex1")
public String responseStatusEx1() {
    throw new BadRequestException();
}
```

#### **결과**
- **요청**: `/api/response-status-ex1`
- **응답**:
```json
{
    "timestamp": "2025-01-20T06:18:09.853+00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "wrong request! use message properties",
    "path": "/api/response-status-ex1"
}
```

---

### ResponseStatusException을 사용한 404 에러 처리

`ResponseStatusException`은 런타임 시 동적으로 HTTP 상태 코드, 메시지, 예외 타입을 설정할 수 있는 방식입니다.

#### **코드**
```java
@GetMapping("/api/response-status-ex2")
public String responseStatusEx2() {
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error.bad", new IllegalArgumentException());
}
```

#### **결과**
- **요청**: `/api/response-status-ex2`
- **응답**:
```json
{
    "timestamp": "2025-01-20T05:51:41.711+00:00",
    "status": 404,
    "error": "Not Found",
    "message": "wrong request! use message properties",
    "path": "/api/response-status-ex2"
}
```

---

### HTML 뷰가 반환된 원인과 해결 방법

초기 테스트에서는 JSON 형식의 응답이 아닌 HTML 뷰가 반환되는 문제가 있었습니다. 원인은 `WebServerCustomizer`에서 404 에러 발생 시 특정 경로(`/error-page/404`)로 리디렉션하도록 설정했기 때문입니다.

#### **문제 코드 (`WebServerCustomizer`)**
```java
@Component
public class WebServerCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND, "/error-page/404");
        factory.addErrorPages(errorPage404);
    }
}
```

#### **해결 방법**
- `WebServerCustomizer`의 `@Component` 주석 처리하면 Spring Boot의 기본 에러 처리 메커니즘이 동작하며, JSON 형식의 응답을 확인할 수 있습니다.



---
# 250116
## [MVC2 8-2] 예외처리와 오류 페이지 (2)
### **오류 페이지 처리 및 속성 출력 테스트**

---

### **1. 구현한 오류 페이지**
- **`templates/error` 디렉토리**:
    - 동적 오류 페이지 (Thymeleaf 템플릿 엔진 사용)
        - `500.html`: HTTP 500 (내부 서버 오류)
        - `5xx.html`: HTTP 5xx (서버 오류 범위)
        - `error.html`: 모든 오류 처리 기본 페이지

- **`static/error` 디렉토리**:
    - 정적 오류 페이지
        - `404.html`: HTTP 404 (페이지를 찾을 수 없음)
        - `4xx.html`: HTTP 4xx (클라이언트 오류 범위)

---

### **2. 테스트 결과**
| **URL**                       | **표시되는 페이지**       | **설명**                             |
|--------------------------------|---------------------------|---------------------------------------|
| `http://localhost:8080/error-404` | `404.html`               | 404 오류 발생 시 표시                 |
| `http://localhost:8080/error-400` | `4xx.html`               | 400 오류 페이지가 없을 경우, 4xx로 대체 |
| `http://localhost:8080/error-500` | `500.html`               | 500 오류 발생 시 표시                 |
| `http://localhost:8080/error-ex`  | `500.html`               | 예외 발생 시 500.html로 처리           |

---

### **3. BasicErrorController를 통해 확인된 속성**
**기본 속성**:
1. `timestamp`: 오류 발생 시간
2. `path`: 요청 경로
3. `status`: HTTP 상태 코드
4. `message`: 오류 메시지
5. `error`: 오류 유형
6. `exception`: 발생한 예외
7. `errors`: 바인딩 오류
8. `trace`: 스택 트레이스

---

### **4. 속성 출력**
- **Thymeleaf 사용**:
    - 위 속성들을 `th:text`로 출력하여 오류 정보를 페이지에 표시.
    - 예외(`exception`, `trace` 등) 정보는 기본적으로 출력되지 않음.

---

### **5. 추가 설정**
`application.properties` 파일에 다음 설정 추가:
```properties
server.error.include-exception=true
server.error.include-message=always
server.error.include-stacktrace=always
server.error.include-binding-errors=always
```

- **설명**:
    - `server.error.include-exception`: 예외 정보를 모델에 포함.
    - `server.error.include-message`: 오류 메시지 포함 여부 (`always`로 설정 시 항상 포함).
    - `server.error.include-stacktrace`: 스택 트레이스 포함 여부 (`always`로 설정 시 항상 포함).
    - `server.error.include-binding-errors`: 바인딩 오류 포함 여부.

---

### **6. 테스트 시나리오**
1. **추가 설정 후** `http://localhost:8080/error-ex` 호출:
    - 예외 발생 시 스택 트레이스(`trace`)와 예외(`exception`) 정보를 포함하여 페이지에 출력.
2. 스택 트레이스 정보는 `500.html` 페이지에 렌더링:
   ```html
   <pre th:text="${trace}"></pre>
   ```

---

### **7. 주의사항**
- 스택 트레이스와 예외 정보를 직접 출력할 경우 보안에 유의해야 합니다.
- 배포 환경에서는 아래와 같이 설정하여 스택 트레이스를 포함하지 않는 것이 권장됩니다:
  ```properties
  server.error.include-stacktrace=never
  server.error.include-message=never
  server.error.include-exception=false
  ```
---
## [MVC2 9-1] API 예외 처리 (1)

### API 예외 처리 구현


---

### **1. 코드 설명**

#### **1.1. `ApiExceptionController`**
`ApiExceptionController`는 예외 발생 시나리오를 테스트하기 위한 API 엔드포인트를 제공합니다.

- **엔드포인트**: `/api/members/{id}`
    - 유효한 ID의 경우 `MemberDto` 객체를 반환합니다.
    - `id`가 `"ex"`일 경우 `RuntimeException`을 발생시키며, 메시지는 `"wrong user"`입니다.
- **DTO 설명**:
    - `MemberDto`는 간단한 데이터 전달 객체로, 다음과 같은 필드를 가집니다:
        - `memberId`: 멤버의 ID.
        - `name`: `id`를 기반으로 생성된 이름.

---

#### **1.2. `ErrorPageController`**
이 컨트롤러는 예외 발생 시 JSON 형식의 오류 응답을 생성하여 반환합니다.


- **엔드포인트**: `/error-page/500`
    - HTTP 500 오류가 발생했을 때 JSON 응답을 생성하여 클라이언트에 반환합니다.
- **구현 로직**:
    1. `HttpServletRequest` 객체에서 예외 및 오류 관련 속성을 가져옵니다:
        - `ERROR_EXCEPTION`: 발생한 예외 객체.
        - `ERROR_STATUS_CODE`: HTTP 상태 코드.
    2. 오류 응답 데이터를 `Map`에 추가:
        - `status`: HTTP 상태 코드.
        - `message`: 예외 메시지.
    3. `ResponseEntity`를 통해 JSON 응답과 상태 코드를 반환.

---

### **2. JSON 응답**

- `/api/members/ex` 요청 시 반환되는 JSON 예시:
```json
{
    "status": 500,
    "message": "wrong user"
}
```

---

### **참고 servlet 계층 구조**

```
[Interface] ServletRequest
       ↑
       │ extends
[Interface] HttpServletRequest
       ↑                           ↑
       │ implements                │ implements
[Class] ServletRequestWrapper      [Class] HttpServletRequestWrapper
       ↑                           ↑
       │ extends                   │ extends
[Class] ApplicationRequest         [Class] ApplicationHttpRequest
```

---

### **구조 설명**

1. **`ServletRequest`** *(Interface)*:
    - 모든 서블릿 요청 객체의 기본 인터페이스.
    - `getAttribute`, `setAttribute` 등의 메서드를 정의.

2. **`HttpServletRequest`** *(Interface)*:
    - `ServletRequest`를 확장한 인터페이스.
    - HTTP 프로토콜에 특화된 메서드(`getHeader`, `getCookies`, `getMethod` 등)를 추가로 정의.

3. **`ServletRequestWrapper`** *(Class)*:
    - `ServletRequest`를 구현한 기본 래퍼 클래스.
    - 요청 객체를 감싸서 확장 가능.

4. **`HttpServletRequestWrapper`** *(Class)*:
    - `ServletRequestWrapper`를 확장하고, `HttpServletRequest`를 구현.
    - HTTP 요청 객체를 감싸서 확장 가능.

5. **`ApplicationRequest`** *(Class)*:
    - `ServletRequestWrapper`를 상속받아 요청 속성(attributes)과 매개변수(parameters)를 관리.

6. **`ApplicationHttpRequest`** *(Class)*:
    - `HttpServletRequestWrapper`를 상속받아 HTTP 요청 속성과 매개변수를 처리.

---

### **ApplicationRequest의 `attributes` 구현**

- **`attributes` 필드**:
    - `ApplicationRequest` 클래스는 요청 속성을 저장하기 위해 `HashMap<String, Object>`를 사용.
    - 이 필드는 요청 범위(request scope) 내의 속성 정보를 관리.

#### **예제 코드**

```java
protected final HashMap<String, Object> attributes = new HashMap<>();
```

---

### **setAttribute 메서드의 동작**
- `ApplicationRequest`는 `ServletRequest`의 `setAttribute` 메서드를 재정의하여 속성을 관리.

#### **구현 코드**
```java
public void setAttribute(String name, Object value) {
    synchronized(this.attributes) { // 동기화 처리
        this.attributes.put(name, value); // attributes 맵에 값 저장
        if (!this.isSpecial(name)) { // 특정 속성은 제외
            this.getRequest().setAttribute(name, value); // 래핑된 요청 객체에 전달
        }
    }
}
```

- **주요 동작**:
    1. **속성 저장**: `attributes` 맵에 키-값 쌍으로 속성을 저장.
    2. **특정 속성 제외**: `isSpecial` 메서드로 확인된 특별한 속성은 래핑된 요청 객체에 저장하지 않음.
    3. **래핑된 요청과 동기화**: `this.getRequest().setAttribute`를 통해 내부 요청 객체와 동기화.

---

#### **`getAttribute` 메서드의 구현**
```java
public Object getAttribute(String name) {
    synchronized(this.attributes) { // 동기화 처리
        return this.attributes.get(name); // attributes 맵에서 값 반환
    }
}
```

- **주요 동작**:
    1. **속성 검색**: `attributes` 맵에서 주어진 이름(`name`)으로 저장된 값을 가져옵니다.
    2. **동기화**: 다중 쓰레드 환경에서도 안전하게 동작하도록 `attributes`에 동기화 처리.
    3. **속성 반환**: 해당 이름의 값이 존재하면 반환하고, 없으면 `null`을 반환.

---

## Spring Boot Custom Exception Resolver


---

### **1. 주요 기능**
1. **`ApiExceptionController`의 예외 처리 확장**:
    - `IllegalArgumentException` 발생 시 HTTP 400 (Bad Request) 응답 반환.
2. **`MyHandlerExceptionResolver` 구현**:
    - 사용자 정의 예외 리졸버를 통해 `IllegalArgumentException`을 HTTP 400으로 매핑.
3. **`WebConfig`를 통한 Exception Resolver 등록**:
    - Spring MVC의 기본 예외 리졸버 체인에 사용자 정의 리졸버 추가.

---

### **2. 주요 구현**

#### **2.1. `ApiExceptionController`**
- `IllegalArgumentException` 발생 시 `MyHandlerExceptionResolver`가 동작하여 HTTP 400 상태 코드를 반환.


---

#### **2.2. 사용자 정의 예외 리졸버**
- `MyHandlerExceptionResolver`는 `IllegalArgumentException`을 HTTP 400 응답으로 처리.


---

#### **2.3. `WebConfig`에서 리졸버 등록**
- `extendHandlerExceptionResolvers` 메서드를 오버라이드하여 사용자 정의 리졸버를 Spring MVC의 리졸버 체인에 추가.


---

### **3. 동작 흐름**
1. `/api/members/bad`로 요청:
    - `IllegalArgumentException` 발생.
2. Spring MVC가 `HandlerExceptionResolver` 체인에서 `MyHandlerExceptionResolver`를 호출.
3. `MyHandlerExceptionResolver`가 `IllegalArgumentException`을 처리하고 HTTP 400 상태 코드와 메시지를 반환.

---

### **4. 테스트 예시**

**요청**:
```http
GET /api/members/bad
```

**응답**:
```json
{
  "timestamp": "2025-01-16T07:03:48.760+00:00",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/members/bad"
}
```
**추가사항**
- BasicErrorController가 처리한 결과를 확인하기 위함이라 WebServerCustomizer.java의 component를 주석처리하고 테스트했다.

---
# 250115


## Spring Boot 내장 Tomcat 서버와 오류 페이지 커스터마이징


### 주요 개념

1. **TomcatServletWebServerFactory**
    - Spring Boot에서 내장 Tomcat 서버의 초기화와 설정을 담당하는 클래스.
    - 서블릿 컨텍스트 초기화, 오류 페이지 등록, 요청 파이프라인 설정 등 다양한 커스터마이징 기능 제공.

2. **ErrorPage**
    - HTTP 상태 코드 또는 특정 예외에 대해 사용자 정의 오류 페이지를 설정하기 위한 객체.
    - `status`와 `path`를 지정하여 오류 발생 시 표시할 페이지 경로를 설정.

---

### 오류 페이지 등록 과정

오류 페이지 등록은 `configureContext` 메서드 내에서 수행되며, 다음 과정을 통해 Tomcat에 설정됩니다:

1. **Spring Boot의 `ErrorPage`를 Tomcat의 `ErrorPage`로 변환**
    - `ErrorPage` 객체의 `status`, `exception` 정보를 사용하여 Tomcat 오류 페이지 객체 생성.
    - `addErrorPage`를 호출해 Tomcat `Context`에 오류 페이지를 등록.

   ```java
   var7 = this.getErrorPages().iterator();

   while (var7.hasNext()) {
       ErrorPage errorPage = (ErrorPage) var7.next();
       org.apache.tomcat.util.descriptor.web.ErrorPage tomcatErrorPage = new org.apache.tomcat.util.descriptor.web.ErrorPage();
       tomcatErrorPage.setLocation(errorPage.getPath());
       tomcatErrorPage.setErrorCode(errorPage.getStatusCode());
       tomcatErrorPage.setExceptionType(errorPage.getExceptionName());
       context.addErrorPage(tomcatErrorPage);
   }
   ```

2. **오류 페이지 설정의 주요 필드**
    - **`status`**: HTTP 상태 코드(예: 404, 500).
    - **`path`**: 오류 발생 시 표시할 페이지의 경로.
    - **`exception`**: 특정 예외 발생 시 처리할 오류 페이지.

---

### `TomcatServletWebServerFactory`의 주요 메서드

1. **`getWebServer`**
    - Tomcat 서버 인스턴스를 생성하고 초기화 작업 수행.
    - 서블릿 컨텍스트 초기화, 오류 페이지 및 기본 서블릿 설정.

2. **`configureContext`**
    - `ServletContext` 초기화 및 주요 설정 수행:
        - 오류 페이지 등록.
        - 요청 파이프라인(`Valve`) 구성.
        - MIME 매핑, 세션 설정.

3. **`customizeConnector`**
    - Tomcat `Connector`의 커스터마이징 설정(포트, URI 인코딩, HTTP/2 등).

---

### `@FunctionalInterface`와 Lambda

#### 정의
`@FunctionalInterface`는 하나의 추상 메서드만 가지는 인터페이스를 정의할 때 사용됩니다. 이는 Java 8의 람다 표현식과 함께 사용되어 간결한 함수형 프로그래밍 스타일을 제공합니다.

#### 주요 특징
- 단일 추상 메서드(Single Abstract Method, SAM) 인터페이스.
- `default` 및 `static` 메서드를 포함 가능.
- 컴파일 타임에 함수형 인터페이스임을 검증.

#### 예제
```java
@FunctionalInterface
public interface Calculator {
    int calculate(int a, int b);
}

public class Main {
    public static void main(String[] args) {
        Calculator add = (a, b) -> a + b;
        System.out.println(add.calculate(3, 5)); // 출력: 8
    }
}
```


---
# 250114

## [MVC2 8-1] 예외처리와 오류 페이지 (1)

### 주요 변경 사항
#### 1. `WebServerCustomizer.java` 디렉토리 구조 변경
- 프로젝트 구조를 개선하기 위해 `WebServerCustomizer.java`의 디렉토리 구조를 변경하였습니다. 이를 통해 유지보수성과 가독성을 높였습니다.

#### 2. `ErrorPageController`
- 오류 처리를 위한 컨트롤러로, 다음과 같은 필드를 설정하고 관련 내용을 구현하였습니다:
    - `ERROR_EXCEPTION`
    - `ERROR_EXCEPTION_TYPE`
    - `ERROR_MESSAGE`
    - `ERROR_REQUEST_URI`
    - `ERROR_SERVLET_NAME`
    - `ERROR_STATUS_CODE`

> **주의**: Spring Boot 6.x 버전에서는 Jakarta Servlet API를 사용하므로, 오류 속성 접근 시 `javax.servlet.error.*`가 아닌 `jakarta.servlet.error.*`를 사용해야 합니다.

### 실행 로그
아래는 애플리케이션 실행 중 발생한 로그를 포함하여, `ErrorPageController`가 잘 작동하고 있는지 확인할 수 있습니다.

### 404 에러 발생 시 로그
```
2025-01-14T11:24:18.141+09:00  INFO 44716 --- [exception] [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2025-01-14T11:24:18.141+09:00  INFO 44716 --- [exception] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2025-01-14T11:24:18.142+09:00  INFO 44716 --- [exception] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
2025-01-14T11:24:18.170+09:00  INFO 44716 --- [exception] [nio-8080-exec-1] h.exception.servlet.ErrorPageController  : errorPage 404
2025-01-14T11:24:18.170+09:00  INFO 44716 --- [exception] [nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_EXCEPTION: ex= null
2025-01-14T11:24:18.171+09:00  INFO 44716 --- [exception] [nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_EXCEPTION_TYPE: null
2025-01-14T11:24:18.171+09:00  INFO 44716 --- [exception] [nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_MESSAGE: No static resource testWrongURI.
2025-01-14T11:24:18.171+09:00  INFO 44716 --- [exception] [nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_REQUEST_URI: /testWrongURI
2025-01-14T11:24:18.171+09:00  INFO 44716 --- [exception] [nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_SERVLET_NAME: dispatcherServlet
2025-01-14T11:24:18.171+09:00  INFO 44716 --- [exception] [nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_STATUS_CODE: 404
2025-01-14T11:24:18.171+09:00  INFO 44716 --- [exception] [nio-8080-exec-1] h.exception.servlet.ErrorPageController  : dispatchType=ERROR
```

### 500 에러 발생 시 로그
```
2025-01-14T11:11:09.367+09:00 ERROR 121468 --- [exception] [nio-8080-exec-1] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: java.lang.RuntimeException: exception occur!!!] with root cause

java.lang.RuntimeException: exception occur!!!

2025-01-14T11:11:09.386+09:00  INFO 121468 --- [exception] [nio-8080-exec-1] h.exception.servlet.ErrorPageController  : errorPage 500
2025-01-14T11:11:09.386+09:00  INFO 121468 --- [exception] [nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_EXCEPTION: ex= {}

java.lang.RuntimeException: exception occur!!!
    
2025-01-14T11:11:09.388+09:00  INFO 121468 --- [exception] [nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_EXCEPTION_TYPE: class java.lang.RuntimeException
2025-01-14T11:11:09.389+09:00  INFO 121468 --- [exception] [nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_MESSAGE: Request processing failed: java.lang.RuntimeException: exception occur!!!
2025-01-14T11:11:09.389+09:00  INFO 121468 --- [exception] [nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_REQUEST_URI: /error-ex
2025-01-14T11:11:09.389+09:00  INFO 121468 --- [exception] [nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_SERVLET_NAME: dispatcherServlet
2025-01-14T11:11:09.389+09:00  INFO 121468 --- [exception] [nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_STATUS_CODE: 500
2025-01-14T11:11:09.389+09:00  INFO 121468 --- [exception] [nio-8080-exec-1] h.exception.servlet.ErrorPageController  : dispatchType=ERROR
```
---
## 요청 및 응답 로깅을 위한 LogFilter 구현


### 주요 기능

- **요청 및 응답 로깅**: 들어오는 모든 요청과 나가는 응답을 로깅합니다.
- **고유 식별자**: 각 요청/응답 쌍에 대해 고유한 UUID를 생성하여 식별합니다.
- **다양한 DispatcherType 지원**: `REQUEST`와 `ERROR` 타입으로 디스패치된 요청의 세부 정보를 로깅합니다.
- **URL 패턴 및 우선순위 설정**: 모든 URL 패턴(`/*`)에 필터를 적용하며, 우선순위가 가장 높은 값(`order = 1`)으로 설정됩니다.

---

### 코드 개요

#### 1. `LogFilter` 클래스

`Filter` 인터페이스를 구현하여 요청과 응답 세부 정보를 캡처하고 로깅합니다.

**핵심 메서드**:
- `init(FilterConfig filterConfig)`: 필터를 초기화하고 시작 메시지를 로깅합니다.
- `doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)`: 요청 세부 정보를 로깅하고, 필터 체인을 실행한 후 응답 세부 정보를 로깅합니다.
- `destroy()`: 필터 종료 시 종료 메시지를 로깅합니다.



---

#### 2. `WebConfig` 클래스

Spring Boot 애플리케이션에서 `LogFilter`를 등록하고 설정하는 역할을 합니다.


---

### 예시 로그 출력

다음은 `LogFilter`가 출력한 예시 로그입니다:

```text
[nio-8080-exec-1] hello.exception.filter.LogFilter         : REQUEST [9db79b69-5a53-40fb-90b2-21bc46c1c66c][REQUEST][/testWrongURI]
[nio-8080-exec-1] hello.exception.filter.LogFilter         : RESPONSE [9db79b69-5a53-40fb-90b2-21bc46c1c66c][REQUEST][/testWrongURI]
[nio-8080-exec-1] hello.exception.filter.LogFilter         : REQUEST [793bbbce-b762-4e0e-9dfb-08b4d8ce22e7][ERROR][/error-page/404]
[nio-8080-exec-1] h.exception.servlet.ErrorPageController  : errorPage 404
[nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_EXCEPTION: ex= null
[nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_EXCEPTION_TYPE: null
[nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_MESSAGE: No static resource testWrongURI.
[nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_REQUEST_URI: /testWrongURI
[nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_SERVLET_NAME: dispatcherServlet
[nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_STATUS_CODE: 404
[nio-8080-exec-1] h.exception.servlet.ErrorPageController  : dispatchType=ERROR
[nio-8080-exec-1] hello.exception.filter.LogFilter         : RESPONSE [793bbbce-b762-4e0e-9dfb-08b4d8ce22e7][ERROR][/error-page/404]
```

---


### 참고 사항

- `DispatcherType.ERROR`를 활성화하려면 `WebConfig`에서 주석을 해제하십시오.

---

## 요청 및 응답 로깅을 위한 LogInterceptor 구현



### 주요 기능

- **요청 전처리 (`preHandle`)**:
    - 각 요청에 대해 고유한 UUID를 생성하여 요청을 식별합니다.
    - 요청 URI, `DispatcherType`, 핸들러 정보를 로그에 기록합니다.
- **요청 후처리 (`postHandle`)**:
    - 요청 처리 후 `ModelAndView` 객체의 상태를 로그에 기록합니다.
- **요청 완료 처리 (`afterCompletion`)**:
    - 요청 처리 완료 후 UUID, 요청 URI를 기록합니다.
    - 처리 중 예외가 발생한 경우, 예외 정보를 로그에 기록합니다.
- **경로 필터링**:
    - 특정 경로(`css`, `error`, `.ico` 등)를 제외하고 지정된 경로의 요청만 로깅합니다.

---

### 코드 개요

#### 1. LogInterceptor 클래스

`LogInterceptor`는 스프링 인터셉터를 통해 요청 처리 단계를 로깅합니다.


---

#### 2. WebConfig 클래스에서 인터셉터 등록

`LogInterceptor`를 스프링 컨텍스트에 등록하여 요청 로깅을 활성화합니다.


---

### 예시 로그 출력

다음은 요청 처리 중 `LogInterceptor`가 기록한 로그 결과입니다:

```text
[nio-8080-exec-1] hello.exception.filter.LogFilter         : REQUEST [53647f49-0e49-41e8-a395-d224cf471554][REQUEST][/error-500]
[nio-8080-exec-1] h.exception.interceptor.LogInterceptor   : PREHANDLE [39acbc0b-8f33-4ca5-b29d-cc18ca58e184][REQUEST][/error-500][hello.exception.servlet.ServletExController#error500(HttpServletResponse)]
[nio-8080-exec-1] h.exception.interceptor.LogInterceptor   : POSTHANDLE null
[nio-8080-exec-1] h.exception.interceptor.LogInterceptor   : AFTER COMPLETION [39acbc0b-8f33-4ca5-b29d-cc18ca58e184][/error-500]
[nio-8080-exec-1] hello.exception.filter.LogFilter         : RESPONSE [53647f49-0e49-41e8-a395-d224cf471554][REQUEST][/error-500]

[nio-8080-exec-1] h.exception.interceptor.LogInterceptor   : PREHANDLE [34d7f61e-f0ea-42b2-b23c-8e6c2c9d72fb][ERROR][/error-page/500][hello.exception.servlet.ErrorPageController#errorPage500(HttpServletRequest, HttpServletResponse)]
[nio-8080-exec-1] h.exception.servlet.ErrorPageController  : errorPage 500
[nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_EXCEPTION: ex= null
[nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_EXCEPTION_TYPE: null
[nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_MESSAGE: 500 error!
[nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_REQUEST_URI: /error-500
[nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_SERVLET_NAME: dispatcherServlet
[nio-8080-exec-1] h.exception.servlet.ErrorPageController  : ERROR_STATUS_CODE: 500
[nio-8080-exec-1] h.exception.servlet.ErrorPageController  : dispatchType=ERROR
[nio-8080-exec-1] h.exception.interceptor.LogInterceptor   : POSTHANDLE ModelAndView [view="/error-page/500"; model={}]
[nio-8080-exec-1] h.exception.interceptor.LogInterceptor   : AFTER COMPLETION [34d7f61e-f0ea-42b2-b23c-8e6c2c9d72fb][/error-page/500]
```



- 인터셉터는 컨트롤러 호출 직전과 직후에 동작하며, 응답 완료 후 예외 상황도 처리할 수 있습니다.
- 로깅 제외 경로를 설정하여 불필요한 로그 생성을 방지할 수 있습니다.



---
# 250113
## ThreadPoolExecutor Test

### 주요 기능

1. **ThreadPoolExecutor 초기화**:
    - 스레드 풀의 동작 설정 (최소/최대 스레드 수, 작업 큐, 거부 정책 등).
    - 작업 제출 및 상태 로깅.

2. **작업 큐 및 거부 정책**:
    - 크기 제한된 작업 큐 (`LinkedBlockingQueue`) 사용.
    - 큐가 가득 찼을 때 `CallerRunsPolicy`를 통해 현재 호출 스레드에서 작업 실행.

3. **작업 처리**:
    - 10개의 작업을 제출하며, 각 작업은 2초 동안 실행.
    - 작업 중 인터럽트를 감지하고 처리.

4. **스레드 풀 종료**:
    - `shutdown()`과 `awaitTermination()`을 사용하여 스레드 풀 정상 종료 시도.
    - 20초 내에 종료되지 않을 경우 강제 종료 (`shutdownNow()`).

5. **스레드 풀 상태 로깅**:
    - 스레드 풀의 상태(스레드 수, 대기 중인 작업, 완료된 작업 등)를 출력.

---

### 코드 설명

#### 1. **ThreadPoolExecutor 초기화**
```java
BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(5);
RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();

ThreadPoolExecutor executor = new ThreadPoolExecutor(
        2,                       // corePoolSize
        4,                       // maximumPoolSize
        1,                       // keepAliveTime
        TimeUnit.MICROSECONDS,   // keepAliveTime 단위
        workQueue,               // 작업 큐
        Executors.defaultThreadFactory(), // 스레드 생성 팩토리
        handler                  // 거부 정책
);
```
- **설정 값**:
    - `corePoolSize`: 최소 스레드 수 (2)
    - `maximumPoolSize`: 최대 스레드 수 (4)
    - `keepAliveTime`: 초과 스레드의 유휴 시간 (1 마이크로초)
    - `workQueue`: 크기 5로 제한된 작업 큐
    - `handler`: `CallerRunsPolicy`를 통해 큐가 가득 찬 경우 호출 스레드에서 작업 실행

---

#### 2. **작업 제출 및 실행**
```java
for (int i = 1; i <= 10; i++) {
    final int taskId = i;
    executor.execute(() -> {
        log.info("Task {} is starting.", taskId);
        try {
            Thread.sleep(2000); // 작업 수행 시간
        } catch (InterruptedException e) {
            log.error("Task {} was interrupted.", taskId);
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
        }
        log.info("Task {} is completed.", taskId);
    });
    printThreadPoolStatus(executor, "Submitted Task " + taskId);
}
```
- **작업 실행**:
    - 10개의 작업이 제출되며, 작업 큐 또는 스레드 풀에 따라 실행됩니다.
    - 각 작업은 2초 동안 실행되며, 인터럽트 발생 시 로그 출력.
- **상태 출력**:
    - 작업 제출마다 스레드 풀 상태(`printThreadPoolStatus`)를 출력.

---

#### 3. **스레드 풀 종료**
```java
executor.shutdown();
try {
    if (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
        log.warn("Forcing shutdown...");
        executor.shutdownNow();
    }
} catch (InterruptedException e) {
    log.error("Interrupted during shutdown.");
    executor.shutdownNow();
    Thread.currentThread().interrupt();
}
printThreadPoolStatus(executor, "ThreadPool shutdown");
```
- **정상 종료**:
    - `shutdown()`으로 새로운 작업 제출을 막고, 기존 작업이 완료될 때까지 대기.
    - 20초 내에 종료되지 않을 경우 `shutdownNow()`를 호출하여 강제 종료.
- **인터럽트 처리**:
    - 종료 중 인터럽트 발생 시 로그 출력 및 상태 복원.

---

#### 4. **스레드 풀 상태 출력**
```java
private static void printThreadPoolStatus(ThreadPoolExecutor executor, String message) {
    log.info("\n[{}]", message);
    log.info("Pool size: {}", executor.getPoolSize());
    log.info("Active Threads: {}", executor.getActiveCount());
    log.info("Completed Tasks: {}", executor.getCompletedTaskCount());
    log.info("Task Queue Size: {}", executor.getQueue().size());
    long submittedTasks = executor.getTaskCount() - executor.getCompletedTaskCount();
    log.info("Submitted Tasks (Active + Queued): {}", submittedTasks);
    log.info("Largest Pool Size: {}", executor.getLargestPoolSize());
}
```
- **출력 정보**:
    - `Pool size`: 현재 생성된 스레드 수
    - `Active Threads`: 실행 중인 스레드 수
    - `Completed Tasks`: 완료된 작업 수
    - `Task Queue Size`: 대기 중인 작업 수
    - `Submitted Tasks`: 대기 및 실행 중인 작업 수
    - `Largest Pool Size`: 생성된 최대 스레드 수

---

### 실행 결과 예시

#### 작업 제출 중 상태 출력
```plaintext
[Submitted Task 1]
Pool size: 1
Active Threads: 1
Completed Tasks: 0
Task Queue Size: 0
Submitted Tasks (Active + Queued): 1
Largest Pool Size: 1

[Submitted Task 6]
Pool size: 4
Active Threads: 4
Completed Tasks: 2
Task Queue Size: 2
Submitted Tasks (Active + Queued): 4
Largest Pool Size: 4
```

#### 스레드 풀 종료 상태 출력
```plaintext
[ThreadPool shutdown]
Pool size: 0
Active Threads: 0
Completed Tasks: 10
Task Queue Size: 0
Submitted Tasks (Active + Queued): 0
Largest Pool Size: 4
```

---

### 추가된 기능: 스레드 풀 크기 동적 조정 테스트

이 코드는 **`ThreadPoolExecutor`의 코어 스레드 크기와 최대 스레드 크기를 동적으로 조정**하여 작업 처리에 미치는 영향을 테스트합니다. 각 설정 변경 후 새로운 작업을 추가하며, 스레드 풀 상태를 확인합니다.

---

### 주요 기능 설명

#### 1. **코어 스레드 크기 증가**
```java
Thread.sleep(3000);
executor.setCorePoolSize(3);
printThreadPoolStatus(executor, "Increased core pool size");
```
- **설명**:
    - 3초 대기 후, 코어 스레드 크기를 기존 2에서 3으로 증가.
    - 코어 스레드는 작업이 없더라도 유지되는 기본 스레드입니다.
    - 이를 통해 더 많은 작업을 동시에 처리할 수 있는지 확인.

---

#### 2. **최대 스레드 크기 감소**
```java
Thread.sleep(3000);
executor.setMaximumPoolSize(3);
printThreadPoolStatus(executor, "Decreased maximum poll size");
```
- **설명**:
    - 추가로 3초 대기 후, 최대 스레드 크기를 기존 4에서 3으로 감소.
    - 최대 스레드는 작업 큐에 대기 중인 작업을 처리하기 위해 추가로 생성 가능한 스레드입니다.
    - 최대 스레드 수를 줄임으로써 리소스 사용량이 감소하는지 확인.

---

#### 3. **새로운 작업 추가**
```java
for (int i = 11; i <= 15; i++) {
    final int taskId = i;
    executor.execute(() -> {
        log.info("!!!!!!!!!!!!!!!!!!!! Task {} is starting.", taskId);
        try {
            Thread.sleep(1000); // 1초 동안 작업 수행
        } catch (InterruptedException e) {
            log.error("???????????????????? Task {} was interrupted.", taskId);
            Thread.currentThread().interrupt();
        }
        log.info("???????????????????? Task {} is completed.", taskId);
    });
    printThreadPoolStatus(executor, "Submitted Task " + taskId);
}
```
- **설명**:
    - 작업 ID 11번부터 15번까지 총 5개의 작업을 새로 추가.
    - 각 작업은 1초 동안 실행되며, 스레드 풀이 이를 처리하는 과정을 모니터링.
    - 작업 제출 시마다 스레드 풀 상태를 출력하여 동작 확인.

---

### 스레드 풀 상태 출력 예시

#### 코어 스레드 크기 증가 후
```plaintext
[Increased core pool size]
Pool size: 3
Active Threads: 3
Completed Tasks: 5
Task Queue Size: 0
Largest Pool Size: 3
```

#### 최대 스레드 크기 감소 후
```plaintext
[Decreased maximum pool size]
Pool size: 3
Active Threads: 3
Completed Tasks: 5
Task Queue Size: 0
Largest Pool Size: 4
```

#### 새로운 작업 추가 시
```plaintext
[Submitted Task 11]
Pool size: 3
Active Threads: 3
Completed Tasks: 5
Task Queue Size: 0

[Submitted Task 12]
Pool size: 3
Active Threads: 3
Completed Tasks: 5
Task Queue Size: 1
```

---


1. **코어 스레드 크기 조정의 영향**:
    - 코어 스레드 크기 증가로 더 많은 작업을 동시에 처리 가능.
    - 동적으로 풀 크기를 변경해도 기존 작업은 영향을 받지 않음.

2. **최대 스레드 크기 감소의 영향**:
    - 최대 스레드 수가 줄어들면 작업이 큐에 대기하는 시간이 증가.
    - 최대 스레드 크기 감소가 과도할 경우 대기 시간이 길어질 수 있음.

3. **새로운 작업 처리**:
    - 작업 추가 후 스레드 풀 상태를 실시간으로 모니터링.
    - 큐 크기와 활성 스레드 수의 변화를 통해 설정 변경의 효과를 분석.


---


### 주의사항
- **스레드 풀 크기 설정**:
  환경에 맞는 `corePoolSize`, `maximumPoolSize`, `keepAliveTime` 값을 설정하여 성능 최적화.
- **거부 정책 선택**:
  작업 과부하 시 적절한 정책(`AbortPolicy`, `CallerRunsPolicy`, 등)을 설정해야 함.
- **정상 종료 보장**:
  스레드 풀 종료 시 `shutdown()`과 `awaitTermination()`을 통해 리소스 누수 방지.


---
# 250110

## [MVC2 8-1] 예외처리와 오류페이지(1)

#### 1. **`application.properties`**

```properties
spring.application.name=exception
server.error.whitelabel.enabled=false
```

- `server.error.whitelabel.enabled=false`: Spring Boot의 기본 Whitelabel Error Page 비활성화.

---

#### 2. **`WebServerCustomizer.java`**
Spring Boot에서 발생하는 특정 HTTP 상태 코드 또는 예외를 사용자 정의 에러 페이지로 매핑하는 설정 클래스.


- HTTP `404 Not Found`: `/error-page/404` 경로로 매핑.
- HTTP `500 Internal Server Error`: `/error-page/500` 경로로 매핑.
- `RuntimeException`: `/error-page/500` 경로로 매핑.

---

#### 3. **`ErrorPageController.java`**
에러 페이지 요청을 처리하는 컨트롤러 클래스.

- `/error-page/404`: HTTP 404 상태 발생 시 호출.
- `/error-page/500`: HTTP 500 상태 또는 런타임 예외 발생 시 호출.
- 각각 요청을 로그에 기록하고, HTML 파일 경로를 반환.

---

#### 4. **`404.html`**
404 에러에 대한 사용자 정의 페이지.

- "404 - Page Not Found" 메시지와 함께 홈 페이지로 돌아가는 링크 제공.

---

#### 5. **`500.html`**
500 에러에 대한 사용자 정의 페이지.

- "500 - Internal Server Error" 메시지와 함께 홈 페이지로 돌아가는 링크 제공.

---


1. **에러 발생**:
    - 특정 HTTP 상태 코드(404, 500) 또는 예외(`RuntimeException`)가 발생.
2. **에러 페이지 매핑**:
    - `WebServerCustomizer`에서 설정된 경로로 요청을 리다이렉트.
3. **에러 처리 컨트롤러 호출**:
    - `ErrorPageController`에서 요청을 처리하고 해당 HTML 파일 반환.
4. **사용자 정의 페이지 렌더링**:
    - `404.html` 또는 `500.html`이 클라이언트에 렌더링.

---

### 테스트

1. **404 에러 테스트**:
    - 존재하지 않는 URL로 접속 (예: `/non-existing-page`).
    - 브라우저에서 "404 - Page Not Found" 페이지 확인.

2. **500 에러 테스트**:
    - 예외를 발생시키는 URL로 접속 (예: `RuntimeException` 발생 시뮬레이션).
    - 브라우저에서 "500 - Internal Server Error" 페이지 확인.

